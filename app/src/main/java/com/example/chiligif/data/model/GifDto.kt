package com.example.chiligif.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GifDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "images")
    val images: ImagesDto,
    @Json(name = "rating")
    val rating: String? = null,
    @Json(name = "username")
    val username: String? = null,
    @Json(name = "import_datetime")
    val importDatetime: String? = null,
    @Json(name = "trending_datetime")
    val trendingDatetime: String? = null,
    @Json(name = "url")
    val url: String? = null
)

@JsonClass(generateAdapter = true)
data class ImagesDto(
    @Json(name = "fixed_width")
    val fixedWidth: ImageVariantDto? = null,
    @Json(name = "fixed_width_downsampled")
    val fixedWidthDownsampled: ImageVariantDto? = null,
    @Json(name = "fixed_width_small")
    val fixedWidthSmall: ImageVariantDto? = null,
    @Json(name = "original")
    val original: ImageVariantDto? = null,
    @Json(name = "downsized")
    val downsized: ImageVariantDto? = null,
    @Json(name = "downsized_medium")
    val downsizedMedium: ImageVariantDto? = null,
    @Json(name = "preview_gif")
    val previewGif: ImageVariantDto? = null
)

@JsonClass(generateAdapter = true)
data class ImageVariantDto(
    @Json(name = "url")
    val url: String? = null,
    @Json(name = "width")
    val width: String? = null,
    @Json(name = "height")
    val height: String? = null,
    @Json(name = "size")
    val size: String? = null
)

