package com.example.chiligif.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResponseDto(
    @Json(name = "data")
    val data: List<GifDto>,
    @Json(name = "pagination")
    val pagination: PaginationDto
)

@JsonClass(generateAdapter = true)
data class PaginationDto(
    @Json(name = "total_count")
    val totalCount: Int,
    @Json(name = "count")
    val count: Int,
    @Json(name = "offset")
    val offset: Int
)

