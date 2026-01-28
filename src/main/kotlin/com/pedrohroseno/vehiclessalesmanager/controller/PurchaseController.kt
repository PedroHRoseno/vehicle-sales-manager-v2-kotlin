package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.Purchase
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseUpdateDTO
import com.pedrohroseno.vehiclessalesmanager.service.PurchaseService
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
@RequestMapping("/purchases")
@Tag(name = "Compras", description = "API para gerenciamento de compras")
@CrossOrigin(origins = ["http://localhost:3000"])
class PurchaseController(
    private val purchaseService: PurchaseService
) {
    @GetMapping
    @Operation(summary = "Listar compras", description = "Retorna uma lista paginada das últimas compras realizadas. Suporta busca por placa do veículo, CPF ou nome do parceiro através do parâmetro 'search'.")
    fun getAllPurchases(
        @Parameter(description = "Termo de busca (placa do veículo, CPF ou nome do parceiro)", required = false)
        @RequestParam(required = false) search: String?,
        @PageableDefault(size = 20, sort = ["purchaseDate,desc"]) pageable: Pageable
    ): ResponseEntity<Page<PurchaseResponseDTO>> {
        return ResponseEntity.ok(purchaseService.getAllPurchases(pageable, search))
    }

    @PostMapping
    @Operation(summary = "Registrar compra", description = "Registra uma nova compra e cadastra o veículo automaticamente como DISPONIVEL")
    fun createPurchase(@RequestBody dto: PurchaseCreateDTO): ResponseEntity<Void> {
        purchaseService.createPurchase(dto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Editar compra",
        description = "Edita uma compra existente. Permite alterar apenas preço e data. Não permite alterar veículo ou parceiro."
    )
    fun updatePurchase(
        @Parameter(description = "ID da compra a ser editada")
        @PathVariable id: Long,
        @RequestBody dto: PurchaseUpdateDTO
    ): ResponseEntity<PurchaseResponseDTO> {
        return try {
            val purchase = purchaseService.updatePurchase(id, dto, null)
            ResponseEntity.ok(purchase.toResponseDTO())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar compra",
        description = "Cancela uma compra. Transações canceladas não aparecem nos cálculos financeiros."
    )
    fun cancelPurchase(
        @Parameter(description = "ID da compra a ser cancelada")
        @PathVariable id: Long
    ): ResponseEntity<PurchaseResponseDTO> {
        return try {
            val purchase = purchaseService.cancelPurchase(id, null)
            ResponseEntity.ok(purchase.toResponseDTO())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Excluir compra",
        description = "Exclui uma compra (soft delete). Se deleteVehicle=true, também remove o veículo do estoque."
    )
    fun deletePurchase(
        @Parameter(description = "ID da compra a ser excluída")
        @PathVariable id: Long,
        @Parameter(description = "Se true, também deleta o veículo. Se false, apenas exclui o registro da compra.")
        @RequestParam(required = false, defaultValue = "false") deleteVehicle: Boolean
    ): ResponseEntity<Void> {
        return try {
            purchaseService.deletePurchase(id, deleteVehicle)
            ResponseEntity.noContent().build()
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.CONFLICT).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
    
    private fun Purchase.toResponseDTO(): PurchaseResponseDTO {
        return PurchaseResponseDTO(
            id = this.id ?: throw IllegalStateException("Purchase ID não pode ser nulo"),
            vehicleLicensePlate = this.vehicle.licensePlate,
            vehicleBrand = this.vehicle.brand.name,
            vehicleModel = this.vehicle.modelName,
            partnerDocument = this.partner.document,
            partnerName = this.partner.name,
            purchasePrice = this.purchasePrice,
            purchaseDate = this.purchaseDate,
            status = this.status
        )
    }
}
