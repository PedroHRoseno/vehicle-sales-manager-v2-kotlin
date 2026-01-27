package com.pedrohroseno.vehiclessalesmanager.model

import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "vehicle_costs")
data class VehicleCost(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne
    @JoinColumn(name = "vehicle_license_plate", nullable = false)
    var vehicle: Vehicle,
    
    @Column(nullable = false)
    var cost: Double,
    
    @Column(nullable = false)
    var description: String,
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var costDate: Date = Date(),
    
    @Column(nullable = false)
    var deleted: Boolean = false
)
