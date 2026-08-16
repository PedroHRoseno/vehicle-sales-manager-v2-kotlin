package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.PublicVehicleDTO
import com.pedrohroseno.vehiclessalesmanager.service.VehicleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/vehicles")
@Tag(name = "Catálogo público", description = "Veículos disponíveis e publicados (sem autenticação)")
class PublicVehicleController(
    private val vehicleService: VehicleService
) {
    @GetMapping
    @Operation(
        summary = "Listar motos do catálogo público",
        description = "Retorna motos com status DISPONIVEL e published=true. Usado pelo bot WhatsApp."
    )
    fun getPublicVehicles(): ResponseEntity<List<PublicVehicleDTO>> {
        return ResponseEntity.ok(vehicleService.getPublicAvailableVehicles())
    }
}
