package com.github.mkulak

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.mkulak.utils.pimp
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServer
import io.vertx.core.json.Json
import io.vertx.ext.web.client.*
import io.vertx.kotlin.coroutines.awaitResult
import io.vertx.kotlin.ext.web.client.WebClientOptions
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import java.net.ServerSocket
import java.net.URL
import kotlin.test.assertEquals

class HttpApiTest {
    val vertx = Vertx.vertx()
    val port = findRandomFreePort()
    val serverUrl = "http://localhost:$port/"

    @Before fun setup() {
        Json.mapper.pimp()
    }

    @Test
    fun `shorten-unshorten should work`() = runBlocking<Unit> {
        val expectedUrl = URL("http://ya.ru/path?param=value")
        val expectedUrlId = UrlId("1234")
        val expectedPrefix = serverUrl
        val expectedDto = ShortenedUrlDto("http://some.long.url", "http://some.short.url")
        val expectedUrlsResponse = UrlsResponse(listOf(
            ShortenedUrlDto("1", "2"),
            ShortenedUrlDto("a", "b")
        ))

        val handler = object : ShortenedUrlsHandler {
            override suspend fun getAll(userId: UserId, prefix: String): UrlsResponse {
                assertEquals(expectedPrefix, prefix)
                return expectedUrlsResponse
            }

            override suspend fun create(userId: UserId, url: URL, prefix: String): ShortenedUrlDto {
                assertEquals(expectedUrl, url)
                assertEquals(expectedPrefix, prefix)
                return expectedDto
            }

            override suspend fun get(urlId: UrlId): URL? {
                assertEquals(expectedUrlId, urlId)
                return expectedUrl
            }
        }
        val api = HttpApi(vertx, handler)
        val server = vertx.createHttpServer()
        val router = api.router()
        awaitResult<HttpServer> { server.requestHandler(router::accept).listen(port, it) }

        val client = WebClient.create(vertx, WebClientOptions(followRedirects = false))

        val response = awaitResult<HttpResponse<Buffer>> {
            client.get(port, "localhost", "/short-urls").send(it)
        }
        assertEquals(200, response.statusCode())
        assertEquals(expectedUrlsResponse, Json.mapper.readValue(response.body().toString()))

        val response2 = awaitResult<HttpResponse<Buffer>> {
            val reqBody = mapOf("url" to expectedUrl)
            client.post(port, "localhost", "/short-urls").sendJson(reqBody, it)
        }
        assertEquals(200, response2.statusCode())
        assertEquals(expectedDto, Json.mapper.readValue(response2.body().toString()))

        val response3 = awaitResult<HttpResponse<Buffer>> {
            client.get(port, "localhost", "/${expectedUrlId.value}").send(it)
        }
        
        assertEquals(301, response3.statusCode())
        assertEquals(expectedUrl.toString(), response3.getHeader("Location"))
    }

    private fun findRandomFreePort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }
}

