package com.gemini.wol.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Utility class to handle permissions related to scheduling exact alarms
 * and battery optimization exemptions.
 */
object PermissionsHelper {

    /**
     * Checks if the app can schedule exact alarms.
     * On Android 12+ (API 31+), this requires explicit permission from user.
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            // On older versions, this permission is granted automatically
            true
        }
    }

    /**
     * Opens the system settings page where user can grant exact alarm permission.
     * Only relevant for Android 12+ (API 31+).
     */
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * Checks if the app is exempt from battery optimizations.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Opens the system settings page where user can disable battery optimization for this app.
     */
    fun requestBatteryOptimizationExemption(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * Checks if all required permissions for reliable scheduling are granted.
     */
    fun hasAllSchedulePermissions(context: Context): Boolean {
        return canScheduleExactAlarms(context) && 
               isIgnoringBatteryOptimizations(context) &&
               (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Data class representing a missing permission and how to fix it.
     */
    data class PermissionItem(
        val id: String,
        val title: String,
        val description: String,
        val action: () -> Unit
    )

    /**
     * Returns a list of missing permissions with details on how to request them.
     */
    fun getMissingPermissions(context: Context): List<PermissionItem> {
        val missing = mutableListOf<PermissionItem>()

        if (!canScheduleExactAlarms(context)) {
            missing.add(
                PermissionItem(
                    id = "exact_alarm",
                    title = "Allarmi Esatti",
                    description = "Necessario per inviare pacchetti WOL esattamente all'orario previsto.",
                    action = { requestExactAlarmPermission(context) }
                )
            )
        }

        if (!isIgnoringBatteryOptimizations(context)) {
            missing.add(
                PermissionItem(
                    id = "battery_optimization",
                    title = "Ottimizzazione Batteria",
                    description = "L'app deve essere esentata dalle restrizioni energetiche per non essere chiusa dal sistema.",
                    action = { requestBatteryOptimizationExemption(context) }
                )
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (!hasNotificationPermission) {
                missing.add(
                    PermissionItem(
                        id = "notifications",
                        title = "Notifiche",
                        description = "Ricevi conferme sull'invio dei pacchetti WOL quando l'app è chiusa o in background.",
                        action = { }
                    )
                )
            }
        }

        // OEM specific "Auto-start" or "Battery" settings
        val oemIntent = getOemSettingsIntent(context)
        if (oemIntent != null) {
            missing.add(
                PermissionItem(
                    id = "oem_optimization",
                    title = "Avvio Automatico",
                    description = "Alcuni produttori (Xiaomi, Samsung, etc.) chiudono l'app se non è attiva la gestione manuale dell'energia.",
                    action = { 
                        try {
                            context.startActivity(oemIntent)
                        } catch (e: Exception) {
                            // Fallback to standard battery optimization if OEM intent fails
                            requestBatteryOptimizationExemption(context)
                        }
                    }
                )
            )
        }

        return missing
    }

    private fun getOemSettingsIntent(context: Context): Intent? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val packageName = context.packageName
        
        val intents = when {
            manufacturer.contains("xiaomi") -> listOf(
                Intent().setComponent(android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
                Intent().setComponent(android.content.ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings"))
            )
            manufacturer.contains("samsung") -> listOf(
                Intent().setComponent(android.content.ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
                Intent().setComponent(android.content.ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.battery.BatteryActivity"))
            )
            manufacturer.contains("huawei") -> listOf(
                Intent().setComponent(android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
                Intent().setComponent(android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"))
            )
            manufacturer.contains("oneplus") -> listOf(
                Intent().setComponent(android.content.ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"))
            )
            else -> emptyList()
        }

        for (intent in intents) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context.packageManager.queryIntentActivities(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()) {
                return intent
            }
        }
        return null
    }

    /**
     * Gets a user-friendly message explaining which permissions are missing.
     */
    fun getMissingPermissionsMessage(context: Context): String? {
        val missing = getMissingPermissions(context).map { it.title }
        
        return if (missing.isEmpty()) {
            null
        } else {
            "Per garantire il corretto funzionamento in background, è consigliato concedere: ${missing.joinToString(", ")}"
        }
    }
}
