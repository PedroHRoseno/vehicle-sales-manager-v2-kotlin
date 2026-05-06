package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.PublicVehicleDTO
import com.pedrohroseno.vehiclessalesmanager.service.VehicleService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/vehicles")
class PublicVehicleController(
    private val vehicleService: VehicleService
) {
    @GetMapping
    @Operation(summary = "Listar veículos disponíveis (público)", description = "Retorna apenas veículos com status DISPONIVEL e sem dados sensíveis.")
    fun getAvailableVehiclesPublic(): ResponseEntity<List<PublicVehicleDTO>> {
        return ResponseEntity.ok(vehicleService.getPublicAvailableVehicles())
    }
}

