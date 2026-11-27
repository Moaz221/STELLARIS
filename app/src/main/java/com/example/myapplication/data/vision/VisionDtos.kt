package com.example.myapplication.data.vision

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PredictResponse(
    val status: String,
    val diagnosis: String? = null,
    val confidence: String? = null,
    @SerialName("raw_score") val rawScore: Double? = null,
    @SerialName("gradcam_base64") val gradcamBase64: String? = null,
    @SerialName("segmentation_mask_base64") val segmentationMaskBase64: String? = null,
    @SerialName("segmentation_overlay_base64") val segmentationOverlayBase64: String? = null,
    val notes: String? = null,
)
