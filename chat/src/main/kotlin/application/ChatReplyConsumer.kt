package com.example.application

import com.example.domain.MessageRepository
import com.example.infrastructure.NotificationReplyEvent
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.ConnectException

class ChatReplyConsumer(private val messageRepository: MessageRepository) {
    private val QUEUE_NAME = "chat_replies"

    fun startListening() {
        GlobalScope.launch(Dispatchers.IO) {
            val factory = ConnectionFactory().apply { host = "localhost" }

            while (isActive) {
                try {
                    val connection = factory.newConnection()
                    val channel = connection.createChannel()

                    channel.queueDeclare(QUEUE_NAME, true, false, false, null)
                    println(" [*] Успішно підключено до RabbitMQ. Очікування подій...")

                    val deliverCallback = DeliverCallback { _, delivery ->
                        val message = String(delivery.body, Charsets.UTF_8)

                        try {
                            val reply = Json.decodeFromString<NotificationReplyEvent>(message)

                            GlobalScope.launch {
                                if (reply.status == "SUCCESS") {
                                    messageRepository.updateMessageStatus(reply.messageId, "DELIVERED")
                                    println(" [x] Сага успішна! Статус повідомлення: ${reply.messageId} оновлено на DELIVERED")
                                } else if (reply.status == "ERROR") {
                                    messageRepository.updateMessageStatus(reply.messageId, "FAILED")
                                    println(" [x] Відкат саги! Статус повідомлення: ${reply.messageId} змінено на FAILED")
                                }
                            }
                        } catch (e: Exception) {
                            println("Помилка обробки події: ${e.message}")
                        }
                    }

                    channel.basicConsume(QUEUE_NAME, true, deliverCallback, { _ -> })

                    break

                } catch (e: java.net.ConnectException) {
                    println(" [!] Брокер RabbitMQ недоступний. Повторна спроба підключення через 5 секунд...")
                    delay(5000)
                } catch (e: Exception) {
                    println(" [!] Сталася непередбачувана помилка: ${e.message}")
                    delay(5000)
                }
            }
        }
    }
}