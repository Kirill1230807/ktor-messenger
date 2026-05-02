package com.example.application

import com.example.configManager
import com.example.domain.Message
import com.example.domain.MessageRepository
import com.example.infrastructure.UserClient

class ChatService(
    private val messageRepository: MessageRepository,
    private val userClient: UserClient
) {

//    fun getMessagesForUser() {
//        // Кожного разу при виклику методу береться НАЙСВІЖІШЕ значення
//        val currentLimit = configManager.getMessageLimit()
//        println("Витягуємо з БД повідомлення. Ліміт: $currentLimit")
//        // ... логіка виклику ExposedMessageRepository ...
//    }

    suspend fun sendMessage(senderId: Int, receiverId: Int, text: String, correlationId: String? = null): Message {
        if (text.isBlank()) {
            throw IllegalArgumentException("Message cannot be empty")
        }

        val userExists = userClient.checkUserExist(receiverId, correlationId)
        if (!userExists) {
            throw IllegalArgumentException("Receiver service unavailable or user not found")
        }

        val timestamp = System.currentTimeMillis()
        return messageRepository.sendMessage(senderId, receiverId, text, timestamp)
    }

    suspend fun getChatHistory(myId: Int, contactId: Int): List<Message> {
        return messageRepository.getHistory(myId, contactId)
    }


}