package com.example.kidshield.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidshield.data.SettingsRepository
import kotlinx.coroutines.launch

@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val settingsRepository = remember { if (!isPreview) SettingsRepository(context) else null }
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF311B92))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = com.example.kidshield.R.drawable.ic_launcher_foreground),
            contentDescription = "KidShield Logo",
            modifier = Modifier.size(100.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Welcome to KidShield", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        if (step == 1) {
            Text("Create a 4-digit master PIN.", color = Color.LightGray, textAlign = TextAlign.Center)
            Text("This will be used to unlock the dashboard and disable the protection.", color = Color.LightGray, textAlign = TextAlign.Center, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { 
                    if (it.length <= 4) pin = it
                    error = ""
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (pin.length == 4) {
                        step = 2
                    } else {
                        error = "PIN must be 4 digits"
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Next", color = Color(0xFF311B92), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text("Confirm your 4-digit PIN.", color = Color.LightGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { 
                    if (it.length <= 4) confirmPin = it
                    error = ""
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray
                ),
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (confirmPin == pin) {
                        scope.launch {
                            settingsRepository?.savePin(pin)
                            onSetupComplete()
                        }
                    } else {
                        error = "PINs do not match"
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f).height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Complete Setup", color = Color(0xFF311B92), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    SetupScreen(onSetupComplete = {})
}
