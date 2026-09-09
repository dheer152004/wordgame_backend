package com.example.WordGame.Service.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class EmailConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.mail.provider", havingValue = "ses")
    public SesClient sesClient(@Value("${app.mail.ses.region:${AWS_REGION:ap-south-1}}") String region) {
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}