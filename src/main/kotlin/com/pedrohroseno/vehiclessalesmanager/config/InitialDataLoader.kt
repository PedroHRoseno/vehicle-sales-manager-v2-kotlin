package com.pedrohroseno.vehiclessalesmanager.config

import com.pedrohroseno.vehiclessalesmanager.model.User
import com.pedrohroseno.vehiclessalesmanager.model.enums.Role
import com.pedrohroseno.vehiclessalesmanager.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class InitialDataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${ADMIN_USERNAME:}") private val adminUsername: String,
    @Value("\${ADMIN_PASSWORD:}") private val adminPassword: String
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            val username = adminUsername.ifBlank { "admin" }
            val password = if (adminPassword.isNotBlank()) adminPassword else "admin123"
            val adminUser = User(
                username = username,
                password = passwordEncoder.encode(password),
                role = Role.ADMIN
            )
            userRepository.save(adminUser)
            if (adminUsername.isNotBlank() && adminPassword.isNotBlank()) {
                println("Usuário administrador criado a partir de ADMIN_USERNAME / ADMIN_PASSWORD")
            } else {
                println("Usuário administrador padrão criado: admin / admin123 (defina ADMIN_USERNAME e ADMIN_PASSWORD para usar credenciais próprias)")
            }
        }
    }
}
