package com.github.mkulak


data class ShortenedUrlDto(val url: String, val shortUrl: String)

data class UrlsResponse(val urls: List<ShortenedUrlDto>)
