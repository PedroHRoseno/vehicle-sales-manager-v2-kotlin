package com.pedrohroseno.vehiclessalesmanager.model

import com.pedrohroseno.vehiclessalesmanager.model.enums.Role
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    var username: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.USER
)
