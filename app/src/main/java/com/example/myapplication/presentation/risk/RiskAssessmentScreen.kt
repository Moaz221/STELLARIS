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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.components.LungCareButton
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareNotice
import com.example.myapplication.presentation.components.LungCareTag

@Composable
fun RiskAssessmentScreen(
    onBack: () -> Unit = {}
) {
    var ageText by remember { mutableStateOf("54") }
    var packYearsText by remember { mutableStateOf("22") }
    var hasFamilyHistory by remember { mutableStateOf(false) }
    var isEvaluated by remember { mutableStateOf(false) }
    var isEligibleForLdct by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        text = "Based on USPSTF LDCT Screening Criteria",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Screening Info Banner
        item {
            LungCareNotice(
                title = "What is LDCT Screening?",
                message = stringResource(R.string.ldct_info_text),
                isUrgent = false
            )
        }

        // Assessment Form
        item {
            LungCareCard(
                backgroundColor = MaterialTheme.colorScheme.surface,
                cornerRadius = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter Your Health Information",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it },
                        label = { Text(stringResource(R.string.risk_age)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packYearsText,
                        onValueChange = { packYearsText = it },
                        label = { Text(stringResource(R.string.pack_years)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Family History of Lung Cancer",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = hasFamilyHistory,
                            onCheckedChange = { hasFamilyHistory = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LungCareButton(
                        text = stringResource(R.string.calculate_risk),
                        onClick = {
                            val age = ageText.toIntOrNull() ?: 0
                            val packYears = packYearsText.toIntOrNull() ?: 0
                            isEligibleForLdct = (age in 50..80) && (packYears >= 20)
                            isEvaluated = true
                        }
                    )
                }
            }
        }

        // Results Section
        if (isEvaluated) {
            item {
                val cardBg = if (isEligibleForLdct) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant

                LungCareCard(
                    backgroundColor = cardBg,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    cornerRadius = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isEligibleForLdct) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isEligibleForLdct) stringResource(R.string.ldct_recommended)
                                else stringResource(R.string.ldct_not_recommended),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = if (isEligibleForLdct)
                                "You meet the clinical eligibility guidelines for an annual Low-Dose CT scan. Please download this report and discuss scheduling an LDCT scan with your primary care provider."
                            else
                                "Based on the provided age and smoking history, routine LDCT screening is currently not recommended. Continue monitoring symptoms and practicing preventive health habits.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
