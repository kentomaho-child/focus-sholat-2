package com.focussholat.network

import com.google.gson.annotations.SerializedName

data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: AladhanData
)

data class AladhanData(
    val timings: AladhanTimings,
    val date: AladhanDate,
    val meta: AladhanMeta
)

data class AladhanTimings(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Sunset") val sunset: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String,
    @SerializedName("Imsak") val imsak: String,
    @SerializedName("Midnight") val midnight: String
)

data class AladhanDate(
    val readable: String,
    val timestamp: String,
    val gregorian: AladhanGregorian,
    val hijri: AladhanHijri
)

data class AladhanGregorian(
    val date: String,
    val format: String,
    val day: String,
    val month: AladhanMonth,
    val year: String
)

data class AladhanHijri(
    val date: String,
    val format: String,
    val day: String,
    val month: AladhanMonth,
    val year: String
)

data class AladhanMonth(
    val number: Int,
    val en: String,
    @SerializedName("ar") val ar: String? = null
)

data class AladhanMeta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val method: AladhanMethod
)

data class AladhanMethod(
    val id: Int,
    val name: String
)
