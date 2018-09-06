package com.github.mkulak

import com.github.andrewoma.kwery.core.Row
import com.github.andrewoma.kwery.core.SessionFactory
import java.net.URL


interface ShortenedUrlDao {
    fun persist(shortenedUrl: ShortenedUrl)
    fun get(id: UrlId): ShortenedUrl?
    fun get(userId: UserId, url: URL): ShortenedUrl?
    fun getAll(userId: UserId): List<ShortenedUrl>
    fun incrementSaveCount(id: UrlId)
    fun incrementViewCount(id: UrlId)
}

class KweryShortenedUrlDao(val factory: SessionFactory) : ShortenedUrlDao {
    val INSERT = """INSERT INTO shortened_url VALUES (:id, :url, :user_id, :view_count, :save_count)"""
    val SELECT_BY_ID = """SELECT id, url, user_id, view_count, save_count FROM shortened_url WHERE id = :id"""
    val SELECT_BY_USER_ID = """SELECT id, user_id, url, view_count, save_count FROM shortened_url WHERE user_id = :user_id"""
    val SELECT_BY_USER_ID_AND_URL = """id, user_id, url, view_count, save_count FROM shortened_url WHERE user_id = :user_id AND url = :url"""
    val INCREMENT_VIEW_COUNT = """UPDATE shortened_url SET view_count = view_count + 1 WHERE id = :id"""
    val INCREMENT_SAVE_COUNT = """UPDATE shortened_url SET save_count = save_count + 1 WHERE id = :id"""

    override fun persist(shortenedUrl: ShortenedUrl) =
        factory.use<Unit> { session ->
            val params = mapOf(
                "id" to shortenedUrl.id.value,
                "url" to shortenedUrl.url.toString(),
                "user_id" to shortenedUrl.userId.value,
                "view_count" to shortenedUrl.viewCount,
                "save_count" to shortenedUrl.saveCount
            )
            session.insert(INSERT, params, f = {})
        }

    override fun get(id: UrlId): ShortenedUrl? =
        factory.use {
            it.select(SELECT_BY_ID, mapOf("id" to id.value), mapper = ::toShortenedUrl).singleOrNull()
        }

    override fun get(userId: UserId, url: URL): ShortenedUrl? =
        factory.use {
            val params = mapOf("user_id" to userId.value, "url" to url.toString())
            it.select(SELECT_BY_USER_ID_AND_URL, params, mapper = ::toShortenedUrl).singleOrNull()
        }


    override fun getAll(userId: UserId): List<ShortenedUrl> =
        factory.use {
            it.select(SELECT_BY_USER_ID, mapOf("user_id" to userId.value), mapper = ::toShortenedUrl)
        }


    override fun incrementSaveCount(id: UrlId) =
        factory.use<Unit> {
            it.update(INCREMENT_SAVE_COUNT, mapOf("id" to id.value))
        }

    override fun incrementViewCount(id: UrlId) =
        factory.use<Unit> {
            it.update(INCREMENT_VIEW_COUNT, mapOf("id" to id.value))
        }


    private fun toShortenedUrl(row: Row) =
        ShortenedUrl(
            UrlId(row.string("id")),
            URL(row.string("url")),
            UserId(row.string("user_id")),
            row.int("view_count"),
            row.int("save_count")
        )
}
