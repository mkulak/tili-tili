package com.github.mkulak

import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.Json
import io.vertx.ext.web.*
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.experimental.launch
import java.net.URL

class HttpApi(val vertx: Vertx, val handler: ShortenedUrlsHandler) {
    fun router(): Router {
        val router = Router.router(vertx)
        router.route().handler(BodyHandler.create())
        router.get("/").handle { ctx ->
            ctx.response().end("Tili-tili url shortener")
        }
        router.get("/short-urls").handle { ctx ->
            val req = ctx.request()
            val response = handler.getAll(req.userId, req.prefix)
            ctx.response().end(Json.mapper.writeValueAsString(response))
        }
        router.post("/short-urls").handle { ctx ->
            val req = ctx.request()
            val url = URL(Json.mapper.readTree(ctx.body.bytes)["url"].asText())
            val result = handler.create(req.userId, url, req.prefix)
            ctx.response().end(Json.mapper.writeValueAsString(result))
        }
        router.get("/:id").handle { ctx ->
            val urlId = UrlId(ctx.request().getParam("id"))
            val url = handler.get(urlId)
            if (url != null) {
                ctx.response().putHeader("Location", url.toString()).setStatusCode(301).end()
            } else {
                ctx.response().setStatusCode(404).end("Not found")
            }
        }
        return router
    }

    private val HttpServerRequest.prefix: String get() = "${scheme()}://${host()}:${localAddress().port()}/"

    private val HttpServerRequest.userId: UserId get() = UserId("admin")

    private fun Route.handle(block: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            launch(vertx.dispatcher()) {
                try {
                    block(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }
}


