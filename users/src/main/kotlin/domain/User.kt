package com.example.domain

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val passwordHash: String
)

interface UserRepository {
    suspend fun createUser(user: User): User
    suspend fun checkByUsername(username: String): User?
    suspend fun checkByEmail(email: String): User?
    suspend fun deleteUser(id: Int): Boolean
    suspend fun findUserById(id: Int): User?
}