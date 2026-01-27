package com.pedrohroseno.vehiclessalesmanager.model

import com.pedrohroseno.vehiclessalesmanager.model.enums.TransactionStatus
import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "sales")
data class Sale(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    var vehicle: Vehicle,
    
    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    var partner: Partner,
    
    @Column(nullable = false)
    var salePrice: Double,
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var saleDate: Date = Date(),
    
    @Column(nullable = false)
    var deleted: Boolean = false,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TransactionStatus = TransactionStatus.ACTIVE
)
