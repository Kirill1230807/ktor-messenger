package com.example.application

import com.example.domain.Notification
import com.example.domain.NotificationRepository

class NotificationService(private val notificationRepository: NotificationRepository) {
    suspend fun createNotification(userId: Int, message: String) : Notification {
        return notificationRepository.createNotification(userId, message)
    }

    suspend fun getUnreadNotifications(userId: Int): List<Notification> {
        return notificationRepository.getUnreadByUserId(userId)
    }

    suspend fun markAsRead(id: Int): Boolean {
        return notificationRepository.markAsRead(id)
    }
}