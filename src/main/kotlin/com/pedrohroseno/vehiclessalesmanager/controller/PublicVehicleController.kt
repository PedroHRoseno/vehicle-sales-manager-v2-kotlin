package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.PublicVehicleDTO
import com.pedrohroseno.vehiclessalesmanager.service.VehicleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/public/vehicles")
@Tag(name = "Catálogo público", description = "Veículos disponíveis e publicados (sem autenticação)")
class PublicVehicleController(
    private val vehicleService: VehicleService
) {
    @GetMapping
    @Operation(
        summary = "Listar motos do catálogo público",
        description = "Retorna motos com status DISPONIVEL e published=true. Query params opcionais não quebram clientes existentes. Consumido pelo almotos-ai (MCP)."
    )
    fun getPublicVehicles(
        @Parameter(description = "Marca (nome do enum, ex.: HONDA)", required = false)
        @RequestParam(required = false) brand: String?,
        @Parameter(description = "Quilometragem máxima", required = false)
        @RequestParam(required = false) maxKm: Int?,
        @Parameter(description = "Ano modelo mínimo", required = false)
        @RequestParam(required = false) yearMin: Int?,
    ): ResponseEntity<List<PublicVehicleDTO>> {
        val body = vehicleService.getPublicAvailableVehicles(brand, maxKm, yearMin)
        return ResponseEntity.ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS).cachePublic())
            .body(body)
    }
}
