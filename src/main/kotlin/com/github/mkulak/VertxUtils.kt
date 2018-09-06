package com.github.mkulak

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitResult


suspend fun <T> Vertx.awaitBlocking(block: () -> T): T =
    awaitResult<T> { handler ->
        executeBlocking<T>({ it.complete(block()) }, handler::handle)
    }
