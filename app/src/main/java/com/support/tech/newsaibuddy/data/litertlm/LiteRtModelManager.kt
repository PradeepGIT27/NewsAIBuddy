package com.support.tech.newsaibuddy.data.litertlm

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages discovery, local storage, and downloading of .litertlm model files.
 */
@Singleton
class LiteRtModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Dedicated client for downloading large binary model files without logging or custom API key interceptors
    private val downloadClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val modelsDir: File
        get() = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }

    /**
     * Checks if any .litertlm model file exists in local storage.
     */
    fun findExistingModelFile(): File? {
        val inModelsDir = modelsDir.listFiles { file -> file.isFile && file.name.endsWith(".litertlm") }
        if (!inModelsDir.isNullOrEmpty()) {
            return inModelsDir.maxByOrNull { it.lastModified() }
        }

        val inFilesDir = context.filesDir.listFiles { file -> file.isFile && file.name.endsWith(".litertlm") }
        if (!inFilesDir.isNullOrEmpty()) {
            return inFilesDir.maxByOrNull { it.lastModified() }
        }

        return null
    }

    /**
     * Returns the target file destination for a given model.
     */
    fun getTargetModelFile(model: LiteRtModel): File {
        return File(modelsDir, model.filename)
    }

    /**
     * Downloads the specified model from Hugging Face into internal app storage.
     * Emits download progress percentage and bytes.
     */
    fun downloadModel(model: LiteRtModel): Flow<DownloadState> = flow {
        val targetFile = getTargetModelFile(model)
        val tempFile = File(modelsDir, "${model.filename}.tmp")

        emit(DownloadState.Downloading(0, 0, model.sizeBytes))

        try {
            Log.d(TAG, "Starting download for ${model.displayName} from ${model.downloadUrl}")
            val request = Request.Builder()
                .url(model.downloadUrl)
                .build()

            val response = downloadClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorMsg = "Download failed with HTTP ${response.code}: ${response.message}"
                Log.e(TAG, errorMsg)
                emit(DownloadState.Failed(errorMsg))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(DownloadState.Failed("Empty response body from server"))
                return@flow
            }

            val contentLength = if (body.contentLength() > 0) body.contentLength() else model.sizeBytes

            body.byteStream().use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(32768)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastEmittedProgress = 0

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val progress = if (contentLength > 0) {
                            ((totalRead * 100) / contentLength).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                        if (progress > lastEmittedProgress || totalRead == contentLength) {
                            lastEmittedProgress = progress
                            emit(DownloadState.Downloading(progress, totalRead, contentLength))
                        }
                    }
                    outputStream.flush()
                }
            }

            if (tempFile.renameTo(targetFile)) {
                Log.d(TAG, "Successfully downloaded model to ${targetFile.absolutePath}")
                emit(DownloadState.Completed(targetFile.absolutePath))
            } else {
                emit(DownloadState.Failed("Failed to finalize downloaded model file"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during model download", e)
            if (tempFile.exists()) tempFile.delete()
            emit(DownloadState.Failed(e.localizedMessage ?: "Unknown download error"))
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "LiteRtModelManager"
    }
}
