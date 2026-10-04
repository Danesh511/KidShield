package com.example.kidshield.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidshield.MainActivity

class LockScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val blockedPackage = intent.getStringExtra("BLOCKED_PACKAGE") ?: "Unknown App"
        
        setContent {
            MaterialTheme {
                LockScreen(
                    blockedPackage = blockedPackage,
                    onGoHome = {
                        // Launch home screen
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onEnterPin = {
                        // Open main activity for PIN entry
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("REQUIRE_PIN", true)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
    
    // Prevent back button from bypassing lock screen
    override fun onBackPressed() {
        // Do nothing
    }
}

@Composable
fun LockScreen(blockedPackage: String, onGoHome: () -> Unit, onEnterPin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF673AB7)) // Purple background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops! \uD83D\uDEE1\uFE0F",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app ($blockedPackage) is currently locked.",
            fontSize = 20.sp,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Go Back to Safe Zone", color = Color(0xFF673AB7), fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onEnterPin) {
            Text("Parent Access (Enter PIN)", color = Color.LightGray)
        }
    }
}
