package com.pedrohroseno.vehiclessalesmanager.model

import jakarta.persistence.*

@Entity
@Table(name = "addresses")
class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    @Column(nullable = true)
    var streetName: String? = null,
    
    @Column(nullable = false)
    var number: String = "",
    
    @Column(nullable = false)
    var city: String = "",
    
    @Column(nullable = false)
    var state: String = "",
    
    var reference: String? = null,
    
    @Column(nullable = false)
    var zipCode: String = ""
) {
    // Construtor padrão para JPA
    constructor() : this(null, null, "", "", "", null, "")
}
