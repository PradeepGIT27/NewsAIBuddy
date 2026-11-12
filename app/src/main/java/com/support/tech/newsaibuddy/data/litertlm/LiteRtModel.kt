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
    val formattedSize: String,
    val fallbackUrls: List<String> = emptyList()
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

        val LLAMA_3_2_1B = LiteRtModel(
            id = "llama-3.2-1b",
            displayName = "Llama 3.2 (1B)",
            description = "Balanced speed & reasoning power by Meta (~1.3 GB).",
            repo = "litert-community/Llama-3.2-1B-Instruct-litert-lm",
            filename = "Llama-3.2-1B-Instruct.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/Llama-3.2-1B-Instruct-litert-lm/resolve/main/Llama-3.2-1B-Instruct.litertlm",
            sizeBytes = 1_300_000_000L,
            formattedSize = "~1.3 GB",
            fallbackUrls = listOf(
                "https://huggingface.co/litert-community/Llama-3.2-1B-Instruct/resolve/main/Llama-3.2-1B-Instruct.litertlm",
                "https://huggingface.co/litert-community/Llama-3.2-1B-Instruct-litert-lm/resolve/main/model.litertlm",
                "https://huggingface.co/litert-community/Llama-3.2-1B-Instruct-litert-lm/resolve/main/Llama-3.2-1B-Instruct-gpu.litertlm"
            )
        )

        val GEMMA_2B = LiteRtModel(
            id = "gemma-2b",
            displayName = "Gemma 4 (2B)",
            description = "High quality reasoning & knowledge from Google (~2.6 GB).",
            repo = "litert-community/gemma-4-E2B-it-litert-lm",
            filename = "gemma-4-E2B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
            sizeBytes = 2_600_000_000L,
            formattedSize = "~2.6 GB",
            fallbackUrls = listOf(
                "https://huggingface.co/litert-community/gemma-2-2b-it-litert-lm/resolve/main/gemma-2-2b-it.litertlm"
            )
        )

        val ALL_MODELS = listOf(QWEN_0_6B, LLAMA_3_2_1B, GEMMA_2B)
    }
}

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(
        val progressPercent: Int,
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val model: LiteRtModel
    ) : DownloadState
    data class Completed(val filePath: String, val model: LiteRtModel) : DownloadState
    data class Failed(val error: String, val model: LiteRtModel? = null) : DownloadState
}
