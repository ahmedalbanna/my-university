package com.example.data.remote

import com.example.data.model.Book
import com.example.data.model.Project
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * P4-3: Retrofit paginated API — replaces static `JamaatyRepository.getBooks()/getProjects()` (libs.versions 2.12 + Moshi).
 * Backend: Yemen universities gateway (Sana'a, Aden, Taiz etc.) — paginated, filtered server-side.
 * Interceptor: OkHttp logging only in debug via `debugImplementation(libs.logging.interceptor)`.
 */
interface JamaatyApiService {

    @GET("v1/books")
    suspend fun getBooks(
        @Query("faculty") faculty: String? = null,
        @Query("level") level: Int? = null,
        @Query("semester") semester: Int? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): PaginatedResponse<Book>

    @GET("v1/projects")
    suspend fun getProjects(
        @Query("faculty") faculty: String? = null,
        @Query("level") level: Int? = null,
        @Query("year") year: String? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): PaginatedResponse<Project>

    @GET("v1/governorates")
    suspend fun getGovernorates(): List<String>

    @GET("v1/universities")
    suspend fun getUniversities(@Query("governorate") governorate: String? = null): List<String>
}

data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val hasMore: Boolean
)
