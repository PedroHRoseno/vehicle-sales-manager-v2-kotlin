package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.dtos.FinancialMovementDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.MovementOrigin
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionTypeEnum
import com.pedrohroseno.vehiclessalesmanager.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.*

@Service
class FinancialMovementService(
    private val saleRepository: SaleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val exchangeRepository: ExchangeRepository,
    private val vehicleCostRepository: VehicleCostRepository,
    private val storeTransactionRepository: StoreTransactionRepository
) {
    fun getAllMovements(
        pageable: Pageable,
        startDate: Date? = null,
        endDate: Date? = null,
        type: TransactionTypeEnum? = null,
        category: String? = null
    ): Page<FinancialMovementDTO> {
        val allMovements = mutableListOf<FinancialMovementDTO>()

        // 1. Vendas (ENTRY)
        val sales = saleRepository.findAllByDeletedFalseOrderBySaleDateDesc(Pageable.unpaged())
            .filter { 
                if (startDate != null && endDate != null) {
                    it.saleDate >= startDate && it.saleDate <= endDate
                } else {
                    true
                }
            }
        
        sales.forEach { sale ->
            if (type == null || type == TransactionTypeEnum.ENTRY) {
                allMovements.add(
                    FinancialMovementDTO(
                        id = sale.id ?: 0L,
                        date = sale.saleDate,
                        description = "Venda: ${sale.vehicle.brand.name} ${sale.vehicle.modelName} - ${sale.partner.name}",
                        value = sale.salePrice,
                        type = TransactionTypeEnum.ENTRY,
                        origin = MovementOrigin.VEHICLE,
                        status = sale.status,
                        vehicleLicensePlate = sale.vehicle.licensePlate,
                        transactionType = "SALE"
                    )
                )
            }
        }

        // 2. Compras (EXIT)
        val purchases = if (startDate != null && endDate != null) {
            purchaseRepository.findAllByDeletedFalseOrderByPurchaseDateDesc(Pageable.unpaged())
                .filter { it.purchaseDate >= startDate && it.purchaseDate <= endDate }
        } else {
            purchaseRepository.findAllByDeletedFalseOrderByPurchaseDateDesc(Pageable.unpaged()).toList()
        }
        
        purchases.forEach { purchase ->
            if (type == null || type == TransactionTypeEnum.EXIT) {
                allMovements.add(
                    FinancialMovementDTO(
                        id = purchase.id ?: 0L,
                        date = purchase.purchaseDate,
                        description = "Compra: ${purchase.vehicle.brand.name} ${purchase.vehicle.modelName} - ${purchase.partner.name}",
                        value = purchase.purchasePrice,
                        type = TransactionTypeEnum.EXIT,
                        origin = MovementOrigin.VEHICLE,
                        status = purchase.status,
                        vehicleLicensePlate = purchase.vehicle.licensePlate,
                        transactionType = "PURCHASE"
                    )
                )
            }
        }

        // 3. Trocas (pode ser ENTRY ou EXIT dependendo do sinal da diferença)
        val exchanges = exchangeRepository.findAllByDeletedFalseOrderByExchangeDateDesc(Pageable.unpaged())
            .filter { 
                if (startDate != null && endDate != null) {
                    it.exchangeDate >= startDate && it.exchangeDate <= endDate
                } else {
                    true
                }
            }
        
        exchanges.forEach { exchange ->
            val isEntry = exchange.diferencaValor >= 0
            if (type == null || (isEntry && type == TransactionTypeEnum.ENTRY) || (!isEntry && type == TransactionTypeEnum.EXIT)) {
                allMovements.add(
                    FinancialMovementDTO(
                        id = exchange.id ?: 0L,
                        date = exchange.exchangeDate,
                        description = "Troca: Entrada ${exchange.vehicleEntrada.licensePlate} / Saída ${exchange.vehicleSaida.licensePlate} - ${exchange.partner.name}",
                        value = Math.abs(exchange.diferencaValor),
                        type = if (isEntry) TransactionTypeEnum.ENTRY else TransactionTypeEnum.EXIT,
                        origin = MovementOrigin.VEHICLE,
                        status = exchange.status,
                        vehicleLicensePlate = exchange.vehicleEntrada.licensePlate,
                        transactionType = "EXCHANGE"
                    )
                )
            }
        }

        // 4. Custos de Veículos (EXIT)
        val vehicleCosts = vehicleCostRepository.findAll()
            .filter { !it.deleted }
            .filter { cost ->
                if (startDate != null && endDate != null) {
                    cost.costDate >= startDate && cost.costDate <= endDate
                } else {
                    true
                }
            }
        
        vehicleCosts.forEach { cost ->
            if (type == null || type == TransactionTypeEnum.EXIT) {
                allMovements.add(
                    FinancialMovementDTO(
                        id = cost.id ?: 0L,
                        date = cost.costDate,
                        description = "Custo: ${cost.description} - Veículo ${cost.vehicle.licensePlate}",
                        value = cost.cost,
                        type = TransactionTypeEnum.EXIT,
                        origin = MovementOrigin.VEHICLE,
                        status = TransactionStatus.ACTIVE,
                        vehicleLicensePlate = cost.vehicle.licensePlate,
                        transactionType = "COST"
                    )
                )
            }
        }

        // 5. Transações da Loja
        val storeTransactions = if (startDate != null && endDate != null) {
            storeTransactionRepository.findAllByDateRangeAndDeletedFalse(startDate, endDate, Pageable.unpaged())
        } else {
            storeTransactionRepository.findAllByDeletedFalseOrderByDateDesc(Pageable.unpaged())
        }.toList()
        
        storeTransactions.forEach { transaction ->
            if (type == null || transaction.type == type) {
                if (category == null || transaction.category.name == category) {
                    allMovements.add(
                        FinancialMovementDTO(
                            id = transaction.id ?: 0L,
                            date = transaction.date,
                            description = transaction.description,
                            value = transaction.value,
                            type = transaction.type,
                            origin = MovementOrigin.STORE,
                            status = transaction.status,
                            category = transaction.category.name,
                            transactionType = "STORE"
                        )
                    )
                }
            }
        }

        // Ordenar por data (mais recente primeiro) e aplicar paginação manual
        allMovements.sortByDescending { it.date }
        
        val start = pageable.pageNumber * pageable.pageSize
        val end = minOf(start + pageable.pageSize, allMovements.size)
        val paginatedMovements = allMovements.subList(start, end)

        return org.springframework.data.domain.PageImpl(
            paginatedMovements,
            pageable,
            allMovements.size.toLong()
        )
    }
}
