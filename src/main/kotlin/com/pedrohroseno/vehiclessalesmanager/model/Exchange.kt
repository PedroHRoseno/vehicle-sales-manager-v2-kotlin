package com.pedrohroseno.vehiclessalesmanager.model

import jakarta.persistence.*
import java.util.Date

@Entity
@Table(name = "exchanges")
data class Exchange(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne
    @JoinColumn(name = "vehicle_entrada_id", nullable = false)
    var vehicleEntrada: Vehicle,
    
    @ManyToOne
    @JoinColumn(name = "vehicle_saida_id", nullable = false)
    var vehicleSaida: Vehicle,
    
    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    var partner: Partner,
    
    @Column(nullable = false)
    var diferencaValor: Double,
    
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var exchangeDate: Date = Date(),
    
    @Column(nullable = false)
    var deleted: Boolean = false
)
