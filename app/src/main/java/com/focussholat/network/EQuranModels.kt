package com.focussholat.network

import com.google.gson.annotations.SerializedName

// Request models
data class KabKotaRequest(val provinsi: String)

data class JadwalRequest(
    val provinsi: String,
    val kabkota: String,
    val bulan: Int? = null,
    val tahun: Int? = null
)

// Response models
data class EQuranProvinsiResponse(
    val code: Int,
    val message: String,
    val data: List<String>
)

data class EQuranKabKotaResponse(
    val code: Int,
    val message: String,
    val data: List<String>
)

data class EQuranJadwalResponse(
    val code: Int,
    val message: String,
    val data: EQuranJadwalData
)

data class EQuranJadwalData(
    val provinsi: String,
    val kabkota: String,
    val bulan: Int,
    val tahun: Int,
    val bulan_nama: String,
    val jadwal: List<EQuranJadwalHarian>
)

data class EQuranJadwalHarian(
    val tanggal: Int,
    val tanggal_lengkap: String, // "2026-09-28"
    val hari: String,
    val imsak: String,
    val subuh: String,
    val terbit: String,
    val dhuha: String,
    val dzuhur: String,
    val ashar: String,
    val maghrib: String,
    val isya: String
)
