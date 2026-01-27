package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Exchange
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface ExchangeRepository : JpaRepository<Exchange, Long> {
    fun findAllByOrderByExchangeDateDesc(pageable: Pageable): Page<Exchange>
    
    @Query("SELECT e FROM Exchange e WHERE e.deleted = false ORDER BY e.exchangeDate DESC")
    fun findAllByDeletedFalseOrderByExchangeDateDesc(pageable: Pageable): Page<Exchange>
    
    fun findByIdAndDeletedFalse(id: Long): Exchange?
    
    @Query("SELECT COALESCE(SUM(e.diferencaValor), 0.0) FROM Exchange e WHERE e.deleted = false AND e.exchangeDate BETWEEN :startDate AND :endDate AND e.status = :status")
    fun sumDiferencaValorByDateRangeAndStatus(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date, @Param("status") status: TransactionStatus): Double
    
    @Query("SELECT COALESCE(SUM(e.diferencaValor), 0.0) FROM Exchange e WHERE e.deleted = false AND e.exchangeDate BETWEEN :startDate AND :endDate")
    fun sumDiferencaValorByDateRange(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date): Double
    
    @Query("SELECT COALESCE(SUM(e.diferencaValor), 0.0) FROM Exchange e WHERE e.deleted = false AND e.status = :status")
    fun sumDiferencaValorByStatus(@Param("status") status: TransactionStatus): Double
    
    @Query("SELECT COALESCE(SUM(e.diferencaValor), 0.0) FROM Exchange e WHERE e.deleted = false")
    fun sumDiferencaValorAll(): Double
    
    @Query("SELECT e FROM Exchange e WHERE e.deleted = false AND (e.vehicleEntrada.licensePlate = :licensePlate OR e.vehicleSaida.licensePlate = :licensePlate) ORDER BY e.exchangeDate DESC")
    fun findAllByVehicleLicensePlateAndDeletedFalse(@Param("licensePlate") licensePlate: String): List<Exchange>
    
    @Query("SELECT COUNT(e) FROM Exchange e WHERE e.vehicleEntrada.licensePlate = :licensePlate OR e.vehicleSaida.licensePlate = :licensePlate")
    fun countByVehicleLicensePlate(@Param("licensePlate") licensePlate: String): Long
}
