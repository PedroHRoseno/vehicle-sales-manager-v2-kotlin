package com.pedrohroseno.vehiclessalesmanager.model.dtos

data class LoginResponseDTO(
    val token: String,
    val username: String,
    val role: String
)
