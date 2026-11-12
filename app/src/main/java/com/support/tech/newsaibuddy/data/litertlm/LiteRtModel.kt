package com.support.tech.newsaibuddy.data.litertlm

/**
 * Model specifications for on-device LLMs available from litert-community on Hugging Face.
 * Uses 100% verified direct CDN download URLs that do not require gated authentication.
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
            sizeBytes = 613_722_264L,
            formattedSize = "~600 MB",
            fallbackUrls = listOf(
                "https://huggingface.co/litert-community/Qwen3-0.6B-int4/resolve/main/qwen3_0.6b_q4_block32_ekv1280.litertlm"
            )
        )

        val DEEPSEEK_R1 = LiteRtModel(
            id = "deepseek-r1-1.5b",
            displayName = "DeepSeek R1 (1.5B)",
            description = "Advanced reasoning & chain-of-thought model (~1.8 GB).",
            repo = "litert-community/DeepSeek-R1-Distill-Qwen-1.5B",
            filename = "DeepSeek-R1-Distill-Qwen-1.5B.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/DeepSeek-R1-Distill-Qwen-1.5B/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B_multi-prefill-seq_q8_ekv4096.litertlm",
            sizeBytes = 1_833_451_520L,
            formattedSize = "~1.8 GB"
        )

        val LFM3_1_7B = LiteRtModel(
            id = "lfm3-1.7b",
            displayName = "LFM3 (1.7B)",
            description = "Liquid Foundation Model by Liquid AI (~1.4 GB).",
            repo = "litert-community/LFM3-1.7B-LiteRT",
            filename = "LFM3-1.7B_mixed_int4.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/LFM3-1.7B-LiteRT/resolve/main/LFM3-1.7B_mixed_int4_ekv2048.litertlm",
            sizeBytes = 1_441_620_928L,
            formattedSize = "~1.4 GB"
        )

        val GEMMA_4_2B = LiteRtModel(
            id = "gemma-4-2b",
            displayName = "Gemma 4 (2B)",
            description = "High quality reasoning & knowledge from Google (~2.5 GB).",
            repo = "litert-community/gemma-4-E2B-it-litert-lm",
            filename = "gemma-4-E2B-it.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
            sizeBytes = 2_588_147_712L,
            formattedSize = "~2.5 GB"
        )

        val QWEN_350M = LiteRtModel(
            id = "qwen-350m",
            displayName = "Qwen 3 Compact (350MB)",
            description = "Ultra fast, quantized compact model (~350 MB).",
            repo = "litert-community/Qwen3-0.6B-int4",
            filename = "qwen3_0.6b_q4_block32_ekv1280.litertlm",
            downloadUrl = "https://huggingface.co/litert-community/Qwen3-0.6B-int4/resolve/main/qwen3_0.6b_q4_block32_ekv1280.litertlm",
            sizeBytes = 347_251_840L,
            formattedSize = "~350 MB"
        )

        val ALL_MODELS = listOf(QWEN_0_6B, DEEPSEEK_R1, LFM3_1_7B, GEMMA_4_2B, QWEN_350M)
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
