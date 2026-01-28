package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Sale
import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface SaleRepository : JpaRepository<Sale, Long> {
    @Query("SELECT COALESCE(SUM(s.salePrice), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.deleted = false AND s.status = :status")
    fun sumSalePriceByDateRangeAndStatus(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date, @Param("status") status: TransactionStatus): Double
    
    @Query("SELECT COALESCE(SUM(s.salePrice), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    fun sumSalePriceByDateRange(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date): Double
    
    @Query("SELECT COALESCE(SUM(s.salePrice), 0) FROM Sale s WHERE s.deleted = false AND s.status = :status")
    fun sumSalePriceByStatus(@Param("status") status: TransactionStatus): Double
    
    fun findAllByOrderBySaleDateDesc(pageable: Pageable): Page<Sale>
    
    @Query("SELECT s FROM Sale s WHERE s.deleted = false ORDER BY s.saleDate DESC")
    fun findAllByDeletedFalseOrderBySaleDateDesc(pageable: Pageable): Page<Sale>
    
    @Query("SELECT s FROM Sale s WHERE s.deleted = false AND (" +
           "LOWER(s.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.partner.document) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.partner.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY s.saleDate DESC")
    fun searchByVehicleOrPartnerAndDeletedFalse(@Param("search") search: String, pageable: Pageable): Page<Sale>
    
    fun findByIdAndDeletedFalse(id: Long): Sale?
    
    @Query("SELECT s FROM Sale s WHERE s.deleted = false AND s.vehicle.licensePlate = :licensePlate ORDER BY s.saleDate DESC")
    fun findAllByVehicleLicensePlateAndDeletedFalse(@Param("licensePlate") licensePlate: String): List<Sale>
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.vehicle.licensePlate = :licensePlate")
    fun countByVehicleLicensePlate(@Param("licensePlate") licensePlate: String): Long
}
