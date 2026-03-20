package com.example.application

import com.rabbitmq.client.ConnectionFactory
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.selectAll
import com.example.infrastructure.OutboxTable

class OutboxRelay {
    private val factory = ConnectionFactory().apply { host = "localhost" }
    private val QUEUE_NAME = "notification_events"

    fun startRelay() {
        GlobalScope.launch(Dispatchers.IO) {
            delay(2000)

            while (isActive) {
                try {
                    factory.newConnection().use { connection ->
                        connection.createChannel().use { channel ->
                            channel.queueDeclare(QUEUE_NAME, true, false, false, null)
                            println(" [*] Relay успішно підключився до RabbitMQ.")

                            // Внутрішній цикл: якщо підключення є, перевіряємо БД кожні 5 секунд
                            while (isActive) {
                                processOutboxEvents(channel)
                                delay(5000)
                            }
                        }
                    }
                } catch (e: java.net.ConnectException) {
                    println(" [!] Relay: Брокер RabbitMQ недоступний. Повторна спроба підключення через 5 секунд...")
                    delay(5000) // Чекаємо і пробуємо знову
                } catch (e: Exception) {
                    println(" [!] Помилка Relay: ${e.message}")
                    delay(5000)
                }
            }
        }
    }

    private suspend fun processOutboxEvents(channel: com.rabbitmq.client.Channel) {
        newSuspendedTransaction {
            val pendingEvents = OutboxTable.selectAll().where { OutboxTable.isProcessed eq false }.toList()

            for (event in pendingEvents) {
                val id = event[OutboxTable.id]
                val payload = event[OutboxTable.payload]

                channel.basicPublish("", QUEUE_NAME, null, payload.toByteArray(Charsets.UTF_8))
                println("Relay: Подію $id надіслано в брокер.")

                OutboxTable.update({ OutboxTable.id eq id }) {
                    it[isProcessed] = true
                }
            }
        }
    }
}