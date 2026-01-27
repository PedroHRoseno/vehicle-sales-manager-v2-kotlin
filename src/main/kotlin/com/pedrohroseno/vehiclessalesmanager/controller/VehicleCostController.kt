package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCostCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCostResponseDTO
import com.pedrohroseno.vehiclessalesmanager.service.VehicleCostService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/vehicles/{licensePlate}/costs")
@Tag(name = "Custos de Veículos", description = "API para gerenciamento de custos adicionais de veículos")
@CrossOrigin(origins = ["http://localhost:3000"])
class VehicleCostController(
    private val vehicleCostService: VehicleCostService
) {
    @GetMapping
    @Operation(summary = "Listar custos", description = "Retorna todos os custos adicionais de um veículo")
    fun getCostsByVehicle(
        @Parameter(description = "Placa do veículo")
        @PathVariable licensePlate: String
    ): ResponseEntity<List<VehicleCostResponseDTO>> {
        val costs = vehicleCostService.getCostsByVehicle(licensePlate)
        return ResponseEntity.ok(costs)
    }

    @PostMapping
    @Operation(summary = "Adicionar custo", description = "Adiciona um custo adicional ao veículo (manutenção, documentação, etc.)")
    fun createCost(
        @Parameter(description = "Placa do veículo")
        @PathVariable licensePlate: String,
        @RequestBody dto: VehicleCostCreateDTO
    ): ResponseEntity<VehicleCostResponseDTO> {
        val cost = vehicleCostService.createCost(dto.copy(vehicleLicensePlate = licensePlate))
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd")
        val response = VehicleCostResponseDTO(
            id = cost.id ?: throw IllegalStateException("VehicleCost ID não pode ser nulo"),
            vehicleLicensePlate = cost.vehicle.licensePlate,
            cost = cost.cost,
            description = cost.description,
            costDate = dateFormat.format(cost.costDate)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir custo", description = "Remove um custo adicional do veículo")
    fun deleteCost(
        @Parameter(description = "ID do custo a ser excluído")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        vehicleCostService.deleteCost(id)
        return ResponseEntity.noContent().build()
    }
}
