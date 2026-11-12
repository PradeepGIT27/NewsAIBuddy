package com.support.tech.newsaibuddy.data.litertlm

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * Service managing the on-device LiteRT-LM Engine and Conversation lifecycles.
 * Executes model operations on a dedicated single-thread executor as required by LiteRT-LM.
 * Features strict initialization timeouts to prevent app unresponsive/hanging states.
 */
@Singleton
class LiteRtLmService @Inject constructor(
    @ApplicationContext private val context: Context
) : AutoCloseable {

    @Volatile
    private var executor = Executors.newSingleThreadExecutor()
    @Volatile
    private var dispatcher = executor.asCoroutineDispatcher()

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

    @Synchronized
    private fun getDispatcher(): CoroutineDispatcher {
        if (executor.isShutdown || executor.isTerminated) {
            executor = Executors.newSingleThreadExecutor()
            dispatcher = executor.asCoroutineDispatcher()
        }
        return dispatcher
    }

    /**
     * Checks if the current device can safely run GPU OpenCL kernels for LiteRT-LM.
     * On Samsung Galaxy S25 series (Snapdragon 8 Elite / Adreno 830) and Android 15+,
     * OpenCL driver differences cause segmentation faults (SIGSEGV in libOpenCL.so).
     * On such devices, the Snapdragon 8 Elite Oryon CPU cores provide blazing-fast, 100% stable inference.
     */
    private fun isGpuSafe(): Boolean {
        val isSamsung = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
        val isAndroid15Plus = Build.VERSION.SDK_INT >= 35
        val isSnapdragon8Elite = Build.HARDWARE.contains("qcom", ignoreCase = true) &&
                ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.SOC_MODEL.equals("SM8750", ignoreCase = true)) ||
                 Build.MODEL.contains("S93", ignoreCase = true))

        if (isSamsung && (isAndroid15Plus || isSnapdragon8Elite)) {
            Log.w(TAG, "Samsung S25 / Android 15+ detected: Selecting CPU backend to prevent OpenCL GPU driver crash.")
            return false
        }
        return true
    }

    /**
     * Initializes the LiteRT-LM engine and conversation off the main thread with timeouts.
     * Uses CPU backend on devices with known OpenCL driver issues (like S25 Ultra),
     * or when forceCpu is true, and attempts GPU with CPU fallback on compatible devices.
     */
    suspend fun initialize(modelPath: String, forceCpu: Boolean = false): Result<String> = withContext(getDispatcher()) {
        mutex.withLock {
            if (engine != null && loadedModelPath == modelPath && (forceCpu == (activeBackend == "CPU"))) {
                return@withLock Result.success(activeBackend ?: "Ready")
            }

            val file = File(modelPath)
            if (!file.exists()) {
                return@withLock Result.failure(IllegalArgumentException("Model file not found at $modelPath"))
            }

            releaseInternal()

            var loadedEngine: Engine? = null
            var backendUsed = "CPU"

            try {
                // Try GPU first only if requested & safe for this device architecture
                if (!forceCpu && isGpuSafe()) {
                    try {
                        Log.d(TAG, "Attempting to initialize LiteRT-LM engine with GPU backend...")
                        val gpuConfig = EngineConfig(
                            modelPath = modelPath,
                            backend = Backend.GPU(),
                            cacheDir = context.cacheDir.path
                        )
                        var tempGpuEngine: Engine? = null
                        loadedEngine = withTimeoutOrNull(15_000L) {
                            val e = Engine(gpuConfig)
                            tempGpuEngine = e
                            e.initialize()
                            e
                        }
                        if (loadedEngine != null) {
                            backendUsed = "GPU"
                            Log.d(TAG, "LiteRT-LM initialized successfully on GPU")
                        } else {
                            Log.w(TAG, "GPU initialization timed out after 15s. Closing temp engine & falling back to CPU...")
                            try { tempGpuEngine?.close() } catch (_: Throwable) {}
                        }
                    } catch (gpuError: Throwable) {
                        Log.w(TAG, "GPU initialization failed (${gpuError.message}), falling back to CPU backend...", gpuError)
                    }
                }

                // CPU initialization (default or fallback)
                if (loadedEngine == null) {
                    try {
                        Log.d(TAG, "Attempting to initialize LiteRT-LM engine with CPU backend...")
                        val cpuConfig = EngineConfig(
                            modelPath = modelPath,
                            backend = Backend.CPU(),
                            cacheDir = context.cacheDir.path
                        )
                        var tempCpuEngine: Engine? = null
                        loadedEngine = withTimeoutOrNull(30_000L) {
                            val e = Engine(cpuConfig)
                            tempCpuEngine = e
                            e.initialize()
                            e
                        }
                        if (loadedEngine != null) {
                            backendUsed = "CPU"
                            Log.d(TAG, "LiteRT-LM initialized successfully on CPU")
                        } else {
                            try { tempCpuEngine?.close() } catch (_: Throwable) {}
                            val timeoutMsg = "Engine initialization timed out (took over 30 seconds)."
                            Log.e(TAG, timeoutMsg)
                            return@withLock Result.failure(IllegalStateException(timeoutMsg))
                        }
                    } catch (cpuError: Throwable) {
                        Log.e(TAG, "CPU initialization also failed", cpuError)
                        return@withLock Result.failure(cpuError)
                    }
                }

                val currentEngine = loadedEngine

                val newConversation = currentEngine.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(
                            "You are NewsAIBuddy, a helpful and knowledgeable on-device AI assistant for news, headlines, and general queries."
                        )
                    )
                )

                engine = currentEngine
                conversation = newConversation
                loadedModelPath = modelPath
                activeBackend = backendUsed
                loadedEngine = null // cleared so finally block won't close it

                Result.success(backendUsed)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to initialize model at $modelPath", t)
                try { loadedEngine?.close() } catch (_: Throwable) {}
                Result.failure(t)
            } finally {
                // Cleanup any dangling engine reference if initialization was cancelled or failed
                try { loadedEngine?.close() } catch (_: Throwable) {}
            }
        }
    }

    /**
     * Streams generated tokens asynchronously for a user query.
     * All calls run on the dedicated single-thread dispatcher as required by LiteRT-LM.
     */
    fun sendMessageStream(text: String): Flow<String> = channelFlow {
        withContext(getDispatcher()) {
            val conv = conversation ?: throw IllegalStateException("Model is not initialized.")
            if (isBusy) {
                throw IllegalStateException("Model is already busy generating.")
            }

            isBusy = true
            try {
                conv.sendMessageAsync(text).collect { message ->
                    send(message.toString())
                }
            } catch (e: CancellationException) {
                try { conv.cancelProcess() } catch (_: Throwable) {}
                throw e
            } finally {
                isBusy = false
            }
        }
    }

    /**
     * Resets the active conversation context in the LiteRT-LM engine.
     * Useful when starting a new chat or switching between history threads.
     */
    suspend fun resetConversation(): Boolean = withContext(getDispatcher()) {
        mutex.withLock {
            val eng = engine ?: return@withContext false
            try {
                if (isBusy) {
                    try { conversation?.cancelProcess() } catch (_: Throwable) {}
                    isBusy = false
                }
                try { conversation?.close() } catch (_: Throwable) {}
                conversation = eng.createConversation(
                    ConversationConfig(
                        systemInstruction = Contents.of(
                            "You are NewsAIBuddy, a helpful and knowledgeable on-device AI assistant for news, headlines, and general queries."
                        )
                    )
                )
                Log.d(TAG, "Conversation context reset successfully.")
                true
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to reset conversation context", e)
                false
            }
        }
    }

    private suspend fun releaseInternal() {
        var attempts = 0
        while (isBusy && attempts < 20) { // max wait 1 second
            try {
                conversation?.cancelProcess()
            } catch (_: Throwable) {}
            delay(50)
            attempts++
        }
        isBusy = false

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
    }

    suspend fun release() = withContext(getDispatcher()) {
        mutex.withLock {
            releaseInternal()
        }
    }

    override fun close() {
        scope.launch {
            mutex.withLock {
                releaseInternal()
            }
            synchronized(this@LiteRtLmService) {
                executor.shutdown()
            }
        }.invokeOnCompletion {
            scope.cancel()
        }
    }

    companion object {
        private const val TAG = "LiteRtLmService"
    }
}

