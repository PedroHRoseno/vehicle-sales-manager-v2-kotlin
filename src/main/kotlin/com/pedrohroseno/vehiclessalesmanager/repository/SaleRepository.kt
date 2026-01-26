package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Sale
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface SaleRepository : JpaRepository<Sale, Long> {
    @Query("SELECT COALESCE(SUM(s.salePrice), 0) FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    fun sumSalePriceByDateRange(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date): Double
    
    fun findAllByOrderBySaleDateDesc(pageable: Pageable): Page<Sale>
    
    @Query("SELECT s FROM Sale s WHERE s.deleted = false ORDER BY s.saleDate DESC")
    fun findAllByDeletedFalseOrderBySaleDateDesc(pageable: Pageable): Page<Sale>
    
    @Query("SELECT s FROM Sale s WHERE s.deleted = false AND (" +
           "LOWER(s.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.partner.cpf) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.partner.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY s.saleDate DESC")
    fun searchByVehicleOrPartnerAndDeletedFalse(@Param("search") search: String, pageable: Pageable): Page<Sale>
    
    fun findByIdAndDeletedFalse(id: Long): Sale?
}
