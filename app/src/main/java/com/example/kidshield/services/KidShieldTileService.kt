package com.example.kidshield.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.app.AppOpsManager
import android.os.Process
import com.example.kidshield.MainActivity

class KidShieldTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        
        if (!checkUsageStatsPermission()) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivityAndCollapse(intent)
            return
        }

        val isActive = KidShieldService.isRunning.value
        
        if (isActive) {
            // Service is ON, require auth to turn it off
            val intent = Intent(this, MainActivity::class.java).apply {
                action = "ACTION_TURN_OFF_SERVICE"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("REQUIRE_PIN", true)
            }
            if (Build.VERSION.SDK_INT >= 34) { // UPSIDE_DOWN_CAKE
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                startActivityAndCollapse(intent)
            }
        } else {
            // Start service (no auth required to turn ON)
            val intent = Intent(this, KidShieldService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            KidShieldService.isRunning.value = true
        }
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        if (KidShieldService.isRunning.value) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "KidShield (On)"
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "KidShield (Off)"
        }
        tile.updateTile()
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
