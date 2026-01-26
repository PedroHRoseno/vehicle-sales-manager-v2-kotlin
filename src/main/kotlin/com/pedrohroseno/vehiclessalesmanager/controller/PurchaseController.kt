package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.PurchaseResponseDTO
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir compra", description = "Exclui uma compra (soft delete) e remove o veículo do estoque")
    fun deletePurchase(
        @Parameter(description = "ID da compra a ser excluída")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        purchaseService.deletePurchase(id)
        return ResponseEntity.noContent().build()
    }
}
