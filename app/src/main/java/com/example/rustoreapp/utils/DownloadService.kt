package com.example.rustoreapp.utils

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File

class DownloadService : Service() {

    private var downloadId: Long = -1
    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private val notificationId = 1001
    private val channelId = "download_channel"

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_APP_NAME = "extra_app_name"
    }

    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                val query = DownloadManager.Query()
                query.setFilterById(id)

                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val statusColumn = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusColumn)

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val uriColumn = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val uriString = cursor.getString(uriColumn)

                        uriString?.let {
                            try {
                                val apkUri = Uri.parse(it)
                                val apkFile = File(apkUri.path ?: "")

                                if (apkFile.exists() && apkFile.length() > 0) {
                                    // Обновляем уведомление
                                    updateNotification("Установка ${intent.getStringExtra(EXTRA_APP_NAME)}...")

                                    // Устанавливаем APK
                                    AppInstaller.installApk(applicationContext, apkFile)
                                } else {
                                    showToast("Файл не найден или поврежден")
                                }
                            } catch (e: Exception) {
                                showToast("Ошибка: ${e.message}")
                            }
                        }
                    } else {
                        val reasonColumn = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(reasonColumn)
                        showToast("Ошибка загрузки: код $reason")
                    }
                }
                cursor.close()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra(EXTRA_URL)?.let { url ->
            val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Приложение"
            startDownload(url, appName)
        }
        return START_NOT_STICKY
    }

    private fun startDownload(url: String, appName: String) {
        try {
            // Создаем уведомление
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Загрузка: $appName")
                .setContentText("Подготовка...")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            startForeground(notificationId, notification)

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Загрузка: $appName")
                .setDescription("Загрузка APK файла")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            // Создаем папку RuStore в Downloads если ее нет
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val rustoreDir = File(downloadsDir, "RuStore")
            if (!rustoreDir.exists()) {
                rustoreDir.mkdirs()
            }

            val fileName = "${appName.replace(" ", "_")}_${System.currentTimeMillis()}.apk"
            val destinationFile = File(rustoreDir, fileName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                request.setDestinationUri(Uri.fromFile(destinationFile))
            } else {
                @Suppress("DEPRECATION")
                request.setDestinationUri(Uri.fromFile(destinationFile))
            }

            downloadId = downloadManager.enqueue(request)

            updateNotification("Загрузка $appName...")

        } catch (e: Exception) {
            showToast("Ошибка начала загрузки: ${e.message}")
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Загрузка приложений",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о загрузке приложений"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RuStore")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showToast(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(onComplete)
        } catch (e: Exception) {
            // Игнорируем ошибку отмены регистрации
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}