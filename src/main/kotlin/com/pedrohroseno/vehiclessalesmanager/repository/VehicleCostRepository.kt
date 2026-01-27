package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.VehicleCost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Date

@Repository
interface VehicleCostRepository : JpaRepository<VehicleCost, Long> {
    fun findByVehicleLicensePlateAndDeletedFalse(licensePlate: String): List<VehicleCost>
    
    @Query("SELECT COALESCE(SUM(vc.cost), 0) FROM VehicleCost vc WHERE vc.vehicle.licensePlate = :licensePlate AND vc.deleted = false")
    fun sumCostsByVehicleLicensePlate(@Param("licensePlate") licensePlate: String): Double
    
    @Query("SELECT COALESCE(SUM(vc.cost), 0) FROM VehicleCost vc WHERE vc.deleted = false")
    fun sumAllCosts(): Double
    
    @Query("SELECT COALESCE(SUM(vc.cost), 0) FROM VehicleCost vc WHERE vc.costDate BETWEEN :startDate AND :endDate AND vc.deleted = false")
    fun sumCostsByDateRange(@Param("startDate") startDate: Date, @Param("endDate") endDate: Date): Double
}
