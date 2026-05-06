package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.VehicleCatalogUpdateDTO
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

    @PutMapping
    @Operation(summary = "Atualizar veículo", description = "Atualiza um veículo existente (inclui publicação e URLs de imagens)")
    fun updateVehicle(@RequestBody dto: VehicleCreateDTO): ResponseEntity<Void> {
        vehicleService.updateVehicle(dto)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PatchMapping("/{licensePlate}/catalog")
    @Operation(summary = "Atualizar vitrine pública", description = "Atualiza campos do catálogo público (published, imageUrlList, description).")
    fun updateCatalog(
        @PathVariable licensePlate: String,
        @RequestBody dto: VehicleCatalogUpdateDTO
    ): ResponseEntity<Void> {
        vehicleService.updateCatalog(licensePlate, dto)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/{licensePlate}")
    @Operation(summary = "Detalhes do veículo", description = "Retorna os dados do veículo (inclui published e imageUrlList).")
    fun getVehicle(@PathVariable licensePlate: String): ResponseEntity<VehicleResponseDTO> {
        val vehicle = vehicleService.findByLicensePlate(licensePlate) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            VehicleResponseDTO(
                licensePlate = vehicle.licensePlate,
                brand = vehicle.brand,
                modelName = vehicle.modelName,
                manufactureYear = vehicle.manufactureYear,
                modelYear = vehicle.modelYear,
                color = vehicle.color,
                kilometersDriven = vehicle.kilometersDriven,
                status = vehicle.status,
                inStock = vehicle.inStock,
                published = vehicle.published,
                description = vehicle.description,
                imageUrlList = vehicle.imageUrlList.toList()
            )
        )
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
