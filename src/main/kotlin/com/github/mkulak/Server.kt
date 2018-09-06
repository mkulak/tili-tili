package com.github.mkulak

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.andrewoma.kwery.core.SessionFactory
import com.github.andrewoma.kwery.core.dialect.PostgresDialect
import com.github.andrewoma.kwery.core.interceptor.LoggingInterceptor
import com.zaxxer.hikari.HikariDataSource
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("Server")

fun main(args: Array<String>) {
    val vertx = Vertx.vertx()

    Json.mapper.apply {
        registerModule(KotlinModule())
        propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
    }

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

    vertx.deployVerticle(HttpVerticle(router, 8080)) {
        if (it.succeeded()) {
            logger.info("Server started on http://localhost:8080")
        } else {
            vertx.close()
            logger.error("Can't start server:", it.cause())
        }
    }
}

