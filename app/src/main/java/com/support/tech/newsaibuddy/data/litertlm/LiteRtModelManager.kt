package com.support.tech.newsaibuddy.data.litertlm

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol
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
    @Volatile
    private var activeDownloadCall: Call? = null
    @Volatile
    private var isCancelledByUser = false

    // HTTP/1.1 avoids HTTP/2 multiplexing stream resets (RST_STREAM CANCEL) on CDNs during large file downloads.
    private val okHttpClient = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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
            isCancelledByUser = true
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
     * supporting fallback URLs, HTTP Range resuming, and automatic retry on network drops/resets.
     */
    fun downloadModel(model: LiteRtModel): Flow<DownloadState> = flow {
        val targetFile = getTargetModelFile(model)
        val parentDir = targetFile.parentFile ?: context.filesDir
        if (!parentDir.exists()) parentDir.mkdirs()

        val tempFile = File(parentDir, "${model.filename}.tmp")
        isCancelledByUser = false

        Log.d(TAG, "Starting download for ${model.displayName}")
        emit(DownloadState.Downloading(0, 0, model.sizeBytes, model))

        val candidateUrls = listOf(model.downloadUrl) + model.fallbackUrls
        val maxRetries = 5
        var currentAttempt = 0
        var isDownloadSuccessful = false
        var lastException: Exception? = null

        while (currentAttempt < maxRetries && !isDownloadSuccessful) {
            if (isCancelledByUser) {
                Log.d(TAG, "Download cancelled by user.")
                if (tempFile.exists()) tempFile.delete()
                emit(DownloadState.Idle)
                return@flow
            }

            currentAttempt++
            val existingLength = if (tempFile.exists()) tempFile.length() else 0L

            var successfulResponse: Response? = null
            var lastErrorCode = -1

            for (url in candidateUrls) {
                if (isCancelledByUser) break
                Log.d(TAG, "Attempting connection to $url (Attempt $currentAttempt/$maxRetries, Resume from: $existingLength bytes)")

                val requestBuilder = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) NewsAIBuddy/1.0")

                if (existingLength > 0) {
                    requestBuilder.addHeader("Range", "bytes=$existingLength-")
                }

                val request = requestBuilder.build()
                val call = okHttpClient.newCall(request)
                activeDownloadCall = call

                try {
                    val response = call.execute()
                    if (response.isSuccessful || response.code == 206) {
                        successfulResponse = response
                        Log.d(TAG, "Established successful download stream from: $url (HTTP ${response.code})")
                        break
                    } else if (response.code == 416) {
                        Log.w(TAG, "HTTP 416 Range Not Satisfiable. Resetting temp file and retrying.")
                        tempFile.delete()
                        response.close()
                        break
                    } else {
                        lastErrorCode = response.code
                        Log.w(TAG, "URL $url returned HTTP ${response.code}: ${response.message}")
                        response.close()
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.w(TAG, "Request failed for $url: ${e.message}")
                }
            }

            if (isCancelledByUser) {
                Log.d(TAG, "Download cancelled by user.")
                if (tempFile.exists()) tempFile.delete()
                emit(DownloadState.Idle)
                return@flow
            }

            if (successfulResponse == null) {
                if (lastErrorCode in listOf(401, 403, 404)) {
                    val errorText = when (lastErrorCode) {
                        401, 403 -> "HTTP $lastErrorCode: Restricted or Gated Repository. Please choose an un-gated model like Qwen 3 or Gemma 4."
                        404 -> "HTTP 404: Model file URL not found on Hugging Face CDN."
                        else -> "HTTP $lastErrorCode"
                    }
                    Log.e(TAG, "Failed to connect to download URLs for ${model.displayName}: $errorText")
                    if (tempFile.exists()) tempFile.delete()
                    emit(DownloadState.Failed("Download failed ($errorText).", model))
                    return@flow
                }

                if (currentAttempt < maxRetries) {
                    Log.w(TAG, "Connection failed on attempt $currentAttempt/$maxRetries. Retrying in ${currentAttempt * 2}s...")
                    delay(2000L * currentAttempt)
                    continue
                } else {
                    val errorText = lastException?.localizedMessage ?: if (lastErrorCode != -1) "HTTP $lastErrorCode" else "Network Connection Failed"
                    Log.e(TAG, "All $maxRetries download attempts failed for ${model.displayName}: $errorText")
                    emit(DownloadState.Failed("Download failed after $maxRetries attempts: $errorText", model))
                    return@flow
                }
            }

            try {
                val responseCode = successfulResponse.code
                val isPartial = (responseCode == 206)
                val body = successfulResponse.body

                val startingBytes = if (isPartial && tempFile.exists()) tempFile.length() else 0L
                val totalContentLength = if (isPartial) {
                    startingBytes + body.contentLength()
                } else {
                    if (body.contentLength() > 0) body.contentLength() else model.sizeBytes
                }

                val appendMode = isPartial && startingBytes > 0

                body.byteStream().use { inputStream ->
                    FileOutputStream(tempFile, appendMode).use { outputStream ->
                        val buffer = ByteArray(65536) // 64KB buffer
                        var bytesRead: Int
                        var totalRead = startingBytes
                        var lastEmittedProgress = -1

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            if (isCancelledByUser) break
                            outputStream.write(buffer, 0, bytesRead)
                            totalRead += bytesRead

                            val progress = if (totalContentLength > 0) {
                                ((totalRead * 100) / totalContentLength).toInt().coerceIn(0, 99)
                            } else {
                                0
                            }

                            if (progress != lastEmittedProgress || totalRead == totalContentLength) {
                                lastEmittedProgress = progress
                                emit(DownloadState.Downloading(progress, totalRead, totalContentLength, model))
                            }
                        }
                        outputStream.flush()
                    }
                }

                if (isCancelledByUser) {
                    Log.d(TAG, "Download cancelled during streaming.")
                    if (tempFile.exists()) tempFile.delete()
                    emit(DownloadState.Idle)
                    return@flow
                }

                isDownloadSuccessful = true
            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Error streaming download for ${model.displayName} on attempt $currentAttempt/$maxRetries", e)
                if (isCancelledByUser) {
                    if (tempFile.exists()) tempFile.delete()
                    emit(DownloadState.Idle)
                    return@flow
                }
                if (currentAttempt < maxRetries) {
                    Log.d(TAG, "Retrying streaming download after error (${e.message})...")
                    delay(1000L * currentAttempt)
                }
            } finally {
                successfulResponse.close()
                activeDownloadCall = null
            }
        }

        if (isDownloadSuccessful && tempFile.exists() && tempFile.length() > 10_000_000L) {
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

        if (!isCancelledByUser) {
            val finalMsg = lastException?.localizedMessage ?: "Failed to finalize downloaded model file."
            Log.e(TAG, "Download failed for ${model.displayName}: $finalMsg")
            emit(DownloadState.Failed(finalMsg, model))
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "LiteRtModelManager"
    }
}

