package com.example.rustoreapp.utils

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService

object DownloadManagerUtil {

    fun downloadApk(context: Context, url: String, appName: String): Long {
        val downloadManager = context.getSystemService<DownloadManager>() ?: return -1

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Загрузка: $appName")
            .setDescription("Загрузка APK файла")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${appName.replace(" ", "_")}_${System.currentTimeMillis()}.apk"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        return downloadManager.enqueue(request)
    }
}