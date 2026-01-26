package com.pedrohroseno.vehiclessalesmanager.model

import jakarta.persistence.*

@Entity
@Table(name = "partners")
data class Partner(
    @Id
    @Column(unique = true, nullable = false)
    var cpf: String,
    
    @Column(nullable = false)
    var name: String,
    
    var phoneNumber1: String? = null,
    var phoneNumber2: String? = null,
    
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "address_id")
    var address: Address? = null,
    
    @OneToMany(mappedBy = "partner", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sales: MutableList<Sale> = mutableListOf(),
    
    @OneToMany(mappedBy = "partner", cascade = [CascadeType.ALL], orphanRemoval = true)
    var purchases: MutableList<Purchase> = mutableListOf(),
    
    @OneToMany(mappedBy = "partner", cascade = [CascadeType.ALL], orphanRemoval = true)
    var exchanges: MutableList<Exchange> = mutableListOf()
)
