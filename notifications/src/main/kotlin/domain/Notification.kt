package com.example.domain

data class Notification(
    val id: Int,
    val userId: Int,
    val message: String,
    val isRead: Boolean
)

interface NotificationRepository {
    suspend fun createNotification(userId: Int, message: String): Notification
    suspend fun getUnreadByUserId(userId: Int): List<Notification>
    suspend fun markAsRead(id: Int): Boolean
}