package com.support.tech.newsaibuddy.data.litertlm

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Service managing the on-device LiteRT-LM Engine and Conversation lifecycles.
 * Executes model operations on a dedicated single-thread executor as required by LiteRT-LM.
 */
@Singleton
class LiteRtLmService @Inject constructor(
    @ApplicationContext private val context: Context
) : AutoCloseable {

    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    var loadedModelPath: String? = null
        private set
    var activeBackend: String? = null
        private set
    var isBusy: Boolean = false
        private set

    val isReady: Boolean
        get() = engine != null && conversation != null

    /**
     * Initializes the LiteRT-LM engine and conversation off the main thread.
     * Attempts GPU backend first with automatic fallback to CPU.
     */
    suspend fun initialize(modelPath: String): Result<String> = withContext(dispatcher) {
        mutex.withLock {
            if (engine != null && loadedModelPath == modelPath) {
                return@withLock Result.success(activeBackend ?: "Ready")
            }

            val file = File(modelPath)
            if (!file.exists()) {
                return@withLock Result.failure(IllegalArgumentException("Model file not found at $modelPath"))
            }

            releaseInternal()

            // Try GPU first
            var loadedEngine: Engine? = null
            var backendUsed = "GPU"
            try {
                Log.d(TAG, "Attempting to initialize LiteRT-LM engine with GPU backend...")
                val gpuConfig = EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.GPU(),
                    cacheDir = context.cacheDir.path
                )
                loadedEngine = Engine(gpuConfig).also { it.initialize() }
                Log.d(TAG, "LiteRT-LM initialized successfully on GPU")
            } catch (gpuError: Throwable) {
                Log.w(TAG, "GPU initialization failed (${gpuError.message}), falling back to CPU backend...", gpuError)
                try {
                    val cpuConfig = EngineConfig(
                        modelPath = modelPath,
                        backend = Backend.CPU(),
                        cacheDir = context.cacheDir.path
                    )
                    loadedEngine = Engine(cpuConfig).also { it.initialize() }
                    backendUsed = "CPU"
                    Log.d(TAG, "LiteRT-LM initialized successfully on CPU")
                } catch (cpuError: Throwable) {
                    Log.e(TAG, "CPU initialization also failed", cpuError)
                    return@withLock Result.failure(cpuError)
                }
            }

            try {
                val newConversation = loadedEngine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(
                            "You are NewsAIBuddy, a helpful and knowledgeable on-device AI assistant for news, headlines, and general queries."
                        )
                    )
                )
                engine = loadedEngine
                conversation = newConversation
                loadedModelPath = modelPath
                activeBackend = backendUsed
                Result.success(backendUsed)
            } catch (convError: Throwable) {
                Log.e(TAG, "Failed to create conversation", convError)
                try { loadedEngine.close() } catch (_: Throwable) {}
                Result.failure(convError)
            }
        }
    }

    /**
     * Streams generated tokens asynchronously for a user query.
     */
    fun sendMessageStream(text: String): Flow<String> = callbackFlow {
        val job = scope.launch {
            if (!isReady || isBusy) {
                close(IllegalStateException("Engine is not ready or is busy"))
                return@launch
            }

            isBusy = true
            try {
                val currentConv = conversation
                if (currentConv == null) {
                    close(IllegalStateException("Conversation is null"))
                    return@launch
                }

                val replies = currentConv.sendMessageAsync(text)
                replies.collect { message ->
                    trySend(message.toString())
                }
                close()
            } catch (e: CancellationException) {
                conversation?.cancelProcess()
                throw e
            } catch (e: Throwable) {
                close(e)
            } finally {
                isBusy = false
            }
        }

        awaitClose {
            if (isBusy) {
                scope.launch {
                    try {
                        conversation?.cancelProcess()
                    } catch (_: Throwable) {}
                    isBusy = false
                }
            }
            job.cancel()
        }
    }

    private suspend fun releaseInternal() {
        while (isBusy) {
            try {
                conversation?.cancelProcess()
            } catch (_: Throwable) {}
            delay(50)
        }
        try {
            conversation?.close()
        } catch (_: Throwable) {}
        conversation = null

        try {
            engine?.close()
        } catch (_: Throwable) {}
        engine = null

        loadedModelPath = null
        activeBackend = null
        isBusy = false
    }

    suspend fun release() = withContext(dispatcher) {
        mutex.withLock {
            releaseInternal()
        }
    }

    override fun close() {
        scope.launch {
            mutex.withLock {
                releaseInternal()
            }
            executor.shutdown()
        }.invokeOnCompletion {
            scope.cancel()
        }
    }

    companion object {
        private const val TAG = "LiteRtLmService"
    }
}
