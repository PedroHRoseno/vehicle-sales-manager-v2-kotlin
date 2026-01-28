package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Purchase
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface PurchaseRepository : JpaRepository<Purchase, Long> {
    @Query("SELECT COALESCE(SUM(p.purchasePrice), 0) FROM Purchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate AND p.deleted = false AND p.status = :status")
    fun sumPurchasePriceByDateRangeAndStatus(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date, @Param("status") status: TransactionStatus): Double
    
    @Query("SELECT COALESCE(SUM(p.purchasePrice), 0) FROM Purchase p WHERE p.purchaseDate BETWEEN :startDate AND :endDate")
    fun sumPurchasePriceByDateRange(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date): Double
    
    @Query("SELECT COALESCE(SUM(p.purchasePrice), 0) FROM Purchase p WHERE p.deleted = false AND p.status = :status")
    fun sumPurchasePriceByStatus(@Param("status") status: TransactionStatus): Double
    
    fun findAllByOrderByPurchaseDateDesc(pageable: Pageable): Page<Purchase>
    
    @Query("SELECT p FROM Purchase p WHERE p.deleted = false ORDER BY p.purchaseDate DESC")
    fun findAllByDeletedFalseOrderByPurchaseDateDesc(pageable: Pageable): Page<Purchase>
    
    @Query("SELECT p FROM Purchase p WHERE p.deleted = false AND (" +
           "LOWER(p.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.partner.document) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.partner.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY p.purchaseDate DESC")
    fun searchByVehicleOrPartnerAndDeletedFalse(@Param("search") search: String, pageable: Pageable): Page<Purchase>
    
    fun findByIdAndDeletedFalse(id: Long): Purchase?
    
    @Query("SELECT p FROM Purchase p WHERE p.deleted = false AND p.vehicle.licensePlate = :licensePlate ORDER BY p.purchaseDate DESC")
    fun findAllByVehicleLicensePlateAndDeletedFalse(@Param("licensePlate") licensePlate: String): List<Purchase>
    
    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.vehicle.licensePlate = :licensePlate")
    fun countByVehicleLicensePlate(@Param("licensePlate") licensePlate: String): Long
}
