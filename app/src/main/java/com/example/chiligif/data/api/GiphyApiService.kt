package com.example.chiligif.data.api

import com.example.chiligif.data.model.SearchResponseDto
import com.example.chiligif.data.model.SingleGifResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GiphyApiService {
    
    @GET("v1/gifs/search")
    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("rating") rating: String = "g",
        @Query("lang") lang: String = "en"
    ): SearchResponseDto
    
    @GET("v1/gifs/trending")
    suspend fun trending(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("rating") rating: String = "g"
    ): SearchResponseDto
    
    @GET("v1/gifs/{gif_id}")
    suspend fun getGifById(
        @Path("gif_id") gifId: String,
        @Query("api_key") apiKey: String
    ): SingleGifResponseDto
}

