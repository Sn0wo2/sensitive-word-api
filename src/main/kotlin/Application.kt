package com.github.sn0wo2

import com.fasterxml.jackson.annotation.JsonInclude
import com.github.sn0wo2.error.configureStatusPages
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureRouting()
    configureStatusPages()
    install(ContentNegotiation) {
        jackson {
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    initSensitiveWordChecker()
}
