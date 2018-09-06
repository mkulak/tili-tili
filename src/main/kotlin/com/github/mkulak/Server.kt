package com.github.mkulak

import io.vertx.core.*
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Server")

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    val router = Router.router(vertx).apply {
        get("/").handler { ctx ->
            ctx.response().end("Hello")
        }
    }
    vertx.deployVerticle(HttpVerticle(router, 8080)) {
        if (it.succeeded()) {
            logger.info("Server started on http://localhost:8080")
        } else {
            vertx.close()
            logger.error("Can't start server:", it.cause())
        }
    }
}

class HttpVerticle(val router: Router, val port: Int) : CoroutineVerticle() {
    override suspend fun start() {
        val server = vertx.createHttpServer()
        awaitResult<HttpServer> { server.requestHandler(router::accept).listen(port, it) }
    }
}
