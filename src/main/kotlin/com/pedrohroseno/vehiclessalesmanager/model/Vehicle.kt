package com.pedrohroseno.vehiclessalesmanager.model

import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleBrand
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import jakarta.persistence.*

@Entity
@Table(name = "vehicles")
data class Vehicle(
    @Id
    var licensePlate: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var brand: VehicleBrand,
    
    @Column(nullable = false)
    var modelName: String,
    
    @Column(nullable = false)
    var manufactureYear: Int,
    
    @Column(nullable = false)
    var modelYear: Int,
    
    @Column(nullable = false)
    var color: String,
    
    @Column(nullable = false)
    var kilometersDriven: Int,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: VehicleStatus = VehicleStatus.DISPONIVEL
) {
    // Propriedade computada para compatibilidade com front-end
    val inStock: Boolean
        get() = status == VehicleStatus.DISPONIVEL
}
