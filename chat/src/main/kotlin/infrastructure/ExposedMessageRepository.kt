package com.example.infrastructure

import com.example.domain.Message
import com.example.domain.MessageRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

object MessageTable : Table("message") {
    val id = integer("id").autoIncrement()
    val senderId = integer("sender_id")
    val receiverId = integer("receiver_id")
    val text = varchar("text", 255)
    val timestamp = long("timestamp")
    val status = varchar("status", 50)

    override val primaryKey = PrimaryKey(id)
}

object OutboxTable : Table("outbox") {
    val id = integer("id").autoIncrement()
    val eventType = varchar("event_type", 50)
    val payload = text("payload")
    val isProcessed = bool("is_processed").default(false)

    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class MessageSentEvent(val receiverId: Int, val text: String, val messageId: Int)

@Serializable
data class NotificationReplyEvent(val messageId: Int, val status: String)

class ExposedMessageRepository : MessageRepository {

    override suspend fun sendMessage(
        senderId: Int,
        receiverId: Int,
        text: String,
        timestamp: Long
    ): Message = newSuspendedTransaction {

        val insertStatement = MessageTable.insert {
            it[this.senderId] = senderId
            it[this.receiverId] = receiverId
            it[this.text] = text
            it[this.timestamp] = timestamp
            it[this.status] = "PENDING"
        }
        val generatedMessageId = insertStatement[MessageTable.id] ?: 0

        val eventPayload = Json.encodeToString(MessageSentEvent(receiverId, text, generatedMessageId))
        OutboxTable.insert {
            it[this.eventType] = "MessageSent"
            it[this.payload] = eventPayload
            it[this.isProcessed] = false
        }

        Message(
            id = insertStatement[MessageTable.id],
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            timestamp = timestamp,
            status = "PENDING"
        )
    }

    override suspend fun getHistory(
        userOneId: Int,
        userTwoId: Int
    ): List<Message> = newSuspendedTransaction {
        MessageTable.selectAll()
            .where {
                ((MessageTable.senderId eq userOneId) and (MessageTable.receiverId eq userTwoId)) or
                        ((MessageTable.senderId eq userTwoId) and (MessageTable.receiverId eq userOneId))
            }
            .orderBy(MessageTable.timestamp to SortOrder.ASC)
            .map {
                Message(
                    id = it[MessageTable.id],
                    senderId = it[MessageTable.senderId],
                    receiverId = it[MessageTable.receiverId],
                    text = it[MessageTable.text],
                    timestamp = it[MessageTable.timestamp],
                    status = it[MessageTable.status]
                )
            }
    }

    override suspend fun updateMessageStatus(
        messageId: Int,
        newStatus: String
    ): Boolean = newSuspendedTransaction {
        val updateRowsCount = MessageTable.update({ MessageTable.id eq messageId }) {
            it[this.status] = newStatus
        }
        updateRowsCount > 0
    }
}