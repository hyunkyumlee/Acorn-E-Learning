package com.acorn.elearning.config;

import com.acorn.elearning.auth.service.AwsLambdaInvoker;
import com.acorn.elearning.auth.service.LambdaPasswordResetMailTransport;
import com.acorn.elearning.auth.service.LambdaInvoker;
import com.acorn.elearning.auth.service.MailTransport;
import com.acorn.elearning.auth.service.SmtpPasswordResetMailTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.mail.javamail.JavaMailSender;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class MailTransportConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "knowva.mail", name = "transport", havingValue = "lambda")
    public LambdaClient lambdaClient(@Value("${knowva.mail.lambda.region}") String region) {
        return LambdaClient.builder()
                .region(Region.of(region))
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "knowva.mail", name = "transport", havingValue = "lambda")
    public LambdaInvoker awsLambdaInvoker(LambdaClient lambdaClient) {
        return new AwsLambdaInvoker(lambdaClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "knowva.mail", name = "transport", havingValue = "lambda")
    @DependsOn("lambdaClient")
    public MailTransport lambdaMailTransport(
            LambdaInvoker invoker,
            tools.jackson.databind.ObjectMapper objectMapper,
            @Value("${knowva.mail.lambda.function-name}") String functionName) {
        return new LambdaPasswordResetMailTransport(invoker, objectMapper, functionName);
    }

    @Bean
    @ConditionalOnProperty(prefix = "knowva.mail", name = "transport", havingValue = "smtp", matchIfMissing = true)
    public MailTransport smtpMailTransport(
            JavaMailSender mailSender,
            @Value("${knowva.mail.from}") String fromAddress,
            @Value("${knowva.mail.from-name}") String fromName) {
        return new SmtpPasswordResetMailTransport(mailSender, fromAddress, fromName);
    }
}
