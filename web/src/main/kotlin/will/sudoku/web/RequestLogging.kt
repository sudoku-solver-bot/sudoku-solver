package will.sudoku.web

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.*

/**
 * Attribute key for storing request ID in call attributes
 */
val RequestIdKey = AttributeKey<String>("RequestId")

/**
 * Extension to get request ID from call
 */
val ApplicationCall.requestId: String
    get() = attributes.getOrNull(RequestIdKey) ?: "unknown"

/**
 * Install request logging with request ID tracking
 */
fun Application.installRequestLogging() {
    val logger = LoggerFactory.getLogger("RequestLogging")
    
    intercept(ApplicationCallPipeline.Setup) {
        // Generate or extract request ID
        val requestId = call.request.headers["X-Request-ID"] 
            ?: UUID.randomUUID().toString().substring(0, 8)
        
        call.attributes.put(RequestIdKey, requestId)
        
        // Add to MDC for structured logging
        MDC.putCloseable("requestId", requestId).use {
            MDC.putCloseable("method", call.request.httpMethod.value).use {
                MDC.putCloseable("path", call.request.path()).use {
                    logger.info("Request started: ${call.request.httpMethod} ${call.request.path()}")
                    
                    try {
                        proceed()
                    } finally {
                        logger.info("Request completed: ${call.request.httpMethod} ${call.request.path()}")
                    }
                }
            }
        }
    }
}
