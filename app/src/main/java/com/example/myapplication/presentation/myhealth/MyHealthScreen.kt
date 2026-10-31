package com.example.myapplication.presentation.myhealth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.presentation.home.JourneyType
import com.example.myapplication.presentation.home.homePalette

// ---------------------------------------------------------------------------
// Main Screen
// ---------------------------------------------------------------------------
@Composable
fun MyHealthScreen(
    viewModel: MyHealthViewModel = viewModel(),
    onNavigateToSymptoms: () -> Unit = {},
    onNavigateToRecords: () -> Unit = {},
    onNavigateToRiskAssessment: () -> Unit = {},
    onNavigateToScreening: () -> Unit = {},
    onNavigateToQuestions: () -> Unit = {},
    onNavigateToMedications: () -> Unit = {},
    onNavigateToSideEffects: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    onNavigateToEducation: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.bgGlow.copy(alpha = 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.3f, 0f),
                        radius = size.width * 1.0f
                    )
                )
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title
            item(key = "title") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.my_health_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.my_health_subtitle),
                        fontSize = 14.sp,
                        color = p.textSecondary
                    )
                }
            }

            // Journey-aware overview card
            item(key = "overview") {
                AnimatedContent(
                    targetState = uiState.journeyType,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                    },
                    label = "overviewTransition"
                ) { journey ->
                    when (journey) {
                        JourneyType.EARLY_DETECTION ->
                            EarlyDetectionOverviewCard(uiState, onNavigateToRiskAssessment)
                        JourneyType.UNDER_EVALUATION ->
                            UnderEvaluationOverviewCard(uiState, onNavigateToRecords)
                        JourneyType.DIAGNOSED ->
                            DiagnosedOverviewCard(uiState, onNavigateToTimeline)
                    }
                }
            }

            // Journey-aware sections
            when (uiState.journeyType) {
                JourneyType.EARLY_DETECTION -> {
                    item(key = "risk_factors") {
                        HealthSectionCard(
                            icon = Icons.Outlined.Warning,
                            title = stringResource(R.string.my_health_risk_factors),
                            count = uiState.riskFactorCount,
                            countLabel = stringResource(R.string.my_health_factors_active),
                            accentColor = p.warning,
                            onClick = onNavigateToRiskAssessment
                        ) {
                            RiskFactorsPreview(uiState.riskFactors)
                        }
                    }

                    item(key = "symptoms") {
                        HealthSectionCard(
                            icon = Icons.Outlined.MonitorHeart,
                            title = stringResource(R.string.my_health_symptoms),
                            count = uiState.activeSymptomCount,
                            countLabel = stringResource(R.string.my_health_active),
                            accentColor = p.accent,
                            onClick = onNavigateToSymptoms
                        ) {
                            SymptomsPreview(uiState.recentSymptoms)
                        }
                    }

                    item(key = "screening") {
                        ScreeningReminderCard(
                            isDue = uiState.screeningDue,
                            message = uiState.screeningMessage,
                            onOpenScreening = onNavigateToScreening
                        )
                    }

                    item(key = "education") {
                        QuickEducationCard(onOpenEducation = onNavigateToEducation)
                    }
                }

                JourneyType.UNDER_EVALUATION -> {
                    item(key = "symptoms") {
                        HealthSectionCard(
                            icon = Icons.Outlined.MonitorHeart,
                            title = stringResource(R.string.my_health_active_symptoms),
                            count = uiState.activeSymptomCount,
                            countLabel = stringResource(R.string.my_health_active),
                            accentColor = p.accent,
                            onClick = onNavigateToSymptoms
                        ) {
                            SymptomsPreview(uiState.recentSymptoms)
                        }
                    }

                    item(key = "records") {
                        HealthSectionCard(
                            icon = Icons.Outlined.FolderOpen,
                            title = stringResource(R.string.my_health_test_results),
                            count = uiState.pendingResultsCount,
                            countLabel = stringResource(R.string.my_health_pending),
                            accentColor = p.warning,
                            onClick = onNavigateToRecords
                        ) {
                            RecordsPreview(uiState.recentRecords)
                        }
                    }

                    item(key = "questions") {
                        HealthSectionCard(
                            icon = Icons.Outlined.QuestionAnswer,
                            title = stringResource(R.string.my_health_doctor_questions),
                            count = uiState.unansweredCount,
                            countLabel = stringResource(R.string.my_health_unanswered),
                            accentColor = p.accentSoft,
                            onClick = onNavigateToQuestions
                        ) {
                            QuestionsPreview(uiState.doctorQuestions)
                        }
                    }

                    item(key = "appointments") {
                        if (uiState.appointments.isNotEmpty()) {
                            NextAppointmentCard(
                                appointment = uiState.appointments.first(),
                                onClick = onNavigateToAppointments
                            )
                        }
                    }
                }

                JourneyType.DIAGNOSED -> {
                    item(key = "medications") {
                        HealthSectionCard(
                            icon = Icons.Outlined.Medication,
                            title = stringResource(R.string.my_health_medications),
                            count = uiState.medications.size,
                            countLabel = stringResource(R.string.my_health_current),
                            accentColor = p.success,
                            onClick = onNavigateToMedications
                        ) {
                            MedicationsPreview(uiState.medications)
                        }
                    }

                    item(key = "side_effects") {
                        HealthSectionCard(
                            icon = Icons.Outlined.Bolt,
                            title = stringResource(R.string.my_health_side_effects),
                            count = uiState.activeSideEffectCount,
                            countLabel = stringResource(R.string.my_health_active),
                            accentColor = p.alert,
                            onClick = onNavigateToSideEffects
                        ) {
                            SideEffectsPreview(uiState.sideEffects)
                        }
                    }

                    item(key = "appointments") {
                        if (uiState.appointments.isNotEmpty()) {
                            NextAppointmentCard(
                                appointment = uiState.appointments.first(),
                                onClick = onNavigateToAppointments
                            )
                        }
                    }

                    item(key = "timeline") {
                        HealthTimelineCard(
                            events = uiState.timeline,
                            onClick = onNavigateToTimeline
                        )
                    }
                }
            }

            // Medical disclaimer (always visible)
            item(key = "disclaimer") {
                MedicalDisclaimerCard()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Overview Cards (Journey-specific)
// ---------------------------------------------------------------------------
@Composable
private fun EarlyDetectionOverviewCard(
    state: MyHealthUiState,
    onClick: () -> Unit
) {
    val p = homePalette()

    val riskColor = when (state.riskLevel) {
        RiskLevel.LOW -> p.success
        RiskLevel.MODERATE -> p.warning
        RiskLevel.ELEVATED -> p.alert
    }

    val riskLabel = when (state.riskLevel) {
        RiskLevel.LOW -> stringResource(R.string.my_health_risk_low)
        RiskLevel.MODERATE -> stringResource(R.string.my_health_risk_moderate)
        RiskLevel.ELEVATED -> stringResource(R.string.my_health_risk_elevated)
    }

    OverviewCardBase(
        icon = Icons.Outlined.Shield,
        iconColor = riskColor,
        title = stringResource(R.string.my_health_risk_profile),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Risk level badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(8.dp, CircleShape, ambientColor = riskColor)
                        .clip(CircleShape)
                        .background(riskColor.copy(alpha = 0.15f))
                        .border(2.dp, riskColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = riskLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_health_risk_factors_found, state.riskFactorCount),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textPrimary
                )
                Text(
                    text = stringResource(R.string.my_health_risk_disclaimer),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = p.textSecondary
                )
            }
        }
    }
}

@Composable
private fun UnderEvaluationOverviewCard(
    state: MyHealthUiState,
    onClick: () -> Unit
) {
    val p = homePalette()

    OverviewCardBase(
        icon = Icons.Outlined.Science,
        iconColor = p.accent,
        title = stringResource(R.string.my_health_eval_status),
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStat(
                value = "${state.activeSymptomCount}",
                label = stringResource(R.string.my_health_symptoms),
                color = p.accent
            )
            MiniStat(
                value = "${state.recentRecords.size}",
                label = stringResource(R.string.my_health_records),
                color = p.accentSoft
            )
            MiniStat(
                value = "${state.pendingResultsCount}",
                label = stringResource(R.string.my_health_pending),
                color = p.warning
            )
        }
    }
}

@Composable
private fun DiagnosedOverviewCard(
    state: MyHealthUiState,
    onClick: () -> Unit
) {
    val p = homePalette()

    OverviewCardBase(
        icon = Icons.Outlined.Favorite,
        iconColor = p.success,
        title = stringResource(R.string.my_health_care_overview),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${state.treatmentDay}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.success
                )
                Text(
                    text = stringResource(R.string.my_health_treatment_day),
                    fontSize = 11.sp,
                    color = p.textSecondary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = state.treatmentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textPrimary
                )
                Text(
                    text = stringResource(R.string.my_health_meds_count, state.medications.size),
                    fontSize = 12.sp,
                    color = p.textSecondary
                )
                if (state.appointments.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.my_health_next_appt,
                            state.appointments.first().date
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = p.accent
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCardBase(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = iconColor.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(listOf(p.cardTop, p.cardBottom))
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(iconColor.copy(alpha = 0.5f), p.borderDim)
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = iconColor
                )
            }
            content()
        }
    }
}

@Composable
private fun RowScope.MiniStat(
    value: String,
    label: String,
    color: Color
) {
    val p = homePalette()

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = p.textSecondary,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Generic Health Section Card
// ---------------------------------------------------------------------------
@Composable
private fun HealthSectionCard(
    icon: ImageVector,
    title: String,
    count: Int,
    countLabel: String,
    accentColor: Color,
    onClick: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = accentColor.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, p.borderDim, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )

                // Count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$count $countLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            // Preview content
            previewContent()

            // See all
            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_health_see_all),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Preview Components
