package com.gemini.wol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gemini.wol.data.repository.PcRepository
import com.gemini.wol.data.repository.ScheduleRepository
import com.gemini.wol.worker.ScheduleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver that triggers on device boot to reschedule all active alarms.
 * This is essential because AlarmManager alarms are cleared on device reboot.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pcRepository: PcRepository

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var scheduleManager: ScheduleManager
    
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Boot completed, rescheduling alarms...")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get all enabled schedules
                val allSchedules = scheduleRepository.getAllSchedules().first()
                
                Log.d(TAG, "Found ${allSchedules.size} total schedules")
                
                var rescheduledCount = 0
                allSchedules.forEach { schedule ->
                    if (schedule.enabled) {
                        // Get the PC for this schedule
                        val pc = pcRepository.getPcById(schedule.pcId)
                        
                        if (pc != null) {
                            // Reschedule this alarm
                            scheduleManager.scheduleNextWake(
                                pc,
                                schedule.timeHour,
                                schedule.timeMinute,
                                schedule.daysBitmap
                            )
                            rescheduledCount++
                            Log.d(TAG, "Rescheduled alarm for ${pc.name} at ${schedule.timeHour}:${schedule.timeMinute}")
                        } else {
                            Log.w(TAG, "PC not found for schedule ${schedule.id}")
                        }
                    }
                }
                
                Log.d(TAG, "Rescheduled $rescheduledCount alarms")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling alarms after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
