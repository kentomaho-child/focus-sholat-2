package com.focussholat.di

import android.content.Context
import androidx.room.Room
import com.focussholat.data.dao.*
import com.focussholat.data.db.FocusSholatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FocusSholatDatabase {
        return Room.databaseBuilder(
            context,
            FocusSholatDatabase::class.java,
            "focussholat.db"
        ).build()
    }

    @Provides
    fun provideBlockedAppDao(db: FocusSholatDatabase): BlockedAppDao = db.blockedAppDao()

    @Provides
    fun provideCustomScheduleDao(db: FocusSholatDatabase): CustomScheduleDao = db.customScheduleDao()

    @Provides
    fun providePrayerTimeDao(db: FocusSholatDatabase): PrayerTimeDao = db.prayerTimeDao()

    @Provides
    fun provideBlockEventDao(db: FocusSholatDatabase): BlockEventDao = db.blockEventDao()

    @Provides
    fun provideFocusSessionDao(db: FocusSholatDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideEmergencyOverrideDao(db: FocusSholatDatabase): EmergencyOverrideDao = db.emergencyOverrideDao()
}
