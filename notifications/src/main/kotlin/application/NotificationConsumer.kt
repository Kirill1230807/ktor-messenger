package com.example.application

import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class MessageSentEvent(val receiverId: Int, val text: String, val messageId: Int)

@Serializable
data class NotificationReplyEvent(val messageId: Int, val status: String)

class NotificationConsumer(private val notificationService: NotificationService) {
    private val QUEUE_NAME = "notification_events"

    @OptIn(DelicateCoroutinesApi::class)
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
                        println(" [x] Отримано подію: '$message'")

                        try {
                            val event = Json.decodeFromString<MessageSentEvent>(message)

                            GlobalScope.launch {
                                if (event.text == "error") {
                                    println(" [!] Імітація помилки для повідомлення ${event.messageId}")
                                    val error = NotificationReplyEvent(event.messageId, "ERROR")
                                    val responseJson = Json.encodeToString(error)
                                    channel.basicPublish(
                                        "",
                                        "chat_replies",
                                        null,
                                        responseJson.toByteArray(Charsets.UTF_8)
                                    )
                                    println(" [x] Відправлено статус ERROR для повідомлення ${event.messageId}")
                                } else {
                                    notificationService.createNotification(
                                        userId = event.receiverId,
                                        message = "У вас нове повідомлення: ${event.text}"
                                    )
                                    println("Сповіщення для користувача ${event.receiverId} успішно створено.")
                                    val successReply = NotificationReplyEvent(event.messageId, "SUCCESS")
                                    val responseJson = Json.encodeToString(successReply)
                                    channel.basicPublish(
                                        "",
                                        "chat_replies",
                                        null,
                                        responseJson.toByteArray(Charsets.UTF_8)
                                    )
                                    println(" [x] Відправлено статус SUCCESS для повідомлення ${event.messageId}")
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