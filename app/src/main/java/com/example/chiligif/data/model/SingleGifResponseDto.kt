package com.example.chiligif.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SingleGifResponseDto(
    @Json(name = "data")
    val data: GifDto
)

