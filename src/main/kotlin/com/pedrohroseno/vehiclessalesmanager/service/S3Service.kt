package com.pedrohroseno.vehiclessalesmanager.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Service
class S3Service(
    private val s3Client: S3Client,
    private val awsBucketName: String,
) {
    fun uploadFile(file: MultipartFile): String {
        require(!file.isEmpty) { "Arquivo vazio" }

        val originalName = file.originalFilename?.trim().orEmpty().ifBlank { "file" }
        val safeOriginal = originalName.substringAfterLast("\\").substringAfterLast("/")
        val key = "${UUID.randomUUID()}-$safeOriginal"

        val putRequest = PutObjectRequest.builder()
            .bucket(awsBucketName)
            .key(key)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .contentType(file.contentType ?: "application/octet-stream")
            .build()

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.bytes))

        val url = s3Client.utilities().getUrl(
            GetUrlRequest.builder()
                .bucket(awsBucketName)
                .key(key)
                .build()
        )

        return url.toString()
    }
}

