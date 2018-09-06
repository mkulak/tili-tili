package com.github.mkulak

import com.github.andrewoma.kwery.core.*
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.awaitBlocking
import io.vertx.kotlin.coroutines.awaitResult
import java.net.URL


interface ShortenedUrlDao {
    suspend fun persist(shortenedUrl: ShortenedUrl)
    suspend fun get(id: UrlId): ShortenedUrl?
    suspend fun get(userId: UserId, url: URL): ShortenedUrl?
    suspend fun getAll(userId: UserId): List<ShortenedUrl>
    suspend fun incrementSaveCount(id: UrlId)
    suspend fun incrementViewCount(id: UrlId)
}

class KweryShortenedUrlDao(val vertx: Vertx, val factory: SessionFactory) : ShortenedUrlDao {
    val INSERT = """INSERT INTO shortened_url VALUES (:id, :url, :user_id, :view_count, :save_count)"""
    val SELECT_BY_ID = """SELECT id, url, user_id, view_count, save_count FROM shortened_url WHERE id = :id"""
    val SELECT_BY_USER_ID = """SELECT id, user_id, url, view_count, save_count FROM shortened_url WHERE user_id = :user_id"""
    val SELECT_BY_USER_ID_AND_URL = """id, user_id, url, view_count, save_count FROM shortened_url WHERE user_id = :user_id AND url = :url"""
    val INCREMENT_VIEW_COUNT = """UPDATE shortened_url SET view_count = view_count + 1 WHERE id = :id"""
    val INCREMENT_SAVE_COUNT = """UPDATE shortened_url SET save_count = save_count + 1 WHERE id = :id"""

    override suspend fun persist(shortenedUrl: ShortenedUrl) =
        query<Unit> {
            val params = mapOf(
                "id" to shortenedUrl.id.value,
                "url" to shortenedUrl.url.toString(),
                "user_id" to shortenedUrl.userId.value,
                "view_count" to shortenedUrl.viewCount,
                "save_count" to shortenedUrl.saveCount
            )
            it.insert(INSERT, params, f = {})
        }

    override suspend fun get(id: UrlId): ShortenedUrl? =
        query {
            it.select(SELECT_BY_ID, mapOf("id" to id.value), mapper = ::toShortenedUrl).singleOrNull()
        }

    override suspend fun get(userId: UserId, url: URL): ShortenedUrl? =
        query {
            val params = mapOf("user_id" to userId.value, "url" to url.toString())
            it.select(SELECT_BY_USER_ID_AND_URL, params, mapper = ::toShortenedUrl).singleOrNull()
        }


    override suspend fun getAll(userId: UserId): List<ShortenedUrl> =
        query {
            it.select(SELECT_BY_USER_ID, mapOf("user_id" to userId.value), mapper = ::toShortenedUrl)
        }


    override suspend fun incrementSaveCount(id: UrlId) =
        query<Unit> {
            it.update(INCREMENT_SAVE_COUNT, mapOf("id" to id.value))
        }

    override suspend fun incrementViewCount(id: UrlId) =
        query<Unit> {
            it.update(INCREMENT_VIEW_COUNT, mapOf("id" to id.value))
        }

    private suspend fun <T> query(block: (Session) -> T): T = vertx.awaitBlocking { factory.use(block) }

    private fun toShortenedUrl(row: Row) =
        ShortenedUrl(
            UrlId(row.string("id")),
            URL(row.string("url")),
            UserId(row.string("user_id")),
            row.int("view_count"),
            row.int("save_count")
        )
}

