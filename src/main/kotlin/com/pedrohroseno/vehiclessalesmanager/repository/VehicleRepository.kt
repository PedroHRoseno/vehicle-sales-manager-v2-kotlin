package com.pedrohroseno.vehiclessalesmanager.repository

import com.pedrohroseno.vehiclessalesmanager.model.Vehicle
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleBrand
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface VehicleRepository : JpaRepository<Vehicle, String> {
    fun findByLicensePlate(licensePlate: String): Vehicle?
    fun findByStatus(status: VehicleStatus, pageable: Pageable): Page<Vehicle>
    fun findByStatus(status: VehicleStatus): List<Vehicle>
    fun findByStatusAndPublishedTrue(status: VehicleStatus): List<Vehicle>
    fun countByStatus(status: VehicleStatus): Long

    @Query(
        """
            SELECT v FROM Vehicle v WHERE
            v.status = :status AND v.published = true
            AND (:brand IS NULL OR v.brand = :brand)
            AND (:maxKm IS NULL OR v.kilometersDriven <= :maxKm)
            AND (:yearMin IS NULL OR v.modelYear >= :yearMin)
        """
    )
    fun findPublicCatalog(
        @Param("status") status: VehicleStatus,
        @Param("brand") brand: VehicleBrand?,
        @Param("maxKm") maxKm: Int?,
        @Param("yearMin") yearMin: Int?
    ): List<Vehicle>

    @Query(
        value = """
            SELECT v FROM Vehicle v WHERE
            (:search IS NULL OR :search = '' OR (
                LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(v.modelName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(CAST(v.brand AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            ))
            AND (:status IS NULL OR v.status = :status)
            AND (:published IS NULL OR v.published = :published)
        """,
        countQuery = """
            SELECT COUNT(v) FROM Vehicle v WHERE
            (:search IS NULL OR :search = '' OR (
                LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(v.modelName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(CAST(v.brand AS string)) LIKE LOWER(CONCAT('%', :search, '%'))
            ))
            AND (:status IS NULL OR v.status = :status)
            AND (:published IS NULL OR v.published = :published)
        """
    )
    fun findFiltered(
        @Param("search") search: String?,
        @Param("status") status: VehicleStatus?,
        @Param("published") published: Boolean?,
        pageable: Pageable
    ): Page<Vehicle>
}