// ---------------------------------------------------------------------------
@Composable
private fun RiskFactorsPreview(factors: List<RiskFactor>) {
    val p = homePalette()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        factors.filter { it.isActive }.take(3).forEach { factor ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(p.warning)
                )
                Text(
                    text = factor.label,
                    fontSize = 13.sp,
                    color = p.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SymptomsPreview(symptoms: List<SymptomEntry>) {
    val p = homePalette()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        symptoms.filter { !it.isResolved }.take(3).forEach { symptom ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SeverityIndicator(severity = symptom.severity)
                Text(
                    text = symptom.name,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = p.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = symptom.date,
                    fontSize = 11.sp,
                    color = p.textTertiary
                )
            }
        }
    }
}

@Composable
private fun SeverityIndicator(severity: Int) {
    val color = when {
        severity <= 2 -> homePalette().success
        severity <= 3 -> homePalette().warning
        else -> homePalette().alert
    }

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < severity) color else color.copy(alpha = 0.15f)
                    )
            )
        }
    }
}

@Composable
private fun RecordsPreview(records: List<MedicalRecord>) {
    val p = homePalette()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        records.take(3).forEach { record ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val statusColor = when (record.status) {
                    "Normal" -> p.success
                    "Abnormal" -> p.alert
                    "Pending" -> p.warning
                    else -> p.textSecondary
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Text(
                    text = record.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = p.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = record.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionsPreview(questions: List<DoctorQuestion>) {
    val p = homePalette()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        questions.filter { !it.isAnswered }.take(2).forEach { q ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = p.accentSoft,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Text(
                    text = q.question,
                    fontSize = 13.sp,
                    color = p.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MedicationsPreview(medications: List<Medication>) {
    val p = homePalette()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        medications.take(3).forEach { med ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Medication,
                    contentDescription = null,
                    tint = p.success,
                    modifier = Modifier.size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = med.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = p.textPrimary
                    )
                    Text(
                        text = "${med.dosage} · ${med.frequency}",
                        fontSize = 11.sp,
                        color = p.textSecondary
                    )
                }
                Text(
                    text = med.nextDose,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = p.accent
                )
            }
        }
    }
}

@Composable
private fun SideEffectsPreview(sideEffects: List<SideEffect>) {
    val p = homePalette()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sideEffects.take(3).forEach { se ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SeverityIndicator(severity = se.severity)
                Text(
                    text = se.name,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    color = p.textPrimary
                )
                if (se.relatedMedication != null) {
                    Text(
                        text = se.relatedMedication,
                        fontSize = 10.sp,
                        color = p.textTertiary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Screening Reminder Card
// ---------------------------------------------------------------------------
@Composable
private fun ScreeningReminderCard(
    isDue: Boolean,
    message: String,
    onOpenScreening: () -> Unit
) {
    val p = homePalette()

    if (!isDue) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp), ambientColor = p.warning.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        p.warning.copy(alpha = 0.12f),
                        p.warning.copy(alpha = 0.04f)
                    )
                )
            )
            .border(1.dp, p.warning.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenScreening)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(p.warning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = p.warning,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_health_screening_due),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )
                Text(
                    text = message,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = p.textSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = p.warning
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Quick Education Card
// ---------------------------------------------------------------------------
@Composable
private fun QuickEducationCard(onOpenEducation: () -> Unit) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, p.borderDim, RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenEducation)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(p.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_health_learn_more),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textPrimary
                )
                Text(
                    text = stringResource(R.string.my_health_learn_desc),
                    fontSize = 12.sp,
                    color = p.textSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = p.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Next Appointment Card
// ---------------------------------------------------------------------------
@Composable
private fun NextAppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp), ambientColor = p.accent.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(
                1.dp,
                Brush.linearGradient(listOf(p.accent.copy(alpha = 0.4f), p.borderDim)),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(p.accent.copy(alpha = 0.12f))
                    .border(1.dp, p.accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.my_health_next_appointment),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = p.accent
                )
                Text(
                    text = appointment.doctor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textPrimary
                )
                Text(
                    text = "${appointment.date} · ${appointment.time}",
                    fontSize = 12.sp,
                    color = p.textSecondary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = p.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Health Timeline Card
// ---------------------------------------------------------------------------
@Composable
private fun HealthTimelineCard(
    events: List<TimelineEvent>,
    onClick: () -> Unit
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, p.borderDim, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Timeline,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.my_health_timeline),
                    modifier = Modifier.weight(1f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )
                Text(
                    text = stringResource(R.string.my_health_see_all),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.accent
                )
            }

            events.take(3).forEachIndexed { index, event ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Timeline dot + line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(p.accent)
                        )
                        if (index < events.take(3).lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(p.borderDim)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = event.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = p.textPrimary
                        )
                        Text(
                            text = event.date,
                            fontSize = 11.sp,
                            color = p.textTertiary
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Medical Disclaimer (CRITICAL — always visible)
// ---------------------------------------------------------------------------
@Composable
private fun MedicalDisclaimerCard() {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(p.accent.copy(alpha = 0.06f))
            .border(1.dp, p.borderDim.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = p.textTertiary,
                modifier = Modifier.size(18.dp).padding(top = 1.dp)
            )
            Text(
                text = stringResource(R.string.my_health_medical_disclaimer),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = p.textTertiary
            )
        }
    }
}
