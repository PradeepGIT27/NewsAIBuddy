package com.support.tech.newsaibuddy.data.litertlm

/**
 * Model specifications for on-device LLMs available from litert-community on Hugging Face.
 */
data class LiteRtModel(
    val id: String,
    val displayName: String,
    val description: String,
    val repo: String,
    val filename: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val formattedSize: String
) {
    companion object {
        val QWEN_0_6B = LiteRtModel(
            id = "qwen-0.6b",
            displayName = "Qwen 3 (0.6B)",
            description = "Lightweight & fast. Ideal for mobile devices (~600 MB).",
            repo = "litert-community/Qwen3-0.6B",
            filename = "Qwen3-0.6B.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm",
            sizeBytes = 630_000_000L,
            formattedSize = "~600 MB"
        )

        val GEMMA_2B = LiteRtModel(
            id = "gemma-2b",
            displayName = "Gemma 4 (2B)",
            description = "High quality reasoning & knowledge from Google (~2.6 GB).",
            repo = "litert-community/gemma-4-E2B-it-litert-lm",
            filename = "gemma-4-E2B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
            sizeBytes = 2_600_000_000L,
            formattedSize = "~2.6 GB"
        )

        val ALL_MODELS = listOf(QWEN_0_6B, GEMMA_2B)
    }
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progressPercent: Int, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState
    data class Completed(val filePath: String) : DownloadState
    data class Failed(val error: String) : DownloadState
}
