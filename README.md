# FocusSholat 🕌

Aplikasi Android untuk memblokir aplikasi yang mengganggu selama waktu sholat dan jadwal kustom.

## Fitur Utama

| Fitur | Status |
|-------|--------|
| ✅ Blokir otomatis saat waktu sholat (Subuh, Dzuhur, Ashar, Maghrib, Isya) | Implemented |
| ✅ Jadwal blokir kustom (rentang waktu + hari) | Implemented |
| ✅ Daftar aplikasi yang diblokir dapat dikustomisasi | Implemented |
| ✅ Notifikasi pengingat sebelum waktu sholat | Implemented |
| ✅ Whitelist aplikasi (tidak pernah diblokir) | Implemented |
| ✅ Override Darurat (buka sementara N menit) | Implemented |
| ✅ PIN/Password + Strict Mode | Implemented |
| ✅ Waktu sholat via GPS atau kota manual (Aladhan API) | Implemented |
| ✅ Statistik penggunaan fokus | Implemented |

## Cara Build APK

### Prasyarat
- Android Studio Hedgehog (2023.1.1) atau lebih baru
- JDK 17
- Android SDK 34

### Langkah Build

1. **Buka project di Android Studio**
   ```
   File → Open → pilih folder focus-sholat/
   ```

2. **Sinkronisasi Gradle**
   - Klik "Sync Now" di banner yang muncul
   - Tunggu hingga selesai (butuh internet pertama kali)

3. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   
   Atau via terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   
   APK tersimpan di: `app/build/outputs/apk/debug/app-debug.apk`

4. **Build Release APK** (untuk distribusi)
   ```bash
   ./gradlew assembleRelease
   ```

## Instalasi di HP Android

### Cara 1: Via ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Cara 2: Transfer File
1. Copy APK ke HP via kabel USB atau WhatsApp
2. Buka file manager di HP
3. Tap APK → Install
4. Jika muncul peringatan "Install dari sumber tidak dikenal" → Izinkan

## Pengaturan Pertama Kali (WAJIB!)

Setelah install, lakukan langkah berikut agar pemblokiran berfungsi:

### 1. Aktifkan Accessibility Service
```
Pengaturan HP → Aksesibilitas → Layanan yang Dipasang → FocusSholat → Aktifkan
```
> ⚠️ Ini WAJIB untuk memblokir aplikasi secara andal

### 2. Izinkan Akses Penggunaan
```
Pengaturan HP → Aplikasi → Akses Khusus → Akses Data Penggunaan → FocusSholat → Aktifkan
```
> ⚠️ Ini untuk deteksi aplikasi aktif sebagai fallback

### 3. Izinkan Notifikasi
- Tap "Izinkan" saat muncul dialog notifikasi
- Atau: Pengaturan → Aplikasi → FocusSholat → Notifikasi → Aktifkan

### 4. Tambahkan Aplikasi yang Ingin Diblokir
- Buka tab "Aplikasi" di FocusSholat
- Toggle ON untuk setiap aplikasi yang ingin diblokir (contoh: Instagram, TikTok, Twitter)

### 5. Atur Lokasi
- Di tab "Pengaturan": pilih GPS atau masukkan nama kota (contoh: "Jakarta")
- Waktu sholat akan otomatis diunduh

## Arsitektur Teknis

```
FocusSholat/
├── AccessibilityService (BlockingService)
│   ├── Mendeteksi TYPE_WINDOW_STATE_CHANGED events
│   ├── Cek apakah app yang aktif ada di daftar blokir
│   └── Tampilkan BlockScreenActivity jika diblokir
│
├── UsageStatsManager (polling setiap 1.5 detik)
│   └── Fallback jika event accessibility tidak trigger
│
├── WorkManager (PrayerTimeWorker)
│   ├── Fetch jadwal sholat dari Aladhan API setiap 6 jam
│   └── Simpan ke Room database
│
├── AlarmManager
│   ├── Pengingat sebelum waktu sholat
│   └── End timer untuk Emergency Override
│
└── Room Database
    ├── blocked_apps - daftar app yang diblokir/whitelist
    ├── custom_schedules - jadwal kustom
    ├── prayer_times - jadwal sholat dari API
    ├── block_events - log percobaan membuka app
    ├── focus_sessions - sesi fokus
    └── emergency_overrides - override darurat aktif
```

## Mekanisme Pemblokiran

1. **AccessibilityService** (utama): Menerima event setiap kali window/app berubah
2. **UsageStatsManager polling** (fallback): Mengecek foreground app setiap 1.5 detik
3. Ketika app terdeteksi dan seharusnya diblokir:
   - Cek whitelist → jika ada, lewati
   - Cek emergency override aktif → jika ada, lewati
   - Cek apakah dalam waktu sholat atau jadwal kustom
   - Jika semua kondisi terpenuhi → tampilkan `BlockScreenActivity`

## Konfigurasi API

Menggunakan Aladhan API:
- **Base URL**: `https://api.aladhan.com/`
- **Method**: 11 (Kementerian Agama Indonesia)
- **Endpoint by GPS**: `GET /v1/timings?latitude={lat}&longitude={lon}&method=11`
- **Endpoint by City**: `GET /v1/timingsByCity?city={kota}&country=Indonesia&method=11`

Tidak memerlukan API key - gratis.

## Perizinan yang Diperlukan

| Permission | Tujuan |
|-----------|--------|
| PACKAGE_USAGE_STATS | Mendeteksi app yang sedang berjalan |
| BIND_ACCESSIBILITY_SERVICE | Mendeteksi perubahan app aktif |
| INTERNET | Download jadwal sholat |
| ACCESS_FINE_LOCATION | GPS untuk lokasi otomatis |
| POST_NOTIFICATIONS | Notifikasi pengingat sholat |
| RECEIVE_BOOT_COMPLETED | Auto-start saat HP restart |
| FOREGROUND_SERVICE | Layanan latar belakang |

## Troubleshooting

**Pemblokiran tidak berfungsi:**
- Pastikan Accessibility Service sudah aktif
- Pastikan izin Usage Access sudah diberikan
- Beberapa HP (Xiaomi, OPPO, Vivo) butuh izin tambahan di "Auto-start" atau "Battery Saver"

**Waktu sholat tidak muncul:**
- Pastikan koneksi internet aktif
- Coba refresh dengan menekan "Perbarui" di beranda
- Cek apakah nama kota sudah benar di Pengaturan

**HP mati dan app tidak jalan:**
- Pastikan FocusSholat tidak di-force stop
- Di Pengaturan HP → Baterai → tambahkan FocusSholat ke "aplikasi yang tidak dioptimalkan"

## Catatan untuk Developer

### Menambah Metode Kalkulasi Sholat
Edit `AladhanApi.kt`, ubah parameter `method`:
- 1 = Muslim World League
- 3 = Egyptian General Authority of Survey  
- 11 = Kementerian Agama Indonesia (default)
- 15 = Spiritual Administration of Muslims of Russia

### Menyesuaikan Durasi Blokir Default
Edit `AppPreferences.kt`:
```kotlin
val KEY_PRAYER_DURATION_MINUTES = intPreferencesKey("prayer_duration_minutes")
// Ubah default 30 jika diperlukan
.map { it[KEY_PRAYER_DURATION_MINUTES] ?: 30 }
```

## Versi
- **App Version**: 1.0.0
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **Architecture**: MVVM + Repository + Hilt
