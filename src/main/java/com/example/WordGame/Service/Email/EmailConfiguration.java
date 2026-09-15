package com.example.WordGame.Service.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class EmailConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.mail.provider", havingValue = "ses")
    public SesClient sesClient(
            @Value("${app.mail.ses.region:${AWS_REGION:ap-south-1}}") String region,
            @Value("${app.mail.ses.access-key:${AWS_ACCESS_KEY_ID:}}") String accessKey,
            @Value("${app.mail.ses.secret-key:${AWS_SECRET_ACCESS_KEY:}}") String secretKey) {
        AwsCredentialsProvider credentialsProvider = accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank()
                ? StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
                : DefaultCredentialsProvider.create();

        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}