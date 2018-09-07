package com.github.mkulak.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.KotlinModule


fun ObjectMapper.pimp() {
    registerModule(KotlinModule())
    propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
}