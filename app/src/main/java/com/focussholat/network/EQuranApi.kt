package com.focussholat.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EQuranApi {

    @GET("api/v2/shalat/provinsi")
    suspend fun getProvinsi(): EQuranProvinsiResponse

    @POST("api/v2/shalat/kabkota")
    suspend fun getKabKota(@Body body: KabKotaRequest): EQuranKabKotaResponse

    @POST("api/v2/shalat")
    suspend fun getJadwalShalat(@Body body: JadwalRequest): EQuranJadwalResponse
}
