package com.pedrohroseno.vehiclessalesmanager.service

import com.pedrohroseno.vehiclessalesmanager.model.User
import com.pedrohroseno.vehiclessalesmanager.model.dtos.LoginRequestDTO
import com.pedrohroseno.vehiclessalesmanager.model.dtos.LoginResponseDTO
import com.pedrohroseno.vehiclessalesmanager.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    fun login(loginRequest: LoginRequestDTO): LoginResponseDTO {
        val user = userRepository.findByUsername(loginRequest.username)
            .orElseThrow { IllegalArgumentException("Usuário ou senha inválidos") }

        if (!passwordEncoder.matches(loginRequest.password, user.password)) {
            throw IllegalArgumentException("Usuário ou senha inválidos")
        }

        val token = jwtService.generateToken(user.username, user.role.name)

        return LoginResponseDTO(
            token = token,
            username = user.username,
            role = user.role.name
        )
    }
}
