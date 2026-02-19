package com.example.application

import com.example.domain.User
import com.example.domain.UserRepository

class UserService(private val userRepository: UserRepository) {
    suspend fun registerUser(username: String, email: String, passwordHash: String): User {
        val existingUser = userRepository.findByUsername(username)
        val existingEmail = userRepository.findByEmail(email)

        if (existingUser != null) {
            throw IllegalArgumentException("This username is already taken")
        }
        if (existingEmail != null) {
            throw IllegalArgumentException("This email is already taken")
        }

        val newUser = User(id = 0, username = username, email = email, passwordHash = passwordHash)
        return userRepository.createUser(newUser)
    }
}