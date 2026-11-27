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
    val educationalNote: String? = null,
    val internalDiagnosis: String? = null,
    val internalConfidence: String? = null,
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

        _uiState.update { it.copy(state = VisionUiState.ANALYZING, errorMessage = null) }

        viewModelScope.launch {
            when (val result = repository.analyzeXray(context, uri)) {
                is VisionResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            state = VisionUiState.RESULT,
                            gradcamBase64 = data.gradcamBase64,
                            segmentationOverlayBase64 = data.segmentationOverlayBase64,
                            educationalNote = data.notes,
                            internalDiagnosis = data.diagnosis,
                            internalConfidence = data.confidence,
                        )
                    }
                }
                is VisionResult.Error -> {
                    _uiState.update {
                        it.copy(
                            state = VisionUiState.ERROR,
                            errorRes = R.string.error_server,
                            errorMessage = "Error ${result.code}: ${result.message}",
                        )
                    }
                }
                is VisionResult.Exception -> {
                    _uiState.update {
                        it.copy(
                            state = VisionUiState.ERROR,
                            errorRes = R.string.error_network,
                            errorMessage = result.throwable.message,
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = VisionState()
    }
}
