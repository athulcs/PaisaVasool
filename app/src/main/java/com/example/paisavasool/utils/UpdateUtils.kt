package com.example.paisavasool.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object UpdateUtils {
    private const val GITHUB_API_URL = "https://api.github.com/repos/athulcs/PaisaVasool/releases/latest"

    data class UpdateInfo(
        val latestVersion: String,
        val releaseUrl: String,
        val isUpdateAvailable: Boolean
    )

    suspend fun checkForUpdates(currentVersion: String): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latestTag = json.getString("tag_name").removePrefix("v")
                val releaseUrl = json.getString("html_url")

                return@withContext UpdateInfo(
                    latestVersion = latestTag,
                    releaseUrl = releaseUrl,
                    isUpdateAvailable = isVersionNewer(currentVersion, latestTag)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun isVersionNewer(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until length) {
            val curr = currentParts.getOrElse(i) { 0 }
            val late = latestParts.getOrElse(i) { 0 }
            if (late > curr) return true
            if (curr > late) return false
        }
        return false
    }

    fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
