package com.example.application

import com.example.domain.Message
import com.example.domain.MessageRepository

class ChatService(private val messageRepository: MessageRepository) {
    suspend fun sendMessage(senderId: Int, receiverId: Int, text: String): Message {
        if (text.isBlank()) {
            throw IllegalArgumentException("Message cannot be empty")
        }
        val timestamp = System.currentTimeMillis()
        return messageRepository.sendMessage(senderId, receiverId, text, timestamp)
    }

    suspend fun getChatHistory(myId: Int, contactId: Int): List<Message> {
        return messageRepository.getHistory(myId, contactId)
    }
}