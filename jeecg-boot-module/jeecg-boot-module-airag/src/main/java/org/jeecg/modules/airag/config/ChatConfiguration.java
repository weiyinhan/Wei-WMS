package org.jeecg.modules.airag.config;


import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class ChatConfiguration {

//    // 注意参数中的model就是使用的模型，这里用了Ollama
//    @Bean
//    public ChatClient chatClientOllama(OllamaChatModel model) {
//        return ChatClient.builder(model) // 创建ChatClient工厂
//                .build(); // 构建ChatClient实例
//    }

    @Bean
    public ChatClient chatClientOpenAi(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())//记录日志
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())//会话记忆
                .build();
    }
    @Bean
    public VectorStore pgVectorStore(
            @Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("openAiEmbeddingModel") OpenAiEmbeddingModel openAiEmbeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, openAiEmbeddingModel)
                .dimensions(768)  // 与嵌入模型维度对齐
                .distanceType(COSINE_DISTANCE)  // 余弦相似度计算
                .schemaName("public")// 指定表所属的schema
                .vectorTableName("vector_store")// 存储向量的表名
                .indexType(HNSW)  // 高效近似最近邻搜索
                .initializeSchema(false)  // 自动初始化表结构
                .idType(PgVectorStore.PgIdType.UUID)
                .build();
    }

    @Bean("vectorJdbcTemplate")
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        DataSource postgresql = ds.getDataSource("postgresql");
        return new JdbcTemplate(postgresql);
    }
}