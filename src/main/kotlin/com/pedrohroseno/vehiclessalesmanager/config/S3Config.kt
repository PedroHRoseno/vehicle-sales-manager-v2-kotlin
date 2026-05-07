package com.pedrohroseno.vehiclessalesmanager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3Config(
    @Value("\${AWS_REGION:us-east-1}") private val region: String,
    @Value("\${AWS_BUCKET_NAME:}") private val bucketName: String,
) {
    @Bean
    fun s3Client(): S3Client {
        return S3Client.builder()
            .region(Region.of(region.trim()))
            // Lê credenciais automaticamente de variáveis de ambiente / metadata:
            // AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY / AWS_SESSION_TOKEN (se houver)
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()
    }

    @Bean
    fun awsBucketName(): String = bucketName.trim()
}

