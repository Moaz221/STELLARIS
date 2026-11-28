package com.example.myapplication.data.vision

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Shape verified against the live server: confidence is a number (0..1), images are base64 PNGs. */
@Serializable
data class PredictResponse(
    val status: String? = null,
    val prediction: String? = null,
    @SerialName("visual_finding") val visualFinding: String? = null,
    val confidence: Double? = null,
    @SerialName("raw_score") val rawScore: Double? = null,
    @SerialName("gradcam_base64") val gradcamBase64: String? = null,
    @SerialName("heatmap_base64") val heatmapBase64: String? = null,
    @SerialName("overlay_base64") val overlayBase64: String? = null,
    @SerialName("segmentation_mask_base64") val segmentationMaskBase64: String? = null,
    @SerialName("segmentation_overlay_base64") val segmentationOverlayBase64: String? = null,
    val disclaimer: String? = null,
)
