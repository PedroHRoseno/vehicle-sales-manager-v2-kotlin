package com.pedrohroseno.vehiclessalesmanager.config

import com.pedrohroseno.vehiclessalesmanager.model.User
import com.pedrohroseno.vehiclessalesmanager.model.enums.Role
import com.pedrohroseno.vehiclessalesmanager.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class InitialDataLoader(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            val adminUser = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                role = Role.ADMIN
            )
            userRepository.save(adminUser)
            println("Usuário administrador criado: admin / admin123")
        }
    }
}
