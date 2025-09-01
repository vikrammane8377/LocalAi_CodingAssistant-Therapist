package com.example.programmingailocal.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "Downloader"

/**
 * Simple (blocking) download helper that streams a URL into the model directory inside
 * externalFilesDir/<modelName>/<version>/file.
 */
suspend fun downloadModelFile(
    context: Context,
    fileUrl: String,
    modelDir: String,
    fileName: String,
    onProgress: (received: Long, total: Long) -> Unit = { _, _ -> },
): Boolean = withContext(Dispatchers.IO) {
    try {
        val url = URL(fileUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "HTTP error ${connection.responseCode}")
            return@withContext false
        }

        val total = connection.contentLengthLong.takeIf { it > 0 } ?: -1
        val dir = File(context.getExternalFilesDir(null), modelDir)
        if (!dir.exists()) dir.mkdirs()
        val dest = File(dir, fileName)

        connection.inputStream.use { input ->
            FileOutputStream(dest).use { output ->
                val buffer = ByteArray(8 * 1024)
                var bytesRead: Int
                var downloaded = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    onProgress(downloaded, total)
                }
            }
        }
        true
    } catch (e: Exception) {
        Log.e(TAG, "Download error", e)
        false
    }
} 