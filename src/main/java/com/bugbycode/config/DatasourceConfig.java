package com.bugbycode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class DatasourceConfig {
    
    @Value("${spring.datasource.mongodb.url}")
    private String connStr;

    @Value("${spring.datasource.mongodb.database}")
    private String database;
    
    @Value("${spring.datasource.mongodb.maxSize}")
    private int maxSize;
    
    @Value("${spring.datasource.mongodb.minSize}")
    private int minSize;
    
    @Value("${spring.datasource.mongodb.maxConnecting}")
    private int maxConnecting;

    @Bean
    public MongoClient mongoClient(){
    	
    	ConnectionString connString = new ConnectionString(connStr);
    	
    	MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(maxSize)
                        .minSize(minSize)
                        .maxConnecting(maxConnecting)
                )
                .build();
    	
        return MongoClients.create(settings);
    }

    @Bean
    public MongoOperations mongoTemplate(MongoClient mongoClient){
        return new MongoTemplate(mongoClient,database);
    }
}
