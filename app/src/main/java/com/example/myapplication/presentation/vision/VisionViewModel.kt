package com.example.myapplication.presentation.vision

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.vision.VisionRepository
import com.example.myapplication.data.vision.VisionRepositoryImpl
import com.example.myapplication.data.vision.VisionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VisionUiState {
    IDLE,
    PREVIEW,
    ANALYZING,
    RESULT,
    ERROR
}

data class VisionState(
    val state: VisionUiState = VisionUiState.IDLE,
    val selectedImageUri: Uri? = null,
    val errorMessage: String? = null,
    val errorRes: Int? = null,
    val gradcamBase64: String? = null,
    val segmentationOverlayBase64: String? = null,
    val prediction: String? = null,
    val visualFinding: String? = null,
    val confidence: Double? = null,
    val overlayBase64: String? = null,
    val errorKind: String? = null,   // NETWORK | TIMEOUT | BAD_IMAGE | SERVER | OTHER
    /** 0 uploading, 1 analyzing, 2 preparing result. Time-based estimate, not real server progress. */
    val step: Int = 0,
)

class VisionViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: VisionRepository = VisionRepositoryImpl(),
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VisionState())
    val uiState: StateFlow<VisionState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                state = VisionUiState.PREVIEW,
                selectedImageUri = uri,
                errorMessage = null,
                errorRes = null,
                gradcamBase64 = null,
                segmentationOverlayBase64 = null,
            )
        }
    }

    fun analyze() {
        val uri = _uiState.value.selectedImageUri ?: return
        val context = getApplication<Application>()

        _uiState.update { it.copy(state = VisionUiState.ANALYZING, errorMessage = null, step = 0) }

        stepJob?.cancel()
        stepJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2500); _uiState.update { if (it.state == VisionUiState.ANALYZING) it.copy(step = 1) else it }
            kotlinx.coroutines.delay(9000); _uiState.update { if (it.state == VisionUiState.ANALYZING) it.copy(step = 2) else it }
            kotlinx.coroutines.delay(12000); _uiState.update { if (it.state == VisionUiState.ANALYZING) it.copy(step = 3) else it }
        }
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            when (val result = repository.analyzeXray(context, uri)) {
                is VisionResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            state = VisionUiState.RESULT,
                            gradcamBase64 = data.gradcamBase64,
                            segmentationOverlayBase64 = data.segmentationOverlayBase64,
                            prediction = data.prediction,
                            visualFinding = data.visualFinding,
                            confidence = data.confidence,
                            overlayBase64 = data.overlayBase64,
                        )
                    }
                }
                is VisionResult.Error -> {
                    _uiState.update {
                        it.copy(
                            state = VisionUiState.ERROR,
                            errorRes = R.string.error_server,
                            errorKind = when {
                                result.code == 400 || result.code == 422 || result.code == 413 -> "BAD_IMAGE"
                                result.code == null -> "OTHER"
                                else -> "SERVER"
                            },
                            errorMessage = null,
                        )
                    }
                }
                is VisionResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            state = VisionUiState.ERROR,
                            errorRes = R.string.error_network,
                            errorKind = if (result.throwable is java.net.SocketTimeoutException) "TIMEOUT" else "NETWORK",
                            errorMessage = null,
                        )
                    }
                }
            }
        }
    }

    private var analyzeJob: kotlinx.coroutines.Job? = null
    private var stepJob: kotlinx.coroutines.Job? = null

    /** Stops waiting for the analysis and goes back to the chosen image. */
    fun cancel() {
        analyzeJob?.cancel(); stepJob?.cancel()
        _uiState.update { it.copy(state = if (it.selectedImageUri != null) VisionUiState.PREVIEW else VisionUiState.IDLE, step = 0) }
    }

    fun reset() {
        analyzeJob?.cancel(); stepJob?.cancel()
        _uiState.value = VisionState()
    }
}
