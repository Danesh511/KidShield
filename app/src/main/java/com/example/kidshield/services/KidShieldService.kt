package com.example.kidshield.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.kidshield.R
import kotlinx.coroutines.flow.MutableStateFlow

class KidShieldService : Service() {

    companion object {
        val isRunning = MutableStateFlow(false)
    }

    private lateinit var appMonitor: AppMonitor

    override fun onCreate() {
        super.onCreate()
        isRunning.value = true
        appMonitor = AppMonitor(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "KidShieldChannel")
            .setContentTitle("KidShield Active")
            .setContentText("Child safe mode is currently enabled.")
            .setSmallIcon(R.drawable.ic_shield)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
        
        appMonitor.startMonitoring()
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning.value = false
        appMonitor.stopMonitoring()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "KidShieldChannel",
                "KidShield Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
