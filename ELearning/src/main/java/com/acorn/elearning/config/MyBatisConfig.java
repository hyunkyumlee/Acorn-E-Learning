package com.acorn.elearning.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.acorn.elearning.**.mapper")
public class MyBatisConfig {}
