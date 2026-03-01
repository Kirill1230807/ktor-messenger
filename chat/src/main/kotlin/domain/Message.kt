package com.example.domain

data class Message(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val text: String,
    val timestamp: Long
)

interface MessageRepository {
    suspend fun sendMessage(senderId: Int, receiverId: Int, text: String, timestamp: Long): Message
    suspend fun getHistory(userOneId: Int, userTwoId: Int): List<Message>
}
