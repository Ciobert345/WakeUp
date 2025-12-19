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
        return canScheduleExactAlarms(context) && isIgnoringBatteryOptimizations(context)
    }

    /**
     * Gets a user-friendly message explaining which permissions are missing.
     */
    fun getMissingPermissionsMessage(context: Context): String? {
        val missing = mutableListOf<String>()
        
        if (!canScheduleExactAlarms(context)) {
            missing.add("permesso allarmi esatti")
        }
        
        if (!isIgnoringBatteryOptimizations(context)) {
            missing.add("esenzione ottimizzazioni batteria")
        }
        
        return if (missing.isEmpty()) {
            null
        } else {
            "Per garantire l'esecuzione puntuale delle task programmate, è necessario concedere: ${missing.joinToString(", ")}"
        }
    }
}
