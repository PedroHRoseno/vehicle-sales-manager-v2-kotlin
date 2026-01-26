package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleResponseDTO
import com.pedrohroseno.vehiclessalesmanager.service.SaleService
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
@RequestMapping("/sales")
@Tag(name = "Vendas", description = "API para gerenciamento de vendas")
class SaleController(
    private val saleService: SaleService
) {
    @GetMapping
    @Operation(summary = "Listar vendas", description = "Retorna uma lista paginada das últimas vendas realizadas. Suporta busca por placa do veículo, CPF ou nome do parceiro através do parâmetro 'search'.")
    fun getAllSales(
        @Parameter(description = "Termo de busca (placa do veículo, CPF ou nome do parceiro)", required = false)
        @RequestParam(required = false) search: String?,
        @PageableDefault(size = 20, sort = ["saleDate,desc"]) pageable: Pageable
    ): ResponseEntity<Page<SaleResponseDTO>> {
        return ResponseEntity.ok(saleService.getAllSales(pageable, search))
    }

    @PostMapping
    @Operation(summary = "Registrar venda", description = "Registra uma nova venda e atualiza o status do veículo para VENDIDO")
    fun createSale(@RequestBody dto: SaleCreateDTO): ResponseEntity<Void> {
        saleService.createSale(dto)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir venda", description = "Exclui uma venda (soft delete) e reverte o status do veículo para DISPONIVEL")
    fun deleteSale(
        @Parameter(description = "ID da venda a ser excluída")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        saleService.deleteSale(id)
        return ResponseEntity.noContent().build()
    }
}
