package com.dreaminko.nashipomodoro.core.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateDownloader @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val downloadManager = context.getSystemService(DownloadManager::class.java)
    private var pendingDownloadId: Long? = null
    private var receiverRegistered = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (completedId != pendingDownloadId) return

            val query = DownloadManager.Query().setFilterById(completedId)
            downloadManager.query(query).use { cursor ->
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(
                        cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                    )
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        openInstaller(downloadManager.getUriForDownloadedFile(completedId))
                    }
                }
                pendingDownloadId = null
                unregisterReceiver()
            }
        }
    }

    fun download(update: AppUpdate): Long {
        registerReceiver()
        val request = DownloadManager.Request(Uri.parse(update.downloadUrl))
            .setTitle(update.fileName)
            .setDescription(update.releaseName)
            .setMimeType(APK_MIME_TYPE)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "${System.currentTimeMillis()}-${update.fileName.sanitizeFileName()}"
            )

        return downloadManager.enqueue(request).also { pendingDownloadId = it }
    }

    private fun registerReceiver() {
        if (receiverRegistered) return
        ContextCompat.registerReceiver(
            context,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
        receiverRegistered = true
    }

    private fun unregisterReceiver() {
        if (!receiverRegistered) return
        context.unregisterReceiver(downloadReceiver)
        receiverRegistered = false
    }

    private fun openInstaller(uri: Uri?) {
        if (uri == null) return
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, APK_MIME_TYPE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        )
    }

    private fun String.sanitizeFileName(): String =
        replace(Regex("""[^A-Za-z0-9._-]"""), "_")

    private companion object {
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"
    }
}
