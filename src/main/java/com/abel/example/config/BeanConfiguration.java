package com.abel.example.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;


/**
 * @auther wangxu
 * @date 2025/05/16
 */
@Configuration
public class BeanConfiguration {
    @Value("${rustfs.endpoint}")
    private String endpoint;
    @Value("${rustfs.access-key}")
    private String accessKey;
    @Value("${rustfs.secret-key}")
    private String secretKey;
    @Value("${rustfs.region:us-east-1}")
    private String region;
    @Value("${rustfs.path-style:true}")
    private boolean pathStyle;


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Spring Boot中使用OpenAPI 3构建RESTful APIs").version("1.0"));
    }


    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(
                        S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build())
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 禁止把中文转义成Unicode
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        return mapper;
    }
}
