package com.example.myapplication.presentation.risk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.components.LungCareButton
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareNotice
import com.example.myapplication.presentation.components.LungCareTag
import com.example.myapplication.presentation.home.homePalette

@Composable
fun RiskAssessmentScreen(
    onBack: () -> Unit = {}
) {
    val p = homePalette()
    var ageText by remember { mutableStateOf("54") }
    var packYearsText by remember { mutableStateOf("22") }
    var hasFamilyHistory by remember { mutableStateOf(false) }
    var isEvaluated by remember { mutableStateOf(false) }
    var isEligibleForLdct by remember { mutableStateOf(true) }

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
                            text = stringResource(R.string.risk_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Low-Dose CT (LDCT) Screening Eligibility Assessment",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Input Form Card
            item {
                LungCareCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(R.string.risk_age),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            placeholder = { Text("e.g. 55") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = stringResource(R.string.pack_years),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = packYearsText,
                            onValueChange = { packYearsText = it },
                            placeholder = { Text("e.g. 20") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = hasFamilyHistory,
                                onCheckedChange = { hasFamilyHistory = it }
                            )
                            Text(
                                text = "Family history of lung cancer / occupational exposure",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        LungCareButton(
                            text = stringResource(R.string.calculate_risk),
                            onClick = {
                                val age = ageText.toIntOrNull() ?: 0
                                val packYears = packYearsText.toIntOrNull() ?: 0
                                isEligibleForLdct = (age in 50..80) && (packYears >= 20 || hasFamilyHistory)
                                isEvaluated = true
                            }
                        )
                    }
                }
            }

            // Result Notice
            if (isEvaluated) {
                item {
                    val resultTitle = if (isEligibleForLdct) {
                        stringResource(R.string.ldct_recommended)
                    } else {
                        stringResource(R.string.ldct_not_recommended)
                    }

                    LungCareNotice(
                        title = resultTitle,
                        message = stringResource(R.string.ldct_info_text),
                        isUrgent = isEligibleForLdct
                    )
                }
            }
        }
    }
}
