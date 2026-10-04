package com.example.kidshield.ui

import android.app.AppOpsManager
import android.app.StatusBarManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidshield.R
import com.example.kidshield.data.SettingsRepository
import com.example.kidshield.services.KidShieldService
import com.example.kidshield.services.KidShieldTileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val settingsRepository = remember { if (!isPreview) SettingsRepository(context) else null }
    val scope = rememberCoroutineScope()
    
    val allowedApps by (settingsRepository?.allowedAppsFlow ?: kotlinx.coroutines.flow.flowOf(emptySet())).collectAsState(initial = emptySet())
    val isServiceRunning by KidShieldService.isRunning.collectAsState()
    
    var hasUsageStatsPermission by remember { mutableStateOf(checkUsageStatsPermission(context)) }
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    var installedApps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingApps by remember { mutableStateOf(true) }
    
    var topUsageStats by remember { mutableStateOf<List<UsageItem>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    if (!isPreview) {
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasUsageStatsPermission = checkUsageStatsPermission(context)
                    hasOverlayPermission = Settings.canDrawOverlays(context)
                    refreshTrigger++ // Force refresh stats
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                
                val apps = resolveInfos
                    .filter { it.activityInfo.packageName != context.packageName }
                    .map { info ->
                        AppItem(
                            name = info.loadLabel(pm).toString(),
                            packageName = info.activityInfo.packageName
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.name }
                
                installedApps = apps
                isLoadingApps = false
            }
        } else {
            // Mock data for preview
            installedApps = listOf(AppItem("YouTube", "com.google.android.youtube"), AppItem("WhatsApp", "com.whatsapp"))
            isLoadingApps = false
        }
    }

    LaunchedEffect(hasUsageStatsPermission, allowedApps, refreshTrigger) {
        if (!isPreview && hasUsageStatsPermission && allowedApps.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                val startTime = calendar.timeInMillis
                
                val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                
                val pm = context.packageManager
                topUsageStats = stats
                    .filter { allowedApps.contains(it.packageName) && it.totalTimeInForeground > 60000 } // only allowed apps > 1 min
                    .sortedByDescending { it.totalTimeInForeground }
                    .take(3)
                    .map { stat ->
                        val name = try {
                            pm.getApplicationLabel(pm.getApplicationInfo(stat.packageName, 0)).toString()
                        } catch (e: Exception) { stat.packageName }
                        UsageItem(name, stat.totalTimeInForeground)
                    }
            }
        }
    }

    val filteredApps = if (searchQuery.isEmpty()) {
        installedApps
    } else {
        installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parental Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF311B92),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Status Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isServiceRunning) Color(0xFF4CAF50) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text(
                            text = if (isServiceRunning) "Protection is ON" else "Protection is OFF",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isServiceRunning) Color.White else Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toggle KidShield exclusively from your phone's Quick Settings Tile.",
                            color = if (isServiceRunning) Color(0xFFE8F5E9) else Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val statusBarManager = context.getSystemService(Context.STATUS_BAR_SERVICE) as StatusBarManager
                            statusBarManager.requestAddTileService(
                                ComponentName(context, KidShieldTileService::class.java),
                                "KidShield",
                                Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
                                ContextCompat.getMainExecutor(context),
                                java.util.function.Consumer { _ -> }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Quick Settings Tile to Phone")
                    }
                }
            }

            if (!hasUsageStatsPermission) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Permission Required", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("KidShield needs Usage Access to block apps.", color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) {
                                Text("Grant Usage Permission")
                            }
                        }
                    }
                }
            }
            
            if (!hasOverlayPermission) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Display Permission Required", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("KidShield needs 'Display over other apps' to show the lock screen.", color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            }) {
                                Text("Grant Overlay Permission")
                            }
                        }
                    }
                }
            }
            
            if (topUsageStats.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Today's Child Usage (Allowed Apps)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF311B92))
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            topUsageStats.forEach { stat ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stat.appName, fontWeight = FontWeight.SemiBold)
                                    val minutes = stat.timeMs / 60000
                                    Text("${minutes} mins", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text("App Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF311B92))
                Text("Select apps that are safe for your child to use.", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps...") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (isLoadingApps) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(filteredApps) { app ->
                    val isAllowed = allowedApps.contains(app.packageName)
                    AppListItem(
                        app = app,
                        isAllowed = isAllowed,
                        onToggle = { allowed ->
                            scope.launch {
                                if (allowed) {
                                    settingsRepository?.addAllowedApp(app.packageName)
                                } else {
                                    settingsRepository?.removeAllowedApp(app.packageName)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: AppItem, isAllowed: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(if (isAllowed) Icons.Default.Check else Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = app.packageName, fontSize = 12.sp, color = Color.Gray)
            }
            
            Switch(
                checked = isAllowed,
                onCheckedChange = { onToggle(it) }
            )
        }
    }
}

data class AppItem(
    val name: String,
    val packageName: String
)

data class UsageItem(
    val appName: String,
    val timeMs: Long
)

private fun checkUsageStatsPermission(context: Context): Boolean {
    try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        return false // Safe fallback for previews
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen()
}
