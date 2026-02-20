import com.example.configureDatabases
import com.example.configureMonitoring
import com.example.configureSecurity
import com.example.configureSockets
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureSecurity()
    configureDatabases()
    configureMonitoring()
    configureRouting()
}
