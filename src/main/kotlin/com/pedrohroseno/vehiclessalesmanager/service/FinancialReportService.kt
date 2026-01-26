package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.dtos.DashboardDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.FinancialReportDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import com.pedrohroseno.vehiclessalesmanager.repository.ExchangeRepository
import com.pedrohroseno.vehiclessalesmanager.repository.PurchaseRepository
import com.pedrohroseno.vehiclessalesmanager.repository.SaleRepository
import com.pedrohroseno.vehiclessalesmanager.repository.VehicleRepository
import org.springframework.stereotype.Service
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

@Service
class FinancialReportService(
    private val saleRepository: SaleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val exchangeRepository: ExchangeRepository,
    private val vehicleRepository: VehicleRepository
) {
    fun getFinancialReport(startDate: Date? = null, endDate: Date? = null): FinancialReportDTO {
        // Se não foram fornecidas datas, usa o padrão de 30 dias atrás até hoje
        val end = endDate ?: Date()
        val start = startDate ?: run {
            val calendar = Calendar.getInstance()
            calendar.time = end
            calendar.add(Calendar.DAY_OF_MONTH, -30)
            calendar.time
        }

        // Calcular totais
        val totalVendas = saleRepository.sumSalePriceByDateRange(start, end)
        val totalCompras = purchaseRepository.sumPurchasePriceByDateRange(start, end)
        val totalTrocas = exchangeRepository.sumDiferencaValorByDateRange(start, end)
        val saldoGeral = totalVendas - totalCompras + totalTrocas

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        
        return FinancialReportDTO(
            saldoGeral = saldoGeral,
            totalVendas = totalVendas,
            totalCompras = totalCompras,
            totalTrocas = totalTrocas,
            startDate = dateFormat.format(start),
            endDate = dateFormat.format(end)
        )
    }

    fun getDashboard(): DashboardDTO {
        // Calcular totais de todas as vendas, compras e trocas (sem filtro de data)
        val totalVendas = saleRepository.findAll().sumOf { it.salePrice }
        val totalCompras = purchaseRepository.findAll().sumOf { it.purchasePrice }
        val totalTrocas = exchangeRepository.sumDiferencaValorAll()
        val saldoLiquido = totalVendas - totalCompras + totalTrocas
        val quantidadeMotosEstoque = vehicleRepository.countByStatus(VehicleStatus.DISPONIVEL)

        return DashboardDTO(
            totalVendas = totalVendas,
            totalCompras = totalCompras,
            totalTrocas = totalTrocas,
            saldoLiquido = saldoLiquido,
            quantidadeMotosEstoque = quantidadeMotosEstoque
        )
    }
}
