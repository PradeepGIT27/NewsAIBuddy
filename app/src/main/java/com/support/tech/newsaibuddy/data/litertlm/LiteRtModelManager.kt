package com.support.tech.newsaibuddy.data.litertlm

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages discovery, local storage, model selection, and background downloading of .litertlm model files.
 */
@Singleton
class LiteRtModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("litert_models_pref", Context.MODE_PRIVATE)
    private var activeDownloadCall: Call? = null

    private val okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val internalModelsDir: File
        get() = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }

    private val externalModelsDir: File?
        get() = context.getExternalFilesDir("models")?.apply { if (!exists()) mkdirs() }

    fun getSelectedModelId(): String? {
        return prefs.getString("selected_model_id", null)
    }

    fun setSelectedModelId(modelId: String) {
        prefs.edit().putString("selected_model_id", modelId).apply()
    }

    /**
     * Cancels any active network download call.
     */
    fun cancelDownload() {
        try {
            activeDownloadCall?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling download call", e)
        } finally {
            activeDownloadCall = null
        }
    }

    /**
     * Checks if a model file exists on local storage (internal or external).
     */
    fun isModelDownloaded(model: LiteRtModel): Boolean {
        val file = getModelFile(model)
        return file != null && file.exists() && file.length() > 10_000_000L
    }

    /**
     * Gets the File object for a downloaded model, or null if not downloaded.
     */
    fun getModelFile(model: LiteRtModel): File? {
        val extFile = externalModelsDir?.let { File(it, model.filename) }
        if (extFile != null && extFile.exists() && extFile.length() > 10_000_000L) {
            return extFile
        }

        val intFile = File(internalModelsDir, model.filename)
        if (intFile.exists() && intFile.length() > 10_000_000L) {
            return intFile
        }

        val rootFile = File(context.filesDir, model.filename)
        if (rootFile.exists() && rootFile.length() > 10_000_000L) {
            return rootFile
        }

        return null
    }

    /**
     * Returns the target file destination for a given model.
     */
    fun getTargetModelFile(model: LiteRtModel): File {
        return externalModelsDir?.let { File(it, model.filename) }
            ?: File(internalModelsDir, model.filename)
    }

    /**
     * Finds an existing model file on storage.
     */
    fun findExistingModelFile(): File? {
        val selectedId = getSelectedModelId()
        if (selectedId != null) {
            val selectedModel = LiteRtModel.ALL_MODELS.find { it.id == selectedId }
            if (selectedModel != null) {
                val file = getModelFile(selectedModel)
                if (file != null) return file
            }
        }

        for (model in LiteRtModel.ALL_MODELS) {
            val file = getModelFile(model)
            if (file != null) {
                setSelectedModelId(model.id)
                return file
            }
        }

        return null
    }

    /**
     * Downloads model weights directly from Hugging Face CDN using OkHttp with progress streaming,
     * supporting fallback URLs in case primary URL encounters repository name changes.
     */
    fun downloadModel(model: LiteRtModel): Flow<DownloadState> = flow {
        val targetFile = getTargetModelFile(model)
        val parentDir = targetFile.parentFile ?: context.filesDir
        if (!parentDir.exists()) parentDir.mkdirs()

        val tempFile = File(parentDir, "${model.filename}.tmp")

        Log.d(TAG, "Starting download for ${model.displayName}")
        emit(DownloadState.Downloading(0, 0, model.sizeBytes, model))

        val candidateUrls = listOf(model.downloadUrl) + model.fallbackUrls
        var successfulResponse: Response? = null
        var lastErrorCode = -1

        for (url in candidateUrls) {
            Log.d(TAG, "Attempting connection to download URL: $url")
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) NewsAIBuddy/1.0")
                .build()

            val call = okHttpClient.newCall(request)
            activeDownloadCall = call

            try {
                val response = call.execute()
                if (response.isSuccessful && response.body != null) {
                    successfulResponse = response
                    Log.d(TAG, "Established successful download stream from: $url")
                    break
                } else {
                    lastErrorCode = response.code
                    Log.w(TAG, "URL $url returned HTTP ${response.code}: ${response.message}")
                    response.close()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Request failed for $url: ${e.message}")
            }
        }

        if (successfulResponse == null) {
            val errorText = if (lastErrorCode != -1) "HTTP $lastErrorCode" else "Network error"
            Log.e(TAG, "Failed to connect to any download candidate URLs for ${model.displayName}")
            emit(DownloadState.Failed("Download server error ($errorText). Please try again.", model))
            return@flow
        }

        try {
            val body = successfulResponse.body
                ?: run {
                    emit(DownloadState.Failed("Empty response body from server.", model))
                    return@flow
                }

            val contentLength = if (body.contentLength() > 0) body.contentLength() else model.sizeBytes

            body.byteStream().use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(65536) // 64KB buffer for high throughput
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastEmittedProgress = -1

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val progress = if (contentLength > 0) {
                            ((totalRead * 100) / contentLength).toInt().coerceIn(0, 99)
                        } else {
                            0
                        }

                        if (progress != lastEmittedProgress || totalRead == contentLength) {
                            lastEmittedProgress = progress
                            emit(DownloadState.Downloading(progress, totalRead, contentLength, model))
                        }
                    }
                    outputStream.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 10_000_000L) {
                if (targetFile.exists()) targetFile.delete()

                val renamed = tempFile.renameTo(targetFile)
                if (renamed || (tempFile.copyTo(targetFile, overwrite = true).exists())) {
                    if (tempFile.exists()) tempFile.delete()
                    setSelectedModelId(model.id)
                    Log.d(TAG, "Successfully finalized download for ${model.displayName}: ${targetFile.absolutePath}")
                    emit(DownloadState.Completed(targetFile.absolutePath, model))
                    return@flow
                }
            }

            emit(DownloadState.Failed("Failed to finalize downloaded model file.", model))
        } catch (e: Exception) {
            Log.e(TAG, "Error streaming download for ${model.displayName}", e)
            if (tempFile.exists()) tempFile.delete()
            emit(DownloadState.Failed(e.localizedMessage ?: "Network error during download.", model))
        } finally {
            activeDownloadCall = null
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "LiteRtModelManager"
    }
}
