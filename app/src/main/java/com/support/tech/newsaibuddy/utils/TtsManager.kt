package com.support.tech.newsaibuddy.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class TtsStatus {
    IDLE,
    SPEAKING,
    PAUSED
}

data class TtsState(
    val status: TtsStatus = TtsStatus.IDLE,
    val currentId: String? = null,
    val currentTitle: String? = null,
    val currentText: String = "",
    val currentChunkIndex: Int = 0,
    val totalChunks: Int = 0,
    val speechRate: Float = 1.0f,
    val isInitialized: Boolean = false
)

/**
 * Singleton manager for Text-To-Speech synthesis with Play, Pause, Resume, Stop, and Speed controls.
 * Uses sentence chunking to deliver a reliable Pause & Resume experience across Android API levels.
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TextToSpeech.OnInitListener {

    private val _ttsState = MutableStateFlow(TtsState())
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    private var textToSpeech: TextToSpeech? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var sentenceChunks: List<String> = emptyList()
    private var isInternalStop = false

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            textToSpeech = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.setLanguage(Locale.US)
            }
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    scope.launch {
                        _ttsState.update { it.copy(status = TtsStatus.SPEAKING) }
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (isInternalStop) return
                    scope.launch {
                        val current = _ttsState.value
                        val nextIndex = current.currentChunkIndex + 1
                        if (nextIndex < sentenceChunks.size && current.status == TtsStatus.SPEAKING) {
                            _ttsState.update { it.copy(currentChunkIndex = nextIndex) }
                            speakChunk(nextIndex)
                        } else if (nextIndex >= sentenceChunks.size) {
                            _ttsState.update {
                                it.copy(
                                    status = TtsStatus.IDLE,
                                    currentChunkIndex = 0
                                )
                            }
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "TTS Utterance Error for ID: $utteranceId")
                    if (isInternalStop) return
                    scope.launch {
                        _ttsState.update { it.copy(status = TtsStatus.IDLE) }
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(TAG, "TTS Utterance Error code $errorCode for ID: $utteranceId")
                    if (isInternalStop) return
                    scope.launch {
                        _ttsState.update { it.copy(status = TtsStatus.IDLE) }
                    }
                }
            })

            _ttsState.update { it.copy(isInitialized = true) }
            Log.d(TAG, "TextToSpeech successfully initialized")
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status $status")
        }
    }

    /**
     * Speaks text for a given ID and optional title.
     * If currently paused on the same ID, resumes playback.
     */
    fun speak(id: String, text: String, title: String? = null) {
        val currentState = _ttsState.value
        if (currentState.currentId == id && currentState.status == TtsStatus.PAUSED) {
            resume()
            return
        }

        stopInternal(resetState = false)

        val cleanedText = cleanTextForSpeech(text)
        if (cleanedText.isBlank()) return

        sentenceChunks = splitIntoSentences(cleanedText)
        if (sentenceChunks.isEmpty()) return

        _ttsState.update {
            it.copy(
                status = TtsStatus.SPEAKING,
                currentId = id,
                currentTitle = title ?: "Reading text",
                currentText = cleanedText,
                currentChunkIndex = 0,
                totalChunks = sentenceChunks.size
            )
        }

        speakChunk(0)
    }

    /**
     * Pauses current speech playback at current sentence chunk.
     */
    fun pause() {
        if (_ttsState.value.status != TtsStatus.SPEAKING) return
        isInternalStop = true
        textToSpeech?.stop()
        isInternalStop = false
        _ttsState.update { it.copy(status = TtsStatus.PAUSED) }
    }

    /**
     * Resumes speech playback from the current sentence chunk.
     */
    fun resume() {
        val currentState = _ttsState.value
        if (currentState.status == TtsStatus.PAUSED && sentenceChunks.isNotEmpty()) {
            val chunkIdx = currentState.currentChunkIndex.coerceIn(0, sentenceChunks.lastIndex)
            _ttsState.update { it.copy(status = TtsStatus.SPEAKING) }
            speakChunk(chunkIdx)
        }
    }

    /**
     * Toggles play/pause state for a specific item.
     */
    fun togglePlayPause(id: String, text: String, title: String? = null) {
        val currentState = _ttsState.value
        if (currentState.currentId == id) {
            when (currentState.status) {
                TtsStatus.SPEAKING -> pause()
                TtsStatus.PAUSED -> resume()
                TtsStatus.IDLE -> speak(id, text, title)
            }
        } else {
            speak(id, text, title)
        }
    }

    /**
     * Stops playback and resets state to Idle.
     */
    fun stop() {
        stopInternal(resetState = true)
    }

    private fun stopInternal(resetState: Boolean) {
        isInternalStop = true
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
        isInternalStop = false
        if (resetState) {
            sentenceChunks = emptyList()
            _ttsState.update {
                it.copy(
                    status = TtsStatus.IDLE,
                    currentId = null,
                    currentTitle = null,
                    currentText = "",
                    currentChunkIndex = 0,
                    totalChunks = 0
                )
            }
        }
    }

    /**
     * Sets speech playback rate (0.75x, 1.0x, 1.25x, 1.5x, 2.0x).
     */
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.5f)
        textToSpeech?.setSpeechRate(clampedRate)
        _ttsState.update { it.copy(speechRate = clampedRate) }

        // If currently speaking, restart current chunk with new speed
        if (_ttsState.value.status == TtsStatus.SPEAKING) {
            val currentIdx = _ttsState.value.currentChunkIndex
            isInternalStop = true
            textToSpeech?.stop()
            isInternalStop = false
            speakChunk(currentIdx)
        }
    }

    private fun speakChunk(index: Int) {
        if (index !in sentenceChunks.indices) return
        val chunkText = sentenceChunks[index]
        textToSpeech?.setSpeechRate(_ttsState.value.speechRate)

        val params = Bundle()
        val utteranceId = "utterance_${System.currentTimeMillis()}_$index"
        textToSpeech?.speak(chunkText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun cleanTextForSpeech(text: String): String {
        return text
            .replace(Regex("```[\\s\\S]*?```"), "Code block omitted.") // Clean code blocks
            .replace(Regex("[#*_`>\\-\\[\\]()]"), " ") // Strip markdown characters
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun splitIntoSentences(text: String): List<String> {
        val rawSentences = text.split(Regex("(?<=[.!?])\\s+"))
        val result = mutableListOf<String>()
        for (s in rawSentences) {
            val trimmed = s.trim()
            if (trimmed.isNotBlank()) {
                if (trimmed.length > 250) {
                    // Split long paragraphs by commas or space chunks
                    result.addAll(trimmed.chunked(200))
                } else {
                    result.add(trimmed)
                }
            }
        }
        return result
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }

    companion object {
        private const val TAG = "TtsManager"
    }
}
