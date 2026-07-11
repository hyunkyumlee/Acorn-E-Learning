package com.acorn.elearning.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({UploadProperties.class, OAuthProperties.class, KakaoPayProperties.class})
public class PropertiesConfig {}
