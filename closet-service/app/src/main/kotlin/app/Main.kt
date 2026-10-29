package app

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.LoggerFactory

fun main() {
    val log = LoggerFactory.getLogger("dev.closet.app.Main")
    val config = AppConfig.fromEnv()
    val deps = AppModule.build(config)

    Runtime.getRuntime().addShutdownHook(Thread {
        log.info("Shutting down")
        deps.close()
    })

    log.info("Starting on port {}", config.port)
    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module(config, deps)
    }.start(wait = true)
}

fun Application.module(config: AppConfig, deps: Dependencies) {
    installPlugins(config)
    installStatusPages()
    configureRouting(deps, config)
}
