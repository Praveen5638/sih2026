package com.example.ai

import android.net.Uri

data class ImageProcessingContractResult(
    val isSuccess: Boolean,
    val originalUri: String,
    val enhancedUri: String?,
    val width: Int,
    val height: Int,
    val sourceRotation: Int,
    val processingTimeMs: Long,
    val segmentationPassed: Boolean,
    val fidelityPassed: Boolean,
    val fallbackTriggered: Boolean,
    val fallbackReason: String?,
    val outputFormat: String, // "JPEG" or "PNG"
    val outputSizeBytes: Long,
    val pipelineVersion: String = "v2.0-hardened",
    val errorCode: String? = null,
    val meanDeltaE: Double = 0.0,
    val p95DeltaE: Double = 0.0,
    val jaccardSilhouetteIndex: Float = 0f,
    val stageLogs: List<ProcessingStageLog> = emptyList()
)
