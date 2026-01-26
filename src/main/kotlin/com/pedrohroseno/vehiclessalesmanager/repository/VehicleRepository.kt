package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VehicleRepository : JpaRepository<Vehicle, String> {
    fun findByLicensePlate(licensePlate: String): Vehicle?
    fun findByStatus(status: VehicleStatus, pageable: Pageable): Page<Vehicle>
    fun countByStatus(status: VehicleStatus): Long
}
