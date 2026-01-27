package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.dtos.DashboardDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.FinancialReportDTO
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionCategory
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
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
    private val vehicleRepository: VehicleRepository,
    private val vehicleCostRepository: com.pedrohroseno.vehiclessalesmanager.repository.VehicleCostRepository,
    private val storeTransactionRepository: com.pedrohroseno.vehiclessalesmanager.repository.StoreTransactionRepository
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

        // Calcular totais apenas de transações ATIVAS
        val totalVendas = saleRepository.sumSalePriceByDateRangeAndStatus(start, end, TransactionStatus.ACTIVE)
        val totalCompras = purchaseRepository.sumPurchasePriceByDateRangeAndStatus(start, end, TransactionStatus.ACTIVE)
        val totalTrocas = exchangeRepository.sumDiferencaValorByDateRangeAndStatus(start, end, TransactionStatus.ACTIVE)
        val totalCustos = vehicleCostRepository.sumCostsByDateRange(start, end)
        // Saldo = Vendas + Trocas - Compras - Custos Adicionais
        val saldoGeral = totalVendas - totalCompras + totalTrocas - totalCustos

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        
        return FinancialReportDTO(
            saldoGeral = saldoGeral,
            totalVendas = totalVendas,
            totalCompras = totalCompras,
            totalTrocas = totalTrocas,
            totalCustos = totalCustos,
            startDate = dateFormat.format(start),
            endDate = dateFormat.format(end)
        )
    }

    fun getDashboard(): DashboardDTO {
        // Calcular totais apenas de transações ATIVAS (sem filtro de data)
        val totalVendas = saleRepository.sumSalePriceByStatus(TransactionStatus.ACTIVE)
        val totalCompras = purchaseRepository.sumPurchasePriceByStatus(TransactionStatus.ACTIVE)
        val totalTrocas = exchangeRepository.sumDiferencaValorByStatus(TransactionStatus.ACTIVE)
        val totalCustos = vehicleCostRepository.sumAllCosts()
        
        // Calcular despesas operacionais (OPERACIONAL, ADMINISTRATIVO, MARKETING, INFRAESTRUTURA)
        val despesasOperacionais = storeTransactionRepository.sumByStatusTypeAndCategories(
            TransactionStatus.ACTIVE,
            TransactionTypeEnum.EXIT,
            listOf(
                TransactionCategory.OPERACIONAL,
                TransactionCategory.ADMINISTRATIVO,
                TransactionCategory.MARKETING,
                TransactionCategory.INFRAESTRUTURA
            )
        )
        
        // Lucro Bruto = Vendas - Compras + Trocas - Custos de Veículos
        val lucroBruto = totalVendas - totalCompras + totalTrocas - totalCustos
        
        // Lucro Líquido = Lucro Bruto - Despesas Operacionais
        val lucroLiquido = lucroBruto - despesasOperacionais
        
        val quantidadeMotosEstoque = vehicleRepository.countByStatus(VehicleStatus.DISPONIVEL)

        return DashboardDTO(
            totalVendas = totalVendas,
            totalCompras = totalCompras,
            totalTrocas = totalTrocas,
            totalCustos = totalCustos,
            despesasOperacionais = despesasOperacionais,
            lucroBruto = lucroBruto,
            lucroLiquido = lucroLiquido,
            saldoLiquido = lucroLiquido, // Mantido para compatibilidade
            quantidadeMotosEstoque = quantidadeMotosEstoque
        )
    }
}
