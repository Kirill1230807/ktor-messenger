package com.example.infrastructure

import com.example.domain.Notification
import com.example.domain.NotificationRepository
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

object NotificationTable : Table("notifications") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id")
    val message = varchar("message", 255)
    val isRead = bool("is_read").default(false)

    override val primaryKey = PrimaryKey(id)
}

class ExposedNotificationRepository : NotificationRepository {
    override suspend fun createNotification(
        userId: Int,
        message: String
    ): Notification = newSuspendedTransaction {
        val insertStatement = NotificationTable.insert {
            it[this.userId] = userId
            it[this.message] = message
            it[this.isRead] = false
        }
        Notification(
            id = insertStatement[NotificationTable.id] ?: 0,
            userId = userId,
            message = message,
            isRead = false
        )
    }

    override suspend fun getUnreadByUserId(userId: Int): List<Notification> = newSuspendedTransaction {
        NotificationTable.selectAll()
            .where { (NotificationTable.userId eq userId) and (NotificationTable.isRead eq false) }
            .map {
                Notification(
                    id = it[NotificationTable.id],
                    userId = it[NotificationTable.userId],
                    message = it[NotificationTable.message],
                    isRead = it[NotificationTable.isRead]
                )
            }
    }

    override suspend fun markAsRead(id: Int): Boolean = newSuspendedTransaction {
        val updatedRowsCount = NotificationTable.update({ NotificationTable.id eq id }) {
            it[isRead] = true
        }
        updatedRowsCount > 0
    }
}