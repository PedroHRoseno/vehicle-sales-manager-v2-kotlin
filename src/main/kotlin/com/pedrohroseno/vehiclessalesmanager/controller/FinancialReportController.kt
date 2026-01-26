package com.pedrohroseno.vehiclessalesmanager.controller

import com.pedrohroseno.vehiclessalesmanager.model.dtos.DashboardDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.FinancialReportDTO
import com.pedrohroseno.vehiclessalesmanager.service.FinancialReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.Date

@RestController
@RequestMapping("/reports")
@Tag(name = "Relatórios", description = "API para relatórios financeiros e dashboard")
class FinancialReportController(
    private val financialReportService: FinancialReportService
) {
    @GetMapping("/dashboard")
    @Operation(
        summary = "Dashboard consolidado",
        description = "Retorna um objeto DTO consolidado contendo: totalVendas, totalCompras, saldoLiquido e quantidadeMotosEstoque"
    )
    fun getDashboard(): ResponseEntity<DashboardDTO> {
        return ResponseEntity.ok(financialReportService.getDashboard())
    }

    @GetMapping("/financial")
    @Operation(
        summary = "Relatório financeiro",
        description = "Retorna o saldo geral (Total de Vendas - Total de Compras). Filtro padrão: últimos 30 dias"
    )
    fun getFinancialReport(
        @Parameter(description = "Data inicial (formato: yyyy-MM-dd)", required = false)
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        startDate: Date?,
        
        @Parameter(description = "Data final (formato: yyyy-MM-dd)", required = false)
        @RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        endDate: Date?
    ): ResponseEntity<FinancialReportDTO> {
        val report = financialReportService.getFinancialReport(startDate, endDate)
        return ResponseEntity.ok(report)
    }
}
