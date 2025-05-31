package com.github.sn0wo2

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger? = LoggerFactory.getLogger("CheckController")


fun Application.configureHTTP() {
    install(CORS) {
        anyHost()
        anyMethod()
    }
}