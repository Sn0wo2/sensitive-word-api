package com.github.sn0wo2

import com.github.houbb.sensitive.word.bs.SensitiveWordBs
import com.github.houbb.sensitive.word.support.allow.WordAllows
import com.github.houbb.sensitive.word.support.deny.WordDenys
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores
import com.github.houbb.sensitive.word.support.resultcondition.WordResultConditions
import com.github.sn0wo2.word.WordAllow
import com.github.sn0wo2.word.WordDeny
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

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

val lock = Any()
var email = true
var url = true
var ipv4 = true
var word = true
var bs: SensitiveWordBs? = null

private val logger = LoggerFactory.getLogger("CheckController")

private fun initBs() {
    bs = SensitiveWordBs.newInstance()
        .ignoreRepeat(true)
        .enableEmailCheck(email)
        .enableUrlCheck(url)
        .enableIpv4Check(ipv4)
        .enableWordCheck(word)
        .charIgnore(SensitiveWordCharIgnores.specialChars())
        .wordResultCondition(WordResultConditions.englishWordMatch())
        .wordAllow(WordAllows.chains(WordAllows.defaults(), WordAllow()))
        .wordDeny(WordDenys.chains(WordDenys.defaults(), WordDeny()))
        .init()
}

fun Application.initSensitiveWordChecker() {
    initBs()
}

fun Application.configureRouting() {
    routing {
        post("$prefix/check") {
            val content = call.receiveText()

            val emailParam = call.request.queryParameters["email"]?.toBooleanStrictOrNull() ?: true
            val urlParam = call.request.queryParameters["url"]?.toBooleanStrictOrNull() ?: true
            val ipv4Param = call.request.queryParameters["ipv4"]?.toBooleanStrictOrNull() ?: true
            val wordParam = call.request.queryParameters["word"]?.toBooleanStrictOrNull() ?: true

            synchronized(lock) {
                if (email != emailParam || url != urlParam || ipv4 != ipv4Param || word != wordParam) {
                    email = emailParam
                    url = urlParam
                    ipv4 = ipv4Param
                    word = wordParam
                    initBs()
                }
            }

            val currentBs =
                bs ?: throw APIException(HttpStatusCode.InternalServerError, "SensitiveWordBs not initialized")

            val check = currentBs.contains(content)
            val response = mutableMapOf<String, Any>(
                "pass" to !check
            )
            if (check) {
                response["disallowed_word"] = currentBs.findAll(content)
                response["replace_text"] = currentBs.replace(content)
            }

            logger.info("content: {}, response: {}", content, response)

            call.respond(
                HttpStatusCode.OK,
                Response(
                    success = true,
                    message = "Success",
                    data = response
                )
            )
        }
    }
}