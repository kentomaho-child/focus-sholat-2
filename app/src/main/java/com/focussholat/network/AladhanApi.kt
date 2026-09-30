package com.focussholat.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {

    /**
     * Get prayer times by coordinates
     * GET https://api.aladhan.com/v1/timings/{timestamp}?latitude=...&longitude=...&method=11
     */
    @GET("v1/timings/{date}")
    suspend fun getPrayerTimesByCoordinates(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 11 // Kementerian Agama Indonesia
    ): AladhanResponse

    /**
     * Get prayer times by city name
     * GET https://api.aladhan.com/v1/timingsByCity?city=Jakarta&country=Indonesia&method=11
     */
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimesByCity(
        @Query("city") city: String,
        @Query("country") country: String = "Indonesia",
        @Query("method") method: Int = 11,
        @Query("date") date: String? = null
    ): AladhanResponse
}
