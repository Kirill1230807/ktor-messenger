import com.example.CityService
import application.ConsulConfigManager
import com.example.infrastructure.MessageTable
import com.example.infrastructure.NotificationTable
import io.ktor.server.application.*
import java.sql.Connection
import java.sql.DriverManager
import org.jetbrains.exposed.sql.*
import com.example.infrastructure.UserTable
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases(configManager: ConsulConfigManager) {
    // 1. Отримуємо нечутливі налаштування (Application-specific) з Consul
    // Якщо Consul недоступний, використовуємо фоллбек для локальної H2
    val dbUrl = configManager.getConfigValue("database_url") ?: "jdbc:h2:file:./data/chat_db;DB_CLOSE_DELAY=-1"
    val dbDriver = configManager.getConfigValue("database_driver") ?: "org.h2.Driver"

    // 2. Отримуємо секрети (Secrets Management) зі змінних середовища ОС
    val dbUser = System.getenv("DB_USER") ?: "root"
    val dbPassword = System.getenv("DB_PASSWORD") ?: ""

    log.info("Connecting to H2 database at $dbUrl")

    // 3. Ініціалізуємо підключення для Exposed
    val database = Database.connect(
        url = dbUrl,
        user = dbUser,
        driver = dbDriver,
        password = dbPassword
    )

    // 4. Створюємо таблиці
    transaction(database) {
        SchemaUtils.create(UserTable, NotificationTable, MessageTable)
    }

    // 5. Оскільки твій CityService вимагає raw JDBC Connection,
    // створюємо його напряму, використовуючи ті самі змінні та секрети
    val dbConnection: Connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
    val cityService = CityService(dbConnection)
}