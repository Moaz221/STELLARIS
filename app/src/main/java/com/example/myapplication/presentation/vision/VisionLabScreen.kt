package com.example.myapplication.presentation.vision

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareNotice
import com.example.myapplication.presentation.components.LungCareTag
import com.example.myapplication.presentation.home.homePalette
import kotlinx.coroutines.delay

@Composable
fun VisionLabScreen(
    onBack: () -> Unit = {}
) {
    val p = homePalette()
    var isAnalyzing by remember { mutableStateOf(false) }
    var isAnalysisComplete by remember { mutableStateOf(false) }

    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            delay(2500)
            isAnalyzing = false
            isAnalysisComplete = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .background(
                Brush.verticalGradient(
                    0f to p.bgGlow.copy(alpha = 0.45f),
                    0.35f to p.bgGlowSecondary.copy(alpha = 0.20f),
                    0.75f to Color.Transparent
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.vision_lab_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Chest X-Ray Capture & AI Quality Check",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Safety Disclaimer
            item {
                LungCareNotice(
                    title = "Educational Prototype Only",
                    message = "This demo simulates image quality validation. It does NOT diagnose cancer or replace clinical evaluation by a qualified radiologist.",
                    isUrgent = false
                )
            }

            // Capture / Upload Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(p.cardTop)
                        .border(1.dp, p.accent.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = p.accent)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Analyzing X-Ray Quality...",
                                color = p.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isAnalysisComplete) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = p.accent,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "X-Ray Quality: Excellent",
                                color = p.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Contrast, position, and resolution meet diagnostic standards.",
                                color = p.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraEnhance,
                                contentDescription = null,
                                tint = p.accent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Capture or Select Chest X-Ray",
                                color = p.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap below to run AI quality evaluation",
                                color = p.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            isAnalysisComplete = false
                            isAnalyzing = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Capture X-Ray", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isAnalysisComplete = false
                            isAnalyzing = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        enabled = !isAnalyzing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results Section
            item {
                AnimatedVisibility(visible = isAnalysisComplete) {
                    LungCareCard(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        cornerRadius = 16.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Image Quality Report",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                LungCareTag(text = "Job #9042")
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Text(
                                text = "• Position Check: Properly Centered\n• Exposure: Optimal\n• Blurring Artifacts: None Detected\n• Next Step: Forward image to your clinician for official radiological evaluation.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
