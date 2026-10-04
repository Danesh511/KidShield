package com.example.kidshield

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.kidshield.data.SettingsRepository
import com.example.kidshield.services.KidShieldService
import com.example.kidshield.theme.KidShieldTheme
import com.example.kidshield.ui.DashboardScreen
import com.example.kidshield.ui.PinScreen
import com.example.kidshield.ui.SetupScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val settingsRepository = SettingsRepository(this)
        val requirePin = intent.getBooleanExtra("REQUIRE_PIN", true)
        val actionTurnOff = intent.action == "ACTION_TURN_OFF_SERVICE"

        setContent {
            KidShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val hasSetup by settingsRepository.hasSetupFlow.collectAsState(initial = null)
                    val expectedPin by settingsRepository.pinFlow.collectAsState(initial = "")
                    
                    var isUnlocked by remember { mutableStateOf(!requirePin) }

                    // Only process logic when DataStore has finished loading
                    if (hasSetup != null) {
                        if (hasSetup == false) {
                            SetupScreen(onSetupComplete = {
                                // Once setup is done, unlock and show dashboard
                                isUnlocked = true
                            })
                        } else {
                            if (isUnlocked) {
                                if (actionTurnOff) {
                                    // Successfully authenticated to turn off service
                                    stopService(Intent(this@MainActivity, KidShieldService::class.java))
                                    KidShieldService.isRunning.value = false
                                    Toast.makeText(this@MainActivity, "KidShield Protection Disabled", Toast.LENGTH_SHORT).show()
                                    finish() // Close the activity
                                } else {
                                    DashboardScreen()
                                }
                            } else {
                                // Show PIN Screen as fallback/primary
                                PinScreen(
                                    expectedPin = expectedPin ?: "1234",
                                    onPinSuccess = {
                                        isUnlocked = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}
