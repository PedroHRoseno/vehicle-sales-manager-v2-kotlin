package com.pedrohroseno.vehiclessalesmanager.model

import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleBrand
import com.pedrohroseno.vehiclessalesmanager.model.enums.VehicleStatus
import jakarta.persistence.*

@Entity
@Table(name = "vehicles")
data class Vehicle(
    @Id
    @Column(name = "license_plate")
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
    
    @Column(nullable = false)
    var published: Boolean = false,

    @Column(nullable = true, length = 1000)
    var description: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "vehicle_images",
        joinColumns = [JoinColumn(name = "vehicle_license_plate")]
    )
    @Column(name = "image_url", nullable = false, length = 1000)
    var imageUrlList: MutableList<String> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: VehicleStatus = VehicleStatus.DISPONIVEL
) {
    // Propriedade computada para compatibilidade com front-end
    val inStock: Boolean
        get() = status == VehicleStatus.DISPONIVEL
}
