package com.github.mkulak

import com.github.andrewoma.kwery.core.SessionFactory
import com.github.andrewoma.kwery.core.dialect.PostgresDialect
import com.github.andrewoma.kwery.core.interceptor.LoggingInterceptor
import com.github.mkulak.utils.pimp
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Server")

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()

    Json.mapper.pimp()

    val dataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/tili"
        username = "postgres"
        password = ""
    }

    Flyway().also { it.dataSource = dataSource }.migrate()

    val sessionFactory = SessionFactory(dataSource, PostgresDialect(), LoggingInterceptor())
    val shortenedUrlDao = KweryShortenedUrlDao(vertx, sessionFactory)
    val handler = ShortenedUrlsHandlerImpl(shortenedUrlDao)
    val httpApi = HttpApi(vertx, handler)
    val router = httpApi.router()

    vertx.deployVerticle(HttpVerticle(router, 9090)) {
        if (it.succeeded()) {
            logger.info("Server started on http://localhost:9090")
        } else {
            vertx.close()
            logger.error("Can't start server:", it.cause())
        }
    }
}

