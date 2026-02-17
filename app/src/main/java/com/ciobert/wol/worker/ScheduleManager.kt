package com.ciobert.wol.worker

import android.content.Context
import android.util.Log
import com.ciobert.wol.data.local.entity.PcEntity
import com.ciobert.wol.data.local.entity.ScheduleEntity
import com.ciobert.wol.util.PermissionsHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    
    companion object {
        private const val TAG = "ScheduleManager"
    }


    fun scheduleNextWake(pc: PcEntity, scheduleId: String, hour: Int, minute: Int, daysBitmap: Int) {
        // Check if we have the necessary permissions
        if (!PermissionsHelper.canScheduleExactAlarms(context)) {
            Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
            // Still attempt to schedule, but it might fail or be inexact
        }
        
        // Best effort: try to cancel any legacy alarm that might exist for this time slot
        // This ensures that after an update, we don't have double alarms firing
        cancelLegacySchedule(pc.id, hour, minute)

        val nextWakeTime = calculateNextWakeTime(hour, minute, daysBitmap)
        
        if (nextWakeTime == -1L) {
            Log.e(TAG, "Failed to calculate next wake time for PC: ${pc.name}")
            return
        }
        
        Log.d(TAG, "Scheduling alarm for ${pc.name} (Schedule: $scheduleId) at ${java.util.Date(nextWakeTime)}")
        
        val intent = android.content.Intent(context, com.ciobert.wol.receiver.WakeReceiver::class.java).apply {
            putExtra("pcId", pc.id)
            putExtra("scheduleId", scheduleId) // NEW: Pass the schedule ID
            putExtra("name", pc.name)
            putExtra("mac", pc.mac)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("daysBitmap", daysBitmap)
        }

        // NEW: specific request code for this schedule ID
        val requestCode = getRequestCode(scheduleId)
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // Use setExactAndAllowWhileIdle for reliability in Doze mode
        // Must handle SecurityException on Android 12+ if SCHEDULE_EXACT_ALARM is not granted
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    nextWakeTime,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled with setExactAndAllowWhileIdle for ${pc.name}")
            } else {
                 alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    nextWakeTime,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled with setExact for ${pc.name}")
            }
        } catch (e: SecurityException) {
            // Fallback for permission errors: use inexact alarm or just standard set
            Log.e(TAG, "SecurityException scheduling alarm, falling back to inexact", e)
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                nextWakeTime,
                pendingIntent
            )
        }
    }

    private fun calculateNextWakeTime(hour: Int, minute: Int, daysBitmap: Int): Long {
        val now = System.currentTimeMillis()
        val candidate = Calendar.getInstance()
        candidate.set(Calendar.HOUR_OF_DAY, hour)
        candidate.set(Calendar.MINUTE, minute)
        candidate.set(Calendar.SECOND, 0)
        candidate.set(Calendar.MILLISECOND, 0)
        
        // If the time today is already passed, start checking from tomorrow
        // We add a small buffer (e.g. 1 sec) to avoid immediate scheduling loops if called right at the minute
        if (candidate.timeInMillis <= now + 1000) {
            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        for (i in 0..7) { // Check next 7 days
            val dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK)
            // Map Calendar (Sun=1...Sat=7) to BitIdx (Mon=0...Sun=6)
            val bitIndex = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
            
            if (isBitSet(daysBitmap, bitIndex)) {
                return candidate.timeInMillis
            }
            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return -1 
    }
    
    private fun isBitSet(bitmap: Int, index: Int): Boolean {
        return (bitmap and (1 shl index)) != 0
    }

    fun cancelSchedule(scheduleId: String) {
        val requestCode = getRequestCode(scheduleId)
        val intent = android.content.Intent(context, com.ciobert.wol.receiver.WakeReceiver::class.java)
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled alarm for schedule: $scheduleId")
        }
    }

    /**
     * Tries to cancel a legacy alarm that was scheduled using the old (pcId + hour + minute) request code generation.
     */
    private fun cancelLegacySchedule(pcId: String, hour: Int, minute: Int) {
        val legacyRequestCode = getLegacyRequestCode(pcId, hour, minute)
        val intent = android.content.Intent(context, com.ciobert.wol.receiver.WakeReceiver::class.java)
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            legacyRequestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Legacy alarm cancelled for $pcId at $hour:$minute")
        }
    }
    
    fun cancelAllSchedulesForPc(schedules: List<ScheduleEntity>) {
        schedules.forEach { schedule ->
            cancelSchedule(schedule.id)
            // Also try to cancel legacy just in case
            cancelLegacySchedule(schedule.pcId, schedule.timeHour, schedule.timeMinute)
        }
        Log.d(TAG, "All alarms cancelled for PC: ${schedules.firstOrNull()?.pcId}")
    }
    
    private fun getRequestCode(scheduleId: String): Int {
        return scheduleId.hashCode()
    }

    private fun getLegacyRequestCode(pcId: String, hour: Int, minute: Int): Int {
        return pcId.hashCode() * 31 + (hour * 60 + minute)
    }
}
