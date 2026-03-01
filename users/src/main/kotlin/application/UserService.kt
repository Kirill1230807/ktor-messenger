package com.example.application

import com.example.domain.User
import com.example.domain.UserRepository

class UserService(private val userRepository: UserRepository) {
    suspend fun registerUser(username: String, email: String, passwordHash: String): User {
        val existingUser = userRepository.checkByUsername(username)
        val existingEmail = userRepository.checkByEmail(email)

        if (existingUser != null) {
            throw IllegalArgumentException("This username is already taken")
        }
        if (existingEmail != null) {
            throw IllegalArgumentException("This email is already taken")
        }

        val newUser = User(id = 0, username = username, email = email, passwordHash = passwordHash)
        return userRepository.createUser(newUser)
    }

    suspend fun deleteUser(id: Int): Boolean {
        return userRepository.deleteUser(id)
    }

    suspend fun findUserById(id: Int) : User? {
        return userRepository.findUserById(id)
    }
}