package com.coinkline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class DatasourceConfig {
    
    @Value("${spring.datasource.mongodb.url}")
    private String ConnectionString;

    @Value("${spring.datasource.mongodb.database}")
    private String database;

    @Bean
    public MongoClient mongoClient(){
        return MongoClients.create(ConnectionString + "/" + database);
    }

    @Bean
    public MongoOperations mongoTemplate(MongoClient mongoClient){
        return new MongoTemplate(mongoClient,database);
    }
}
