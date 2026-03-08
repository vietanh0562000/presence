package com.presence.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables @CreatedDate and @LastModifiedDate on MongoDB documents.
 * CORS is configured in SecurityConfig.
 */
@Configuration
@EnableMongoAuditing
public class AppConfig {}
