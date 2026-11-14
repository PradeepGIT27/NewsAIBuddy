package com.support.tech.newsaibuddy.data.litertlm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiteRtLmServiceTest {

    @Test
    fun liteRtModel_properties_areCorrect() {
        val model = LiteRtModel.QWEN_0_6B
        assertEquals("qwen-0.6b", model.id)
        assertEquals("Qwen 3 (0.6B)", model.displayName)
        assertTrue(model.downloadUrl.contains("Qwen3-0.6B"))
    }

    @Test
    fun downloadState_initial_isIdle() {
        val state: DownloadState = DownloadState.Idle
        assertEquals(DownloadState.Idle, state)
    }

    @Test
    fun downloadState_downloadingProgress_holdsProgressPercent() {
        val state = DownloadState.Downloading(
            progressPercent = 50,
            bytesDownloaded = 500_000,
            totalBytes = 1_000_000,
            model = LiteRtModel.QWEN_0_6B
        )
        assertEquals(50, state.progressPercent)
        assertEquals(1_000_000L, state.totalBytes)
    }
}
