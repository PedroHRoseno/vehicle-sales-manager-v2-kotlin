package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.Sale
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleCreateDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleResponseDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.SaleUpdateDTO
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
@CrossOrigin(origins = ["http://localhost:3000"])
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

    @PutMapping("/{id}")
    @Operation(
        summary = "Editar venda",
        description = "Edita uma venda existente. Permite alterar apenas preço e data. Não permite alterar veículo ou parceiro."
    )
    fun updateSale(
        @Parameter(description = "ID da venda a ser editada")
        @PathVariable id: Long,
        @RequestBody dto: SaleUpdateDTO
    ): ResponseEntity<SaleResponseDTO> {
        return try {
            val sale = saleService.updateSale(id, dto, null)
            ResponseEntity.ok(sale.toResponseDTO())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(
        summary = "Cancelar venda",
        description = "Cancela uma venda. Transações canceladas não aparecem nos cálculos financeiros. O veículo é revertido para DISPONIVEL."
    )
    fun cancelSale(
        @Parameter(description = "ID da venda a ser cancelada")
        @PathVariable id: Long
    ): ResponseEntity<SaleResponseDTO> {
        return try {
            val sale = saleService.cancelSale(id, null)
            ResponseEntity.ok(sale.toResponseDTO())
        } catch (e: IllegalStateException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
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
    
    private fun Sale.toResponseDTO(): SaleResponseDTO {
        return SaleResponseDTO(
            id = this.id ?: throw IllegalStateException("Sale ID não pode ser nulo"),
            vehicleLicensePlate = this.vehicle.licensePlate,
            vehicleBrand = this.vehicle.brand.name,
            vehicleModel = this.vehicle.modelName,
            partnerCpf = this.partner.cpf,
            partnerName = this.partner.name,
            salePrice = this.salePrice,
            saleDate = this.saleDate,
            status = this.status
        )
    }
}
