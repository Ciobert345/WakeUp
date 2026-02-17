package com.ciobert.wol.data.repository

import android.util.Log
import com.ciobert.wol.data.model.GithubRelease
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor() {

    private val gson = Gson()
    private val repoOwner = "Ciobert345"
    private val repoName = "WakeUp"

    suspend fun checkForUpdate(currentVersion: String): GithubRelease? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            // Allow 404/403 for rate limits or no releases
            if (connection.responseCode != 200) {
                Log.w("UpdateRepository", "GitHub API returned ${connection.responseCode}")
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val release = gson.fromJson(response, GithubRelease::class.java)

            // clean versions (remove 'v' prefix if present)
            val remoteVersion = release.tagName.removePrefix("v")
            val localVersion = currentVersion.removePrefix("v")
            
            Log.d("UpdateRepository", "Remote: $remoteVersion, Local: $localVersion")

            if (isNewer(remoteVersion, localVersion)) {
                return@withContext release
            }
            return@withContext null

        } catch (e: Exception) {
            Log.e("UpdateRepository", "Error checking for updates", e)
            return@withContext null
        }
    }

    private fun isNewer(remote: String, local: String): Boolean {
        // Filter out empty strings from split (handles cases like ".5.0.0" or "5.0.0.")
        val remoteParts = remote.split(".").filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").filter { it.isNotEmpty() }.map { it.toIntOrNull() ?: 0 }
        
        val length = maxOf(remoteParts.size, localParts.size)
        
        for (i in 0 until length) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r > l) return true
            if (r < l) return false
        }
        return false
    }
}
