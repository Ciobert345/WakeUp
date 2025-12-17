package com.gemini.wol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.gemini.wol.worker.WakeWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// @AndroidEntryPoint removed as we use WorkManager.getInstance() directly
class WakeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pcId = intent.getStringExtra("pcId") ?: return
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val daysBitmap = intent.getIntExtra("daysBitmap", -1)

        val data = Data.Builder()
            .putString("pcId", pcId)
            .putInt("hour", hour)
            .putInt("minute", minute)
            .putInt("daysBitmap", daysBitmap)
            .build()

        // Create an Expedited WorkRequest to run immediately
        val workRequest = OneTimeWorkRequest.Builder(WakeWorker::class.java)
            .setInputData(data)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .addTag("wake_execute_${pcId}")
            .build()

        // Enqueue unique work to ensure we don't pile up if something goes weird, 
        // though typically this receiver fires once per schedule.
        // We use REPLACE because if a previous one is stuck, we want this new one (new time) to run.
        WorkManager.getInstance(context).enqueueUniqueWork(
            "wake_execute_${pcId}_${hour}_${minute}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
