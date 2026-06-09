package com.elderlycare.ai.config;

import com.elderlycare.ai.service.RedisEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * RAG 增强检索生成配置
 * <p>
 * 装配 RAG 核心 Bean：Jedis 客户端、向量存储、文档切分器。
 * Jedis 用于连接 Redis Stack 并执行 RediSearch 向量检索命令。
 *
 * @author 郑
 */
@Slf4j
@Configuration
public class RagConfig {

    /** Redis 主机 */
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    /** Redis 端口 */
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /** Redis 密码（可选） */
    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /** RediSearch 索引名称 */
    @Value("${elderly.rag.embedding-store.index-name:idx_rag_docs}")
    private String indexName;

    /** Redis Key 前缀 */
    @Value("${elderly.rag.embedding-store.prefix:rag:doc:}")
    private String keyPrefix;

    /** 向量维度（必须与嵌入模型输出一致） */
    @Value("${elderly.rag.embedding-store.dimension:1536}")
    private int dimension;

    /**
     * 创建 Jedis 客户端 Bean（连接 Redis Stack）
     * <p>
     * JedisPooled 是线程安全的连接池，用于执行 RediSearch 向量检索命令。
     * 与现有的 Lettuce-based RedisTemplate 共存，各自独立管理连接。
     */
    @Bean
    public JedisPooled jedisPooled() {
        log.info("初始化 Jedis 连接池: {}:{}", redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            return new JedisPooled(redisHost, redisPort, "default", redisPassword);
        }
        return new JedisPooled(redisHost, redisPort);
    }

    /**
     * 创建 Redis 向量存储 Bean
     * <p>
     * 实现 LangChain4j 的 EmbeddingStore 接口，封装 Redis Stack 的向量读写和 KNN 检索。
     */
    @Bean
    public RedisEmbeddingStore redisEmbeddingStore(JedisPooled jedis) {
        log.info("初始化 Redis 向量存储: indexName={}, prefix={}, dimension={}",
                indexName, keyPrefix, dimension);
        return new RedisEmbeddingStore(jedis, indexName, keyPrefix, dimension);
    }
}
