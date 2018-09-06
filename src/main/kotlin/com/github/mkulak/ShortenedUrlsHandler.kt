package com.github.mkulak

import java.net.URL


interface ShortenedUrlsHandler {
    fun getAll(userId: UserId, prefix: String): UrlsResponse
    fun create(userId: UserId, url: URL, prefix: String): ShortenedUrlDto
    fun get(urlId: UrlId): URL?
}

class ShortenedUrlsHandlerImpl(val shortenedUrlDao: ShortenedUrlDao) : ShortenedUrlsHandler {
    override fun getAll(userId: UserId, prefix: String): UrlsResponse {
        val urls = shortenedUrlDao.getAll(userId)
        return UrlsResponse(urls.map { it.dto(prefix) })
    }

    override fun create(userId: UserId, url: URL, prefix: String): ShortenedUrlDto {
        val existing = shortenedUrlDao.get(userId, url)
        val result = if (existing == null) {
            val shortenedUrl = ShortenedUrl(newUrlId(), url, userId, 0, 1)
            shortenedUrlDao.persist(shortenedUrl)
            shortenedUrl
        } else {
            shortenedUrlDao.incrementSaveCount(existing.id)
            existing
        }
        return result.dto(prefix)
    }

    override fun get(urlId: UrlId): URL? {
        val url = shortenedUrlDao.get(urlId)
        if (url != null) {
            shortenedUrlDao.incrementViewCount(urlId)
        }
        return url?.url
    }

    fun ShortenedUrl.dto(prefix: String) = ShortenedUrlDto(url.toString(), prefix + id.value)
}