package com.example.domain

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String
)

interface UserRepository {
    suspend fun createUser(user: User): User
    suspend fun findByUsername(username: String): User?
    suspend fun findByEmail(email: String): User?
}