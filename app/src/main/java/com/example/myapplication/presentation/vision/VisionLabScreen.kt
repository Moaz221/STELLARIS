package com.example.myapplication.presentation.vision

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.core.design.LungCarePalette
import com.example.myapplication.core.design.lungCarePalette

@Composable
fun VisionLabScreen(
    onBack: () -> Unit = {},
    viewModel: VisionViewModel = viewModel(),
) {
    val p = lungCarePalette()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .background(
                Brush.verticalGradient(
                    listOf(p.primary.copy(alpha = 0.08f), Color.Transparent)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = p.textPrimary
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.vision_lab_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.vision_lab_subtitle),
                        fontSize = 12.sp,
                        color = p.textSecondary
                    )
                }
            }

            // Safety banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = p.info.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, p.info.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = p.info,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.vision_disclaimer),
                        fontSize = 11.sp,
                        color = p.textPrimary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Image preview / capture area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(p.surface)
                    .border(1.5.dp, p.border, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (state.state) {
                    VisionUiState.IDLE -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraEnhance,
                                contentDescription = null,
                                tint = p.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.capture_or_select),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.tap_to_analyze),
                                fontSize = 12.sp,
                                color = p.textSecondary
                            )
                        }
                    }

                    VisionUiState.PREVIEW -> {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    VisionUiState.ANALYZING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = p.primary, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.analyzing),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.analyzing_hint),
                                fontSize = 11.sp,
                                color = p.textSecondary
                            )
                        }
                    }

                    VisionUiState.RESULT -> {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    VisionUiState.ERROR -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = p.urgent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.errorMessage ?: stringResource(R.string.error_generic),
                                fontSize = 13.sp,
                                color = p.urgent
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.state != VisionUiState.ANALYZING
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.pick_photo), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.analyze() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (state.state == VisionUiState.PREVIEW) || (state.state == VisionUiState.ERROR),
                    colors = ButtonDefaults.buttonColors(containerColor = p.primary)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.analyze), fontWeight = FontWeight.Bold)
                }
            }

            // Results section
            AnimatedVisibility(
                visible = state.state == VisionUiState.RESULT,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.analysis_results),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.textPrimary
                    )

                    // Grad-CAM overlay
                    state.gradcamBase64?.let { b64 ->
                        ResultImageCard(
                            title = stringResource(R.string.gradcam_title),
                            subtitle = stringResource(R.string.gradcam_subtitle),
                            base64 = b64,
                            p = p
                        )
                    }

                    // Segmentation overlay
                    state.segmentationOverlayBase64?.let { b64 ->
                        ResultImageCard(
                            title = stringResource(R.string.segmentation_title),
                            subtitle = stringResource(R.string.segmentation_subtitle),
                            base64 = b64,
                            p = p
                        )
                    }

                    // Educational note
                    state.educationalNote?.let { note ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = p.softMint,
                            border = BorderStroke(1.dp, p.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = p.primary, modifier = Modifier.size(18.dp))
                                Text(note, fontSize = 11.sp, color = p.textPrimary, lineHeight = 15.sp)
                            }
                        }
                    }

                    // Medical safety warning
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = p.warning.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, p.warning.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = p.warning, modifier = Modifier.size(18.dp))
                            Text(
                                text = stringResource(R.string.ai_not_diagnosis),
                                fontSize = 11.sp,
                                color = p.textPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Error state action
            if (state.state == VisionUiState.ERROR) {
                Button(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = p.urgent)
                ) {
                    Text(stringResource(R.string.try_again), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultImageCard(
    title: String,
    subtitle: String,
    base64: String,
    p: LungCarePalette
) {
    val bitmap = remember(base64) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = p.surface,
        border = BorderStroke(1.dp, p.border)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = p.textSecondary, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(10.dp))
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } ?: Text(stringResource(R.string.image_decode_error), color = p.urgent, fontSize = 12.sp)
        }
    }
}
