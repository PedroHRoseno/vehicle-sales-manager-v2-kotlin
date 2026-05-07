package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.service.S3Service
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

data class VehicleImageUploadResponseDTO(
    val url: String,
)

@RestController
@RequestMapping("/api/vehicles/images")
class VehicleImageController(
    private val s3Service: S3Service,
) {
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload de imagem para S3", description = "Recebe um MultipartFile e retorna a URL pública.")
    fun upload(@RequestParam("file") file: MultipartFile): ResponseEntity<VehicleImageUploadResponseDTO> {
        val url = s3Service.uploadFile(file)
        return ResponseEntity.ok(VehicleImageUploadResponseDTO(url = url))
    }
}

