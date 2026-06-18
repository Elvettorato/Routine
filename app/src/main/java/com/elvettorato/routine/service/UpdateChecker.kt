package com.elvettorato.routine.service

import android.util.Log
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val API_URL = "https://api.github.com/repos/Elvettorato/Routine/releases/latest"

    fun check(currentVersion: String): UpdateInfo? {
        return try {
            val response = URL(API_URL).openConnection().let {
                it as HttpURLConnection
                it.setRequestProperty("Accept", "application/vnd.github.v3+json")
                it.connectTimeout = 8000
                it.readTimeout = 8000
                it.inputStream.bufferedReader().readText()
            }
            val release = Gson().fromJson(response, ReleaseJson::class.java)
            val latest = release.tagName.trimStart('v')
            val downloadUrl = release.htmlUrl
            val isAvailable = compareVersions(latest, currentVersion.trimStart('v')) > 0
            UpdateInfo(latest, downloadUrl, isAvailable)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check for updates: ${e.message}")
            null
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val partsA = a.split(".").map { it.toIntOrNull() ?: 0 }
        val partsB = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(partsA.size, partsB.size)) {
            val diff = (partsA.getOrElse(i) { 0 }) - (partsB.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }

    private data class ReleaseJson(
        @com.google.gson.annotations.SerializedName("tag_name") val tagName: String,
        @com.google.gson.annotations.SerializedName("html_url") val htmlUrl: String
    )
}
