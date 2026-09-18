package com.example.downloader

import android.content.Context
import com.example.data.local.TrackDao
import com.example.data.model.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class MusicDownloader(
    private val context: Context,
    private val trackDao: TrackDao
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // Map of trackId -> download progress percentage (0..100) or null if not downloading
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val downloadsDir: File
        get() {
            val dir = File(context.filesDir, "downloads")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    suspend fun downloadTrack(track: TrackEntity, onFinished: ((Boolean) -> Unit)? = null) = withContext(Dispatchers.IO) {
        // Set initial downloading state
        updateProgress(track.id, 5)

        try {
            val outputFile = File(downloadsDir, "${track.id}.mp3")
            if (outputFile.exists() && outputFile.length() > 0) {
                // Already downloaded
                trackDao.setDownloaded(
                    id = track.id,
                    downloaded = true,
                    localPath = outputFile.absolutePath,
                    size = outputFile.length()
                )
                clearProgress(track.id)
                onFinished?.invoke(true)
                return@withContext
            }

            val request = Request.Builder().url(track.audioUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                clearProgress(track.id)
                onFinished?.invoke(false)
                return@withContext
            }

            val body = response.body!!
            val totalBytes = body.contentLength()
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(8 * 1024)
            var bytesCopied: Long = 0
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                bytesCopied += read
                if (totalBytes > 0) {
                    val progress = ((bytesCopied * 100) / totalBytes).toInt().coerceIn(5, 99)
                    updateProgress(track.id, progress)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            // Update database
            trackDao.setDownloaded(
                id = track.id,
                downloaded = true,
                localPath = outputFile.absolutePath,
                size = outputFile.length()
            )
            updateProgress(track.id, 100)
            clearProgress(track.id)
            onFinished?.invoke(true)
        } catch (e: Exception) {
            e.printStackTrace()
            clearProgress(track.id)
            onFinished?.invoke(false)
        }
    }

    suspend fun removeDownload(track: TrackEntity) = withContext(Dispatchers.IO) {
        track.localAudioPath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        trackDao.setDownloaded(
            id = track.id,
            downloaded = false,
            localPath = null,
            size = 0L
        )
    }

    private fun updateProgress(trackId: String, progress: Int) {
        val current = _downloadProgress.value.toMutableMap()
        current[trackId] = progress
        _downloadProgress.value = current
    }

    private fun clearProgress(trackId: String) {
        val current = _downloadProgress.value.toMutableMap()
        current.remove(trackId)
        _downloadProgress.value = current
    }
}
