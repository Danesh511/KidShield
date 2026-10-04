package com.example.kidshield.services

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import com.example.kidshield.data.SettingsRepository
import com.example.kidshield.ui.LockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppMonitor(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val settingsRepository = SettingsRepository(context)
    private var monitorJob: Job? = null
    
    private var allowedApps = setOf(
        context.packageName,
        "com.android.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher"
    )

    init {
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository.allowedAppsFlow.collect { savedApps ->
                allowedApps = savedApps + setOf(
                    context.packageName,
                    "com.android.launcher",
                    "com.google.android.apps.nexuslauncher",
                    "com.sec.android.app.launcher"
                )
            }
        }
    }

    fun startMonitoring() {
        if (monitorJob?.isActive == true) return
        
        monitorJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                checkForegroundApp()
                delay(1000) // Poll every 1 second
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun checkForegroundApp() {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 2000 // Look back 2 seconds

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var currentForegroundPackage: String? = null

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentForegroundPackage = event.packageName
            }
        }

        currentForegroundPackage?.let { packageName ->
            if (!allowedApps.contains(packageName) && packageName != context.packageName) {
                showLockScreen(packageName)
            }
        }
    }

    private fun showLockScreen(blockedPackage: String) {
        val intent = Intent(context, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("BLOCKED_PACKAGE", blockedPackage)
        }
        context.startActivity(intent)
    }
}
