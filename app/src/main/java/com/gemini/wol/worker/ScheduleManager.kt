package com.gemini.wol.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.gemini.wol.data.local.entity.PcEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScheduleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

    fun scheduleNextWake(pc: PcEntity, hour: Int, minute: Int, daysBitmap: Int) {
        val nextWakeTime = calculateNextWakeTime(hour, minute, daysBitmap)
        
        val intent = android.content.Intent(context, com.gemini.wol.receiver.WakeReceiver::class.java).apply {
            putExtra("pcId", pc.id)
            putExtra("name", pc.name)
            putExtra("mac", pc.mac)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("daysBitmap", daysBitmap)
        }

        val requestCode = getRequestCode(pc.id, hour, minute)
        
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
            } else {
                 alarmManager.setExact(
                    android.app.AlarmManager.RTC_WAKEUP,
                    nextWakeTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback for permission errors: use inexact alarm or just standard set
            alarmManager.set(
                android.app.AlarmManager.RTC_WAKEUP,
                nextWakeTime,
                pendingIntent
            )
            e.printStackTrace()
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

    fun cancelSchedule(pcId: String, hour: Int, minute: Int) {
        val requestCode = getRequestCode(pcId, hour, minute)
        // Intent must match the one used for scheduling (filter, class, etc)
        val intent = android.content.Intent(context, com.gemini.wol.receiver.WakeReceiver::class.java)
        // Extras don't need to match for filterEquals, but action/data/component/categories do.
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
    
    private fun getRequestCode(pcId: String, hour: Int, minute: Int): Int {
        // Simple hash combination. 
        return pcId.hashCode() * 31 + (hour * 60 + minute)
    }
}
