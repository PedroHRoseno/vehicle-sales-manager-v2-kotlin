package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.FinancialMovementDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
import com.pedrohroseno.vehiclessalesmanager.service.FinancialMovementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Date

@RestController
@RequestMapping("/financial/movements")
@Tag(name = "Movimentações Financeiras", description = "API para visualização unificada de todas as movimentações financeiras")
@CrossOrigin(origins = ["http://localhost:3000"])
class FinancialMovementController(
    private val financialMovementService: FinancialMovementService
) {
    @GetMapping
    @Operation(
        summary = "Listar movimentações",
        description = "Retorna uma lista paginada unificada de todas as movimentações financeiras (Vendas, Compras, Trocas, Custos de Veículos e Transações da Loja)"
    )
    fun getAllMovements(
        @Parameter(description = "Data inicial (opcional)")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: Date?,
        @Parameter(description = "Data final (opcional)")
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: Date?,
        @Parameter(description = "Tipo de transação: ENTRY ou EXIT (opcional)")
        @RequestParam(required = false) type: TransactionTypeEnum?,
        @Parameter(description = "Categoria (apenas para transações da loja, opcional)")
        @RequestParam(required = false) category: String?,
        @PageableDefault(size = 20, sort = ["date,desc"]) pageable: Pageable
    ): ResponseEntity<Page<FinancialMovementDTO>> {
        return ResponseEntity.ok(
            financialMovementService.getAllMovements(pageable, startDate, endDate, type, category)
        )
    }
}
