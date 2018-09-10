package com.github.mkulak;
import java.net.URL
import java.util.Base64
import java.util.concurrent.ThreadLocalRandom

data class UrlId(val value: String)

data class UserId(val value: String)

data class ShortenedUrl(
    val id: UrlId,
    val url: URL,
    val userId: UserId,
    val viewCount: Int,
    val saveCount: Int
)

fun newUrlId(): UrlId {
    val bytes = ByteArray(5)
    ThreadLocalRandom.current().nextBytes(bytes)
    val value = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    return UrlId(value)
}