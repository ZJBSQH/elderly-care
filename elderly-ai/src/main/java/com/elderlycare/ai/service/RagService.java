package com.elderlycare.ai.service;

import com.elderlycare.ai.dto.AiErrorCode;
import com.elderlycare.common.core.exception.BusinessException;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * RAG 增强问答服务
 * <p>
 * 核心流程：用户问题 → 向量嵌入 → KNN 检索 → Prompt 增强 → AI 流式生成。
 * 将检索到的医学知识片段与用户个人数据合并，构建增强 Prompt 后交给大模型回答。
 * <p>
 * 降级策略：若检索失败，自动退化为纯大模型回答（仅使用用户个人上下文）。
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    /** 千问嵌入模型，将用户问题转为向量 */
    private final EmbeddingModel embeddingModel;

    /** Redis 向量存储，执行 KNN 语义检索 */
    private final RedisEmbeddingStore embeddingStore;

    /** 现有 AI 服务，流式生成回答 */
    private final MedicalAssistant assistant;

    /** 医疗上下文构建器，获取用户个人数据 */
    private final MedicalContextBuilder contextBuilder;

    /** 检索返回的最大片段数 */
    @Value("${elderly.rag.retrieval.max-results:5}")
    private int maxResults;

    /** 最低相似度阈值 */
    @Value("${elderly.rag.retrieval.min-score:0.65}")
    private double minScore;

    /**
     * RAG 增强问答（SSE 流式输出）
     * <p>
     * 1. 将用户问题向量化
     * 2. 在知识库中检索 Top-K 相似片段
     * 3. 获取用户个人医疗上下文
     * 4. 组装增强 Prompt（知识 + 个人数据 + 问题）
     * 5. 流式生成 AI 回答
     *
     * @param elderId  老人 ID（用于个性化上下文）
     * @param question 用户问题
     * @return 流式 AI 回答
     */
    public Flux<String> askWithRag(Integer elderId, String question) {
        log.info("RAG 问答: elderId={}, question={}", elderId, question);

        // 步骤1: 检索相关知识片段
        List<EmbeddingMatch<TextSegment>> relevantDocs;
        try {
            relevantDocs = retrieveRelevant(question);
            log.info("检索到 {} 个相关片段", relevantDocs.size());
        } catch (Exception e) {
            log.error("向量检索失败，降级为纯大模型回答", e);
            // 降级：不使用知识库，仅用个人上下文
            String fallbackContext = elderId != null
                    ? contextBuilder.buildAndFormat(elderId)
                    : "（无个人数据）";
            return assistant.chat(fallbackContext + "\n\n用户问题: " + question);
        }

        // 步骤2: 获取个性化医疗上下文
        String personalContext = elderId != null
                ? contextBuilder.buildAndFormat(elderId)
                : "（无个人数据）";

        // 步骤3: 组装增强 Prompt
        String augmentedPrompt = buildAugmentedPrompt(relevantDocs, personalContext, question);
        log.info("增强 Prompt 长度: {} 字符", augmentedPrompt.length());

        // 步骤4: 流式生成
        return assistant.chat(augmentedPrompt);
    }

    /**
     * 向量检索：将问题向量化后在 Redis 中执行 KNN 搜索
     */
    private List<EmbeddingMatch<TextSegment>> retrieveRelevant(String question) {
        Embedding queryEmbedding = embeddingModel.embed(question).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        return result.matches();
    }

    /**
     * 组装增强 Prompt（核心）
     * <p>
     * Prompt 结构：
     * <pre>
     * 【参考知识】（来自医学知识库）
     *   片段1 (来源: 布洛芬说明书)
     *   片段2 (来源: 用药安全指南)
     * 【用户个人数据】
     *   药品清单、今日用药记录、提醒任务
     * 【用户问题】
     *   布洛芬可以和阿司匹林一起吃吗？
     * </pre>
     */
    private String buildAugmentedPrompt(
            List<EmbeddingMatch<TextSegment>> docs,
            String personalContext,
            String question) {

        StringBuilder sb = new StringBuilder();
        sb.append("【参考知识】请基于以下专业知识回答用户问题，如果参考知识不足以回答，请明确说明：\n");

        for (int i = 0; i < docs.size(); i++) {
            TextSegment segment = docs.get(i).embedded();
            String title = segment.metadata().getString("title");
            String source = title != null ? title : "未知来源";
            double score = docs.get(i).score();
            sb.append(String.format("--- 参考资料%d (来源: %s, 相关度: %.2f) ---\n%s\n",
                    i + 1, source, score, segment.text()));
        }

        sb.append("\n【用户个人数据】\n").append(personalContext);
        sb.append("\n【用户问题】").append(question);
        sb.append("\n\n请结合参考知识和用户个人数据，用自然段落行文给出专业、安全、易懂的回答，");
        sb.append("不使用 Markdown 格式。涉及用药安全时必须谨慎，建议咨询医生。");

        return sb.toString();
    }
}
