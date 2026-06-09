package com.elderlycare.ai.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * 基于 Redis Stack (RediSearch) 的向量存储实现
 * <p>
 * 实现 LangChain4j 的 {@link EmbeddingStore} 接口，将文本向量存储到 Redis Hash 中，
 * 通过 RediSearch 的 HNSW 索引实现高效的 KNN 语义检索。
 * <p>
 * 存储结构：
 * <ul>
 *   <li>Redis Key: {@code {prefix}{id}}</li>
 *   <li>每个 Key 是一个 Hash，包含 embedding(原始字节)、content(文本)、元数据字段</li>
 *   <li>RediSearch 索引建立在这些 Hash 上，支持 KNN 向量搜索</li>
 * </ul>
 *
 * @author 郑
 */
@Slf4j
public class RedisEmbeddingStore implements EmbeddingStore<TextSegment> {

    /** Jedis 客户端（连接 Redis Stack，支持 RediSearch 命令） */
    private final JedisPooled jedis;

    /** RediSearch 索引名称 */
    private final String indexName;

    /** Redis Key 前缀 */
    private final String keyPrefix;

    /** 向量维度 */
    private final int dimension;

    /** 索引是否已创建 */
    private volatile boolean indexCreated = false;

    /**
     * 构造向量存储实例
     *
     * @param jedis     Jedis 客户端
     * @param indexName 索引名称
     * @param keyPrefix Key 前缀
     * @param dimension 向量维度
     */
    public RedisEmbeddingStore(JedisPooled jedis, String indexName, String keyPrefix, int dimension) {
        this.jedis = jedis;
        this.indexName = indexName;
        this.keyPrefix = keyPrefix;
        this.dimension = dimension;
        ensureIndexExists();
    }

    // ═══════════════════════════════════════════
    // 写入操作
    // ═══════════════════════════════════════════

    @Override
    public String add(Embedding embedding) {
        String id = UUID.randomUUID().toString();
        add(id, embedding);
        return id;
    }

