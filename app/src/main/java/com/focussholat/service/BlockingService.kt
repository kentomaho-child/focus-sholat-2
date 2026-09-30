package com.focussholat.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.focussholat.data.entity.PrayerTime
import com.focussholat.data.repository.*
import com.focussholat.ui.block.BlockScreenActivity
import com.focussholat.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BlockingService : AccessibilityService() {

    @Inject lateinit var blockedAppRepository: BlockedAppRepository
    @Inject lateinit var prayerTimeRepository: PrayerTimeRepository
    @Inject lateinit var customScheduleRepository: CustomScheduleRepository
    @Inject lateinit var emergencyOverrideRepository: EmergencyOverrideRepository
    @Inject lateinit var statsRepository: StatsRepository
    @Inject lateinit var appPreferences: AppPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private var blockedPackages: Set<String> = emptySet()
    private var whitelistedPackages: Set<String> = emptySet()
    private var currentOverriddenPackages: Set<String> = emptySet()
    private var isBlockingEnabled = false
    private var isManualBlocking = false
    private var isPrayerBlockingEnabled = true
    private var isInPrayerTime = false
    private var isInCustomSchedule = false
    private var currentPrayerSessionId: Long = -1
    private var currentPrayerTimeInfo: PrayerTimeInfo? = null
    private var prayerDurationMinutes = 30
    private var enabledPrayers: Map<String, Boolean> = emptyMap()

    private val usageStatsManager: UsageStatsManager by lazy {
        getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    // Polling runnable - checks foreground app every second via UsageStats as fallback
    private val pollRunnable = object : Runnable {
        override fun run() {
            checkAndBlockCurrentApp()
            handler.postDelayed(this, 1500)
        }
    }

    // Refresh state every minute
    private val stateRefreshRunnable = object : Runnable {
        override fun run() {
            serviceScope.launch { refreshState() }
            // Re-read schedules frequently so a newly saved manual window becomes active
            // without waiting for the next minute boundary or restarting Accessibility.
            handler.postDelayed(this, 5 * 1000)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info

        // AccessibilityService is already managed by Android. Do not call startForeground()
        // here: on Android 14+ that requires a declared foreground-service type and can
        // crash immediately when the user enables Accessibility.
        serviceScope.launch { refreshState() }
        handler.post(pollRunnable)
        handler.post(stateRefreshRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName.isNotEmpty()) {
                checkAndBlock(packageName)
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(pollRunnable)
        handler.removeCallbacks(stateRefreshRunnable)
        serviceScope.cancel()
    }

    private fun checkAndBlockCurrentApp() {
        try {
            val now = System.currentTimeMillis()
            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 5000,
                now
            )
            if (usageStats.isNullOrEmpty()) return

            val topApp = usageStats.maxByOrNull { it.lastTimeUsed }
            topApp?.packageName?.let { pkg ->
                if (pkg != packageName && pkg.isNotEmpty()) {
                    checkAndBlock(pkg)
                }
            }
        } catch (e: Exception) {
            // UsageStats not available
        }
    }

    private fun checkAndBlock(packageName: String) {
        if (!isBlockingEnabled) return
        if (packageName == this.packageName) return
        if (packageName in whitelistedPackages) return
        if (packageName in currentOverriddenPackages) return

        val shouldBlock = packageName in blockedPackages &&
            PrayerTimeCalculator.shouldBlock(
                blockingEnabled = isBlockingEnabled,
                manualBlocking = isManualBlocking,
                prayerTimeActive = isInPrayerTime,
                customScheduleActive = isInCustomSchedule
            )

        if (shouldBlock) {
            serviceScope.launch {
                // Log the block attempt
                val appName = blockedPackages.contains(packageName).let {
                    getAppName(packageName)
                }
                val reason = when {
                    isManualBlocking -> "MANUAL"
                    isInPrayerTime -> "PRAYER_TIME"
                    else -> "CUSTOM_SCHEDULE"
                }
                statsRepository.logBlockEvent(packageName, appName, reason)
            }
            showBlockScreen(packageName)
        }
    }

    private fun showBlockScreen(blockedPackage: String) {
        val intent = Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(BlockScreenActivity.EXTRA_BLOCKED_PACKAGE, blockedPackage)
            putExtra(BlockScreenActivity.EXTRA_PRAYER_NAME,
                currentPrayerTimeInfo?.displayName ?: "")
            putExtra(BlockScreenActivity.EXTRA_IS_PRAYER_TIME, isInPrayerTime)
        }
        startActivity(intent)
    }

    private suspend fun refreshState() {
        // Never keep a stale blocking state when a database read temporarily fails.
        isBlockingEnabled = false
        isManualBlocking = false
        isInPrayerTime = false
        isInCustomSchedule = false
        try {
            isBlockingEnabled = appPreferences.blockingEnabled.first()
            isManualBlocking = appPreferences.manualBlockingEnabled.first()
            isPrayerBlockingEnabled = appPreferences.prayerBlockingEnabled.first()
            prayerDurationMinutes = appPreferences.prayerDurationMinutes.first()
            val locationTimeZone = TimeZone.getTimeZone(appPreferences.timezone.first())

            enabledPrayers = mapOf(
                "fajr" to appPreferences.fajrEnabled.first(),
                "dhuhr" to appPreferences.dhuhrEnabled.first(),
                "asr" to appPreferences.asrEnabled.first(),
                "maghrib" to appPreferences.maghribEnabled.first(),
                "isha" to appPreferences.ishaEnabled.first()
            )

            blockedPackages = blockedAppRepository.getActiveBlockedPackageNames().toSet()
            whitelistedPackages = blockedAppRepository.getWhitelistedPackageNames().toSet()

            // Update overrides
            emergencyOverrideRepository.deactivateExpiredOverrides()
            val activeOverrides = emergencyOverrideRepository.getActiveOverrides().first()
            currentOverriddenPackages = activeOverrides.map { it.packageName }.toSet()

            // Check prayer time
            if (isPrayerBlockingEnabled) {
                val todayStr = dateFormat.format(Date())
                val todayPrayerTime = prayerTimeRepository.getTodayPrayerTime()
                if (todayPrayerTime != null) {
                    val prayerInfo = PrayerTimeCalculator.getCurrentPrayerAt(
                        todayPrayerTime,
                        prayerDurationMinutes,
                        enabledPrayers,
                        Calendar.getInstance(locationTimeZone)
                    )
                    val wasInPrayer = isInPrayerTime
                    isInPrayerTime = prayerInfo != null
                    currentPrayerTimeInfo = prayerInfo

                    // Track focus session
                    if (isInPrayerTime && !wasInPrayer && prayerInfo != null) {
                        val sessionId = statsRepository.startFocusSession(
                            "PRAYER_${prayerInfo.key.uppercase()}",
                            prayerInfo.displayName
                        )
                        currentPrayerSessionId = sessionId
                    } else if (!isInPrayerTime && wasInPrayer && currentPrayerSessionId != -1L) {
                        statsRepository.completeSession(
                            currentPrayerSessionId,
                            System.currentTimeMillis() - (prayerDurationMinutes * 60 * 1000L)
                        )
                        currentPrayerSessionId = -1
                    }
                }
            } else {
                isInPrayerTime = false
            }

            // Check custom schedules
            val schedules = customScheduleRepository.getActiveSchedulesList()
            isInCustomSchedule = schedules.any { schedule ->
                PrayerTimeCalculator.isScheduleActiveAt(
                    Calendar.getInstance(locationTimeZone),
                    schedule.startHour, schedule.startMinute,
                    schedule.endHour, schedule.endMinute,
                    schedule.daysOfWeek
                )
            }
        } catch (e: Exception) {
            // Error refreshing state - keep current state
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    companion object {
        fun isRunning(context: Context): Boolean {
            // Check via accessibility service enabled state
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
            return am?.isEnabled == true
        }
    }
}
