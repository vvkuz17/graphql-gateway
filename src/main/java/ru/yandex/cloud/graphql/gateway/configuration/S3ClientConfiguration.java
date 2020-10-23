package ru.yandex.cloud.graphql.gateway.configuration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfiguration {

    @Bean
    public S3Client s3Client(
            @Value("${s3.endpoint:https://storage.yandexcloud.net}") String endpoint,
            @Value("${s3.region:ru-central1}") String region,
            @Value("${s3.access.key.id:none}") String accessKeyId,
            @Value("${s3.secret.access.key:none}") String secretAccessKey) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