    @Override
    public void add(String id, Embedding embedding) {
        addInternal(id, embedding, null);
    }

    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        String id = UUID.randomUUID().toString();
        addInternal(id, embedding, textSegment);
        return id;
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        return embeddings.stream().map(this::add).toList();
    }

    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> textSegments) {
        if (embeddings.size() != textSegments.size()) {
            throw new IllegalArgumentException("嵌入向量数量与文本片段数量不一致");
        }
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < embeddings.size(); i++) {
            String id = UUID.randomUUID().toString();
            addInternal(id, embeddings.get(i), textSegments.get(i));
            ids.add(id);
        }
        return ids;
    }

    /**
     * 内部写入逻辑：将向量 + 文本片段序列化后存入 Redis Hash
     * <p>
     * 向量以 FLOAT32 小端字节序存储，文本和元数据以字符串形式存入 Hash 各字段。
     */
    private void addInternal(String id, Embedding embedding, TextSegment segment) {
        String key = keyPrefix + id;
        Map<String, String> fields = new HashMap<>();
        // 向量字段：原始字节（RediSearch 自动解析为 VECTOR）
        fields.put("embedding", bytesToBlob(embeddingToBytes(embedding)));
        if (segment != null) {
            // 文本内容
            fields.put("content", segment.text());
            // 元数据字段
            Metadata meta = segment.metadata();
            fields.put("title", safeString(meta, "title"));
            fields.put("category", safeString(meta, "category"));
            fields.put("doc_id", safeString(meta, "doc_id"));
            fields.put("chunk_index", safeString(meta, "chunk_index"));
            fields.put("created_at", safeString(meta, "created_at"));
        }
        jedis.hset(key, fields);
    }

    /** 安全地从 Metadata 中获取字符串值（兼容 Integer 等非 String 类型） */
    private String safeString(Metadata meta, String key) {
        if (meta == null) return "";
        Object val = meta.toMap().get(key);
        return val != null ? val.toString() : "";
    }

    // ═══════════════════════════════════════════
    // 删除操作
    // ═══════════════════════════════════════════

    @Override
    public void remove(String id) {
        jedis.del(keyPrefix + id);
    }

    @Override
    public void removeAll(Collection<String> ids) {
        if (ids.isEmpty()) return;
        String[] keys = ids.stream().map(id -> keyPrefix + id).toArray(String[]::new);
        jedis.del(keys);
    }

    /**
     * 按文档 ID 删除该文档的所有片段
     * <p>
     * 先通过 RediSearch 查询该 doc_id 的所有 key，再批量删除。
     *
     * @param docId 文档 ID
     */
    public void removeByDocId(String docId) {
        try {
            SearchResult result = jedis.ftSearch(indexName, "@doc_id:{" + docId + "}");
            List<String> keys = result.getDocuments().stream()
                    .map(Document::getId)
                    .toList();
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
                log.info("删除文档 {} 的 {} 个向量片段", docId, keys.size());
            }
        } catch (Exception e) {
            log.warn("删除文档 {} 的向量片段时发生异常: {}", docId, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════
    // 检索操作（核心）
    // ═══════════════════════════════════════════

    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        int maxResults = request.maxResults();
        double minScore = request.minScore();

        // 将查询向量序列化为字节
        byte[] queryVector = embeddingToBytes(request.queryEmbedding());

        // 构建 KNN 查询: "*=>[KNN K @embedding $BLOB AS score]"
        String query = "*=>[KNN " + maxResults + " @embedding $BLOB AS score]";
        FTSearchParams params = FTSearchParams.searchParams()
                .addParam("BLOB", queryVector)
                .dialect(2)
                .limit(0, maxResults);

        try {
            SearchResult result = jedis.ftSearch(indexName, query, params);
            List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();

            for (Document doc : result.getDocuments()) {
                // score 是 RediSearch 返回的距离值（越小越相似），转为相似度
                String scoreStr = getProperty(doc, "__score");
                double score = scoreStr != null
                        ? 1.0 - Double.parseDouble(scoreStr)
                        : 0.0;
                if (score < minScore) continue;

                // 反序列化向量
                String embeddingRaw = getProperty(doc, "embedding");
                float[] vector = embeddingRaw != null
                        ? blobToEmbedding(embeddingRaw)
                        : new float[0];

                // 构建 TextSegment（附带元数据）
                String content = getProperty(doc, "content");
                Metadata metadata = buildMetadata(doc);
                TextSegment segment = TextSegment.from(
                        content != null ? content : "", metadata);

                matches.add(new EmbeddingMatch<>(score, doc.getId(),
                        new Embedding(vector), segment));
            }

            return new EmbeddingSearchResult<>(matches);
        } catch (Exception e) {
            log.error("Redis 向量检索失败: {}", e.getMessage(), e);
            return new EmbeddingSearchResult<>(List.of());
        }
    }

    /**
     * 从 Jedis Document 中安全获取属性值
     * <p>
     * Jedis 6.x Document.getProperties() 返回 Iterable<Map.Entry<String, Object>>，
     * 需要遍历查找对应 key。
     */
    private String getProperty(Document doc, String key) {
        for (Map.Entry<String, Object> entry : doc.getProperties()) {
            if (key.equals(entry.getKey())) {
                Object val = entry.getValue();
                return val != null ? val.toString() : null;
            }
        }
        return null;
    }

    /**
     * 从 Jedis Document 的属性中构建 LangChain4j Metadata
     */
    private Metadata buildMetadata(Document doc) {
        Metadata metadata = new Metadata();
        for (Map.Entry<String, Object> entry : doc.getProperties()) {
            String key = entry.getKey();
            Object val = entry.getValue();
            if (val == null || "__score".equals(key) || "embedding".equals(key)) continue;
            metadata.put(key, val.toString());
        }
        return metadata;
    }

    // ═══════════════════════════════════════════
    // 索引管理
    // ═══════════════════════════════════════════

    /**
     * 确保 RediSearch 索引已创建，若不存在则自动创建
     * <p>
     * 使用 HNSW 算法（默认参数），COSINE 距离度量。
     */
    private void ensureIndexExists() {
        if (indexCreated) return;
        try {
            jedis.ftInfo(indexName);
            indexCreated = true;
            log.info("RediSearch 索引 {} 已存在，跳过创建", indexName);
        } catch (Exception e) {
            createIndex();
            indexCreated = true;
        }
    }

    /**
     * 创建 RediSearch 向量索引
     * <p>
     * Schema: content(TEXT) + embedding(HNSW VECTOR) + title/category/doc_id(TAG) +
     * chunk_index/created_at(NUMERIC)
     */
    private void createIndex() {
        log.info("创建 RediSearch 向量索引: {} (维度={})", indexName, dimension);
        try {
            // HNSW 向量字段属性
            Map<String, Object> vectorAttrs = new LinkedHashMap<>();
            vectorAttrs.put("TYPE", "FLOAT32");
            vectorAttrs.put("DIM", dimension);
            vectorAttrs.put("DISTANCE_METRIC", "COSINE");
            vectorAttrs.put("EF_CONSTRUCTION", 200);
            vectorAttrs.put("M", 16);

            // 构建 Schema（旧版 API，与 IndexOptions 配合使用）
            Schema schema = new Schema()
                    .addTextField("content", 1.0)
                    .addHNSWVectorField("embedding", vectorAttrs)
                    .addTagField("title")
                    .addTagField("category")
                    .addNumericField("chunk_index")
                    .addNumericField("created_at")
                    .addTagField("doc_id");

            // 索引定义：HASH 类型 + Key 前缀
            IndexDefinition definition = new IndexDefinition(IndexDefinition.Type.HASH);
            definition.setPrefixes(keyPrefix);

            // 索引选项
            IndexOptions options = IndexOptions.defaultOptions()
                    .setDefinition(definition);

            // 创建索引
            jedis.ftCreate(indexName, options, schema);
            log.info("RediSearch 索引 {} 创建成功", indexName);
        } catch (Exception e) {
            log.error("创建 RediSearch 索引失败: {}", e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════
    // 向量序列化工具方法
    // ═══════════════════════════════════════════

    /**
     * 将 float[] 向量序列化为 FLOAT32 小端字节数组（RediSearch 标准格式）
     */
    private byte[] embeddingToBytes(Embedding embedding) {
        float[] vector = embedding.vector();
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float v : vector) {
            buffer.putFloat(v);
        }
        return buffer.array();
    }

    /**
     * 将字节数组包装为 RediSearch 可接受的 BLOB 字符串
     */
    private String bytesToBlob(byte[] bytes) {
        return new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    /**
     * 将存储的 BLOB 字符串还原为 float[] 向量
     */
    private float[] blobToEmbedding(String blob) {
        byte[] bytes = blob.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        float[] vector = new float[bytes.length / 4];
        for (int i = 0; i < vector.length; i++) {
            vector[i] = buffer.getFloat();
        }
        return vector;
    }
}
