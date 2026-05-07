package com.pedrohroseno.vehiclessalesmanager.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3Config(
    @Value("\${AWS_ACCESS_KEY:}") private val accessKey: String,
    @Value("\${AWS_SECRET_KEY:}") private val secretKey: String,
    @Value("\${AWS_REGION:}") private val region: String,
    @Value("\${AWS_BUCKET_NAME:}") private val bucketName: String,
) {
    @Bean
    fun s3Client(): S3Client {
        require(accessKey.isNotBlank()) { "AWS_ACCESS_KEY não configurado" }
        require(secretKey.isNotBlank()) { "AWS_SECRET_KEY não configurado" }
        require(region.isNotBlank()) { "AWS_REGION não configurado" }
        require(bucketName.isNotBlank()) { "AWS_BUCKET_NAME não configurado" }

        val creds = AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())
        return S3Client.builder()
            .region(Region.of(region.trim()))
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .build()
    }

    @Bean
    fun awsBucketName(): String = bucketName.trim()
}

