package com.github.mkulak

import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult

class HttpVerticle(val router: Router, val port: Int) : CoroutineVerticle() {
    override suspend fun start() {
        val server = vertx.createHttpServer()
        awaitResult<HttpServer> { server.requestHandler(router::accept).listen(port, it) }
    }
}