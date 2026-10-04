package com.example.kidshield.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinScreen(expectedPin: String, onPinSuccess: () -> Unit) {
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    
    // Scramble numbers 0-9 for the keypad
    val scrambledNumbers = remember { (0..9).shuffled() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF311B92))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Parent Access", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your 4-digit PIN", color = Color.LightGray)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Custom PIN Indicator (dots)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (i in 0 until 4) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = if (i < enteredPin.length) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                ) {
                    if (i >= enteredPin.length) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                        ) {}
                    }
                }
            }
        }

        if (error) {
            Text("Invalid PIN", color = Color(0xFFFF5252), modifier = Modifier.padding(top = 16.dp))
        } else {
            Spacer(modifier = Modifier.height(36.dp)) // Reserve space so layout doesn't jump
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Scrambled Keypad
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Rows of 3
            for (row in 0..2) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    for (col in 0..2) {
                        val num = scrambledNumbers[row * 3 + col]
                        KeypadButton(num.toString()) {
                            if (enteredPin.length < 4) {
                                enteredPin += num
                                error = false
                                if (enteredPin.length == 4) {
                                    scope.launch {
                                        delay(150) // Tiny delay to see the last dot fill
                                        if (enteredPin == expectedPin) {
                                            onPinSuccess()
                                        } else {
                                            error = true
                                            enteredPin = "" // Reset
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Last row has the remaining number and a backspace/clear
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.size(72.dp)) // Empty space for alignment
                
                val lastNum = scrambledNumbers[9]
                KeypadButton(lastNum.toString()) {
                    if (enteredPin.length < 4) {
                        enteredPin += lastNum
                        error = false
                        if (enteredPin.length == 4) {
                            scope.launch {
                                delay(150)
                                if (enteredPin == expectedPin) {
                                    onPinSuccess()
                                } else {
                                    error = true
                                    enteredPin = ""
                                }
                            }
                        }
                    }
                }

                // Delete Button
                Button(
                    onClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            error = false
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("DEL", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun KeypadButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)), // Semi-transparent white
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun PinScreenPreview() {
    PinScreen(expectedPin = "1234", onPinSuccess = {})
}
