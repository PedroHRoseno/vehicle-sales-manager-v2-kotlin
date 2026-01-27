package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleHistoryDTO
import com.pedrohroseno.vehiclessalesmanager.service.VehicleService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Veículos", description = "API para gerenciamento de veículos")
class VehicleController(
    private val vehicleService: VehicleService
) {
    @GetMapping
    @Operation(summary = "Listar todos os veículos", description = "Retorna uma lista paginada de todos os veículos")
    fun getAllVehicles(
        @PageableDefault(size = 20, sort = ["licensePlate"]) pageable: Pageable
    ): ResponseEntity<Page<VehicleResponseDTO>> {
        return ResponseEntity.ok(vehicleService.getAllVehicles(pageable))
    }

    @GetMapping("/available")
    @Operation(summary = "Listar veículos disponíveis", description = "Retorna apenas veículos com status DISPONIVEL")
    fun getAvailableVehicles(
        @PageableDefault(size = 20, sort = ["licensePlate"]) pageable: Pageable
    ): ResponseEntity<Page<VehicleResponseDTO>> {
        return ResponseEntity.ok(vehicleService.getAvailableVehicles(pageable))
    }

    @PostMapping
    @Operation(summary = "Criar veículo", description = "Cadastra um novo veículo no sistema")
    fun createVehicle(@RequestBody dto: VehicleCreateDTO): ResponseEntity<Void> {
        vehicleService.createVehicle(dto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/{licensePlate}/history")
    @Operation(
        summary = "Histórico completo do veículo",
        description = "Retorna o histórico completo do veículo incluindo todas as compras, vendas e trocas relacionadas"
    )
    fun getVehicleHistory(
        @Parameter(description = "Placa do veículo")
        @PathVariable licensePlate: String
    ): ResponseEntity<VehicleHistoryDTO> {
        return try {
            val history = vehicleService.getVehicleHistory(licensePlate)
            ResponseEntity.ok(history)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}
