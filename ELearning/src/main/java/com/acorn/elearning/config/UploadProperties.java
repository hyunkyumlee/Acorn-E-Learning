package com.acorn.elearning.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "knowva.upload")
public record UploadProperties(String basePath) {}
