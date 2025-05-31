package com.github.sn0wo2.error

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

data class Response(
    val success: Boolean,
    val message: String,
    val data: Any?,
)

class APIException(
    val statusCode: HttpStatusCode,
    override val message: String
) : Exception(message)

const val prefix = "/v1"

fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<APIException> { call, cause ->
            call.respond(
                cause.statusCode,
                Response(
                    success = false,
                    message = cause.message,
                    data = null
                )
            )
        }

        exception<Throwable> { call, cause ->
            val message = if (call.application.developmentMode) {
                cause.message ?: "Internal Server Error"
            } else {
                "Internal Server Error"
            }

            cause.printStackTrace()

            call.respond(
                HttpStatusCode.InternalServerError,
                Response(
                    success = false,
                    message = message,
                    data = null
                )
            )
        }

        status(*HttpStatusCode.allStatusCodes.toTypedArray()) { call, status ->
            if (status.isSuccess() && call.response.status() == null) {
                call.respond(
                    status,
                    Response(
                        success = false,
                        message = status.description,
                        data = null
                    )
                )
            }
        }
    }
}