package com.ciobert.wol.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.ciobert.wol.receiver.UpdateDownloadReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var downloadId: Long = -1


    fun downloadAndInstall(url: String, fileName: String) {
        android.util.Log.d("UpdateManager", "downloadAndInstall called: url=$url, fileName=$fileName")
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("WakeUp Update")
            .setDescription("Downloading version $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
        android.util.Log.d("UpdateManager", "Download enqueued with ID: $downloadId")

        // Start progress monitoring
        startProgressMonitoring(downloadManager, downloadId, fileName)

        // Register receiver for download complete
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                android.util.Log.d("UpdateManager", "Download complete broadcast received")
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                android.util.Log.d("UpdateManager", "Download ID from broadcast: $id, expected: $downloadId")
                if (downloadId == id) {
                    android.util.Log.d("UpdateManager", "Download ID matches, calling installApk")
                    // Cancel progress notification
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    notificationManager.cancel(998) // Progress notification ID
                    
                    installApk(fileName)
                    try {
                        context.unregisterReceiver(this)
                        android.util.Log.d("UpdateManager", "Receiver unregistered successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("UpdateManager", "Error unregistering receiver", e)
                    }
                }
            }
        }
        
        ContextCompat.registerReceiver(
            context, 
            onComplete, 
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        android.util.Log.d("UpdateManager", "Download complete receiver registered")
    }

    private fun startProgressMonitoring(downloadManager: DownloadManager, downloadId: Long, fileName: String) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val progressRunnable = object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    
                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val bytesTotal = cursor.getLong(bytesTotalIndex)
                    val status = cursor.getInt(statusIndex)
                    
                    android.util.Log.d("UpdateManager", "Progress: $bytesDownloaded/$bytesTotal, Status: $status")
                    
                    if (bytesTotal > 0) {
                        val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                        updateProgressNotification(progress, fileName)
                    }
                    
                    // Check if download is complete
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        android.util.Log.d("UpdateManager", "Download completed successfully via progress monitor")
                        // Cancel progress notification
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.cancel(998)
                        
                        // Trigger installation
                        installApk(fileName)
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        android.util.Log.e("UpdateManager", "Download failed!")
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                        notificationManager.cancel(998)
                    } else if (status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING) {
                        // Continue monitoring if download is still in progress
                        handler.postDelayed(this, 500) // Update every 500ms
                    }
                }
                cursor.close()
            }
        }
        handler.post(progressRunnable)
    }

    private fun updateProgressNotification(progress: Int, fileName: String) {
        val notification = androidx.core.app.NotificationCompat.Builder(context, "wol_channel")
            .setSmallIcon(com.ciobert.wol.R.mipmap.ic_launcher)
            .setContentTitle("Download aggiornamento")
            .setContentText("$fileName - $progress%")
            .setProgress(100, progress, false)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(998, notification) // Different ID from the update available notification
    }

    fun showUpdateNotification(version: String, downloadUrl: String) {
        android.util.Log.d("UpdateManager", "showUpdateNotification called for version: $version")
        
        // Create a broadcast intent for download action
        val downloadIntent = Intent(context, UpdateDownloadReceiver::class.java).apply {
            action = "com.ciobert.wol.ACTION_DOWNLOAD_UPDATE"
            putExtra("download_url", downloadUrl)
            putExtra("version", version)
        }
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            0,
            downloadIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, "wol_channel")
            .setSmallIcon(com.ciobert.wol.R.mipmap.ic_launcher)
            .setContentTitle("Aggiornamento Disponibile")
            .setContentText("Versione $version pronta per il download. Clicca per scaricare.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        android.util.Log.d("UpdateManager", "Posting notification with ID 999")
        notificationManager.notify(999, notification)
        android.util.Log.d("UpdateManager", "Notification posted successfully")
    }

    private fun installApk(fileName: String) {
        android.util.Log.d("UpdateManager", "installApk called for: $fileName")
        try {
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            android.util.Log.d("UpdateManager", "APK file path: ${file.absolutePath}")
            android.util.Log.d("UpdateManager", "File exists: ${file.exists()}, Size: ${file.length()} bytes")
            
            if (!file.exists()) {
                android.util.Log.e("UpdateManager", "APK file does not exist!")
                return
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            android.util.Log.d("UpdateManager", "FileProvider URI: $uri")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            
            android.util.Log.d("UpdateManager", "Starting install activity")
            context.startActivity(intent)
            android.util.Log.d("UpdateManager", "Install activity started successfully")
        } catch (e: Exception) {
            android.util.Log.e("UpdateManager", "Error installing APK", e)
            e.printStackTrace()
        }
    }
}
