package application

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

class ConsulConfigManager(
    private val serviceName: String,
    private val profile: String = "dev"
) {
    private val client = HttpClient(CIO)
    private val consulHost = "http://localhost:8500/v1/kv"

    // Зберігаємо поточну конфігурацію. Спочатку вона порожня.
    private val _configFlow = MutableStateFlow<Map<String, String>>(emptyMap())
    val configFlow = _configFlow.asStateFlow()

    // Функція, яка працюватиме у фоні і перевірятиме оновлення
    suspend fun startWatching() = coroutineScope {
        println("🔵 [CONSUL] Запускаємо менеджер для сервісу: $serviceName, профіль: $profile")
        while (isActive) {
            val newConfig = fetchConfig()
            if (newConfig.isNotEmpty() && newConfig != _configFlow.value) {
                _configFlow.value = newConfig
                println("🟢 [CONSUL] Оновлено конфігурацію! Поточні дані: ${_configFlow.value}")
            }
            delay(10000)
        }
    }

    // Отримуємо значення поточного ліміту (зручна функція-хелпер)
    fun getMessageLimit(): Int {
        return _configFlow.value["message_limit"]?.toIntOrNull() ?: 50 // 50 - дефолт
    }

    private suspend fun fetchConfig(): Map<String, String> {
        val mergedConfig = mutableMapOf<String, String>()
        mergedConfig.putAll(fetchFromConsul("config/application"))
        mergedConfig.putAll(fetchFromConsul("config/$serviceName/$profile"))
        return mergedConfig
    }

    private suspend fun fetchFromConsul(path: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val url = "$consulHost/$path?recurse=true"
        println("🟡 [CONSUL] Робимо запит за адресою: $url")

        try {
            val response: HttpResponse = client.get(url)
            if (response.status.value == 200) {
                val responseText = response.bodyAsText()
                println("🟣 [CONSUL] Отримано сиру відповідь: $responseText")

                val jsonArray = Json.parseToJsonElement(responseText).jsonArray
                for (element in jsonArray) {
                    val key = element.jsonObject["Key"]?.jsonPrimitive?.content ?: continue
                    val base64Value = element.jsonObject["Value"]?.jsonPrimitive?.content ?: continue

                    // Декодуємо Base64
                    val decodedValue = String(Base64.getDecoder().decode(base64Value))
                    val simpleKey = key.substringAfterLast("/")
                    result[simpleKey] = decodedValue
                }
            } else {
                println("🔴 [CONSUL] Помилка від Consul. Статус: ${response.status.value} для шляху: $path")
            }
        } catch (e: Exception) {
            println("🔴 [CONSUL] Помилка з'єднання: ${e.message}")
        }
        return result
    }

    fun getConfigValue(key: String): String? {
        return _configFlow.value[key]
    }
}