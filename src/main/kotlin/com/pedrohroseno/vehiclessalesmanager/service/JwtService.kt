package com.pedrohroseno.vehiclessalesmanager.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.SecretKey
import java.util.*

@Service
class JwtService {
    @Value("\${jwt.secret:mySecretKey12345678901234567890123456789012345678901234567890}")
    private lateinit var secretKey: String

    @Value("\${jwt.expiration:86400000}") // 24 horas em milissegundos
    private var expiration: Long = 86400000

    /** Normaliza o secret para 256 bits (exigência HS256), evitando falha com JWT_SECRET curto no Railway. */
    private fun getSigningKey(): SecretKey {
        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest(secretKey.toByteArray(StandardCharsets.UTF_8))
        return Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(username: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims.subject
    }

    fun getRoleFromToken(token: String): String {
        val claims = getClaimsFromToken(token)
        return claims["role"] as String
    }

    fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = getClaimsFromToken(token)
            !claims.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }
}
