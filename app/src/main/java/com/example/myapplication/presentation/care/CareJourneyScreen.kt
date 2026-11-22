package com.example.myapplication.presentation.care

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.core.design.LungCarePalette
import com.example.myapplication.core.design.lungCarePalette
import com.example.myapplication.core.model.*

@Composable
fun CareJourneyScreen(
    viewModel: CareJourneyViewModel = viewModel(),
    onOpenDiagnosis: () -> Unit = {},
    onOpenAppointment: (String) -> Unit = {},
    onOpenMedications: () -> Unit = {},
    onOpenSideEffects: () -> Unit = {},
    onAddSideEffect: () -> Unit = {},
    onOpenQuestions: () -> Unit = {},
    onOpenRecords: () -> Unit = {},
    onOpenEducation: () -> Unit = {},
    onShareSummary: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val p = lungCarePalette()

    Box(Modifier.fillMaxSize().background(p.background)) {
        when (val s = uiState) {
            is CareJourneyUiState.Loading -> LoadingState(p)
            is CareJourneyUiState.Error -> ErrorState(p, s.messageRes) { viewModel.load() }
            is CareJourneyUiState.Content -> Content(
                p = p,
                data = s.data,
                journey = viewModel.journey,
                onOpenDiagnosis = onOpenDiagnosis,
                onOpenAppointment = onOpenAppointment,
                onOpenMedications = onOpenMedications,
                onOpenSideEffects = onOpenSideEffects,
                onAddSideEffect = onAddSideEffect,
                onOpenQuestions = onOpenQuestions,
                onOpenRecords = onOpenRecords,
                onOpenEducation = onOpenEducation,
                onShareSummary = onShareSummary,
                onCompleteMilestone = { viewModel.completeMilestone(it) }
            )
        }
    }
}

@Composable
private fun Content(
    p: LungCarePalette,
    data: CareJourneyState,
    journey: JourneyType,
    onOpenDiagnosis: () -> Unit,
    onOpenAppointment: (String) -> Unit,
    onOpenMedications: () -> Unit,
    onOpenSideEffects: () -> Unit,
    onAddSideEffect: () -> Unit,
    onOpenQuestions: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenEducation: () -> Unit,
    onShareSummary: () -> Unit,
    onCompleteMilestone: (String) -> Unit
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { show = true }

    val nextAppointment = data.appointments.firstOrNull { it.status == AppointmentStatus.UPCOMING }
    val severeCount = data.sideEffects.count { it.severity == SideEffectSeverity.SEVERE }
    val timeline = data.appointments.sortedWith(
        compareBy({ it.status != AppointmentStatus.UPCOMING }, { it.dateTime })
    )

    LazyColumn(
        state = rememberLazyListState(),
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ─── Header ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 5 }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.care_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                        Text(stringResource(R.string.care_subtitle), fontSize = 12.sp, color = p.textSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                    SourceTag(p, SourceType.DEMO, VerificationStatus.UNVERIFIED)
                }
            }
        }

        // ── Journey gate (non-diagnosed users) ──
        if (journey != JourneyType.DIAGNOSED) {
            item {
                AnimatedVisibility(show, enter = fadeIn(tween(500, 100))) {
                    InfoBanner(
                        p, icon = Icons.Default.FavoriteBorder,
                        title = stringResource(R.string.my_health_journey),
                        body = stringResource(R.string.not_diagnosed_body),
                        color = p.info
                    )
                }
            }
        }

        // ─── Diagnosis hero ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(500, 150)) + slideInVertically(tween(500, 150)) { it / 4 }) {
                DiagnosisHero(p, data.diagnosis, onOpenDiagnosis)
            }
        }

        // ─── Next up ──
        if (nextAppointment != null) {
            item {
                AnimatedVisibility(show, enter = fadeIn(tween(500, 220)) + slideInVertically(tween(500, 220)) { it / 4 }) {
                    NextUpCard(p, nextAppointment, onOpenAppointment)
                }
            }
        }

        // ─── Timeline ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { it / 4 }) {
                Column {
                    SectionTitle(p, stringResource(R.string.treatment_timeline))
                    Spacer(Modifier.height(10.dp))
                    timeline.forEachIndexed { index, appt ->
                        TimelineItem(
                            p = p,
                            title = appt.title,
                            subtitle = appt.facility + (appt.doctor?.let { " • $it" } ?: ""),
                            date = appt.dateTime,
                            status = appt.status,
                            type = appt.type,
                            source = appt.sourceType,
                            isLast = index == timeline.lastIndex,
                            onClick = { onOpenAppointment(appt.id) }
                        )
                    }
                }
            }
        }

        // ─── Milestones ──
        if (data.milestones.isNotEmpty()) {
            item {
                AnimatedVisibility(show, enter = fadeIn(tween(500, 420)) + slideInVertically(tween(500, 420)) { it / 4 }) {
                    Column {
                        SectionTitle(p, stringResource(R.string.follow_up_milestones))
                        Spacer(Modifier.height(10.dp))
                        CareCard(p, radius = 18.dp) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                data.milestones.forEach { m ->
                                    MilestoneRow(p, m, onComplete = { onCompleteMilestone(m.id) })
                                }
                            }
                        }
                    }
                }
            }
        }

        // ─── Medications ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(500, 520)) + slideInVertically(tween(500, 520)) { it / 4 }) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle(p, stringResource(R.string.medications_section), Modifier.weight(1f))
                        SourceTag(p, SourceType.DEMO, VerificationStatus.UNVERIFIED, small = true)
                    }
                    Spacer(Modifier.height(10.dp))
                    CareCard(p, radius = 18.dp) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.active_reminders, data.medications.count { it.isActive }),
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onOpenMedications) {
                                    Text(stringResource(R.string.manage), color = p.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            data.medications.forEach { med ->
                                MedicationRow(p, med, onClick = onOpenMedications)
                            }
                            HorizontalDivider(color = p.border)
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, null, tint = p.warning, modifier = Modifier.size(16.dp))
                                Text(stringResource(R.string.med_safety_notice), fontSize = 11.sp, color = p.textSecondary, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }
        }

        // ─── Side effects ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(500, 620)) + slideInVertically(tween(500, 620)) { it / 4 }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle(p, stringResource(R.string.side_effects_section), Modifier.weight(1f))
                        TextButton(onClick = onAddSideEffect) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = p.primary)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.log_side_effect), color = p.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    if (severeCount > 0) {
                        InfoBanner(p, icon = Icons.Default.Warning,
                            title = stringResource(R.string.severe_warning), body = "", color = p.urgent)
                    }
                    CareCard(p, radius = 18.dp) {
                        Row(
                            Modifier.fillMaxWidth().clickable(onClick = onOpenSideEffects).padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SeverityStat(p, stringResource(R.string.severity_mild), data.sideEffects.count { it.severity == SideEffectSeverity.MILD }, p.success)
                            SeverityStat(p, stringResource(R.string.severity_moderate), data.sideEffects.count { it.severity == SideEffectSeverity.MODERATE }, p.warning)
                            SeverityStat(p, stringResource(R.string.severity_severe), severeCount, p.urgent)
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.textSecondary)
                        }
                    }
                }
            }
        }

        // ─── Quick actions ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(500, 720)) + slideInVertically(tween(500, 720)) { it / 4 }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle(p, stringResource(R.string.quick_actions))
                    QuickActionsGrid(
                        p,
                        onSideEffects = onOpenSideEffects,
                        onMeds = onOpenMedications,
                        onAppointments = { nextAppointment?.let { onOpenAppointment(it.id) } },
                        onQuestions = onOpenQuestions,
                        onRecords = onOpenRecords,
                        onEducation = onOpenEducation
                    )
                }
            }
        }

        // ─── Share + disclaimer ──
        item {
            AnimatedVisibility(show, enter = fadeIn(tween(500, 800))) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onShareSummary,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, p.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = p.primary)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.share_summary), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text(
                        stringResource(R.string.care_disclaimer),
                        fontSize = 11.sp, color = p.textSecondary, lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────── Components ───────────────────────

@Composable
private fun LoadingState(p: LungCarePalette) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(color = p.primary)
            Text(stringResource(R.string.loading_care), fontSize = 13.sp, color = p.textSecondary)
        }
    }
}

@Composable
private fun ErrorState(p: LungCarePalette, messageRes: Int, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Warning, null, tint = p.urgent, modifier = Modifier.size(40.dp))
            Text(stringResource(messageRes), fontSize = 14.sp, color = p.textPrimary)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = p.primary)) {
                Text(stringResource(R.string.retry), color = p.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionTitle(p: LungCarePalette, text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.textPrimary, modifier = modifier.padding(top = 4.dp))
}

@Composable
private fun CareCard(p: LungCarePalette, radius: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius),
        color = p.surface,
        border = BorderStroke(1.dp, p.border)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun IconTile(
    p: LungCarePalette,
    icon: ImageVector,
    size: Dp = 42.dp,
    container: Color = p.softMint,
    tint: Color = p.primary
) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size.value * 0.5f).dp))
    }
}

@Composable
private fun PressScale(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(stiffness = 450f), label = "press")
    Box(
        modifier.scale(scale).clickable(interactionSource = interaction, indication = null, onClick = onClick),
        content = content
    )
}

@Composable
private fun SourceTag(p: LungCarePalette, source: SourceType, verification: VerificationStatus, small: Boolean = false) {
    val labelRes: Int
    val color: Color
    when {
        verification == VerificationStatus.CLINICIAN_CONFIRMED -> { labelRes = R.string.clinician_confirmed; color = p.success }
        source == SourceType.AI_GENERATED -> { labelRes = R.string.ai_generated; color = p.warning }
        source == SourceType.DEMO -> { labelRes = R.string.demo_data_tag; color = p.info }
        else -> { labelRes = R.string.patient_entered; color = p.textSecondary }
    }
    val shape = RoundedCornerShape(999.dp)
    val borderColor = if (verification == VerificationStatus.CLINICIAN_CONFIRMED) color else p.border
    val bg = if (verification == VerificationStatus.CLINICIAN_CONFIRMED) color.copy(alpha = 0.12f) else Color.Transparent
    Row(
        Modifier.clip(shape).background(bg).border(1.dp, borderColor, shape).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (verification == VerificationStatus.CLINICIAN_CONFIRMED) {
            Icon(Icons.Default.CheckCircle, null, tint = color, modifier = Modifier.size(if (small) 11.dp else 13.dp))
        }
        Text(stringResource(labelRes), fontSize = if (small) 9.sp else 10.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun InfoBanner(p: LungCarePalette, icon: ImageVector, title: String, body: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                if (body.isNotBlank()) {
                    Text(body, fontSize = 11.sp, color = p.textSecondary, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun DiagnosisHero(p: LungCarePalette, diagnosis: DiagnosisInfo?, onOpen: () -> Unit) {
    if (diagnosis != null) {
        CareCard(p, radius = 20.dp) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SourceTag(p, diagnosis.sourceType, diagnosis.verificationStatus)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onOpen, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Edit, stringResource(R.string.edit), tint = p.textSecondary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(diagnosis.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                if (diagnosis.details.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(diagnosis.details, fontSize = 12.sp, color = p.textSecondary, lineHeight = 17.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    diagnosis.confirmedDate?.let {
                        Column {
                            Text(stringResource(R.string.confirmed_date), fontSize = 10.sp, color = p.textSecondary)
                            Text(it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
                        }
                    }
                    Column {
                        Text(stringResource(R.string.treating_specialist), fontSize = 10.sp, color = p.textSecondary)
                        Text(diagnosis.treatingSpecialist, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
                    }
                }
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen),
            shape = RoundedCornerShape(20.dp),
            color = p.primary.copy(alpha = 0.08f),
            border = BorderStroke(1.5.dp, p.primary.copy(alpha = 0.35f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                IconTile(p, Icons.Default.Shield, size = 48.dp, container = p.primary.copy(alpha = 0.15f))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.diagnosis_not_provided), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                    Text(stringResource(R.string.diagnosis_not_provided_body), fontSize = 11.sp, color = p.textSecondary, lineHeight = 15.sp, modifier = Modifier.padding(top = 3.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.add_clinician_info), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.primary)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.primary)
            }
        }
    }
}

@Composable
private fun NextUpCard(p: LungCarePalette, appt: Appointment, onOpen: (String) -> Unit) {
    val typeIcon = appointmentIcon(appt.type)
    PressScale(onClick = { onOpen(appt.id) }) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = p.primary,
            shadowElevation = 4.dp
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(p.onPrimary.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                    Icon(typeIcon, null, tint = p.onPrimary, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.next_up), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.onPrimary.copy(alpha = 0.8f))
                    Text(appt.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = p.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(appt.dateTime + " • " + appt.facility, fontSize = 11.sp, color = p.onPrimary.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.onPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun appointmentIcon(type: AppointmentType): ImageVector = when (type) {
    AppointmentType.CONSULTATION -> Icons.Default.MedicalServices
    AppointmentType.IMAGING -> Icons.Default.DocumentScanner
    AppointmentType.INFUSION -> Icons.Default.Vaccines
    AppointmentType.LAB -> Icons.Default.Science
    AppointmentType.OTHER -> Icons.Default.Event
}

@Composable
private fun TimelineItem(
    p: LungCarePalette,
    title: String,
    subtitle: String,
    date: String,
    status: AppointmentStatus,
    type: AppointmentType,
    source: SourceType,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val statusColor: Color
    val statusRes: Int
    when (status) {
        AppointmentStatus.UPCOMING -> { statusColor = p.primary; statusRes = R.string.upcoming }
        AppointmentStatus.COMPLETED -> { statusColor = p.success; statusRes = R.string.completed }
        AppointmentStatus.MISSED -> { statusColor = p.urgent; statusRes = R.string.missed }
        AppointmentStatus.CANCELLED -> { statusColor = p.textSecondary; statusRes = R.string.cancelled }
    }

    Row(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .padding(top = 14.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(statusColor)
                    .border(3.dp, p.background, CircleShape)
            )
            if (!isLast) {
                Box(Modifier.width(2.dp).height(64.dp).background(p.border))
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 10.dp)) {
            CareCard(p, radius = 14.dp) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile(p, appointmentIcon(type), size = 38.dp, container = statusColor.copy(alpha = 0.12f), tint = statusColor)
                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(subtitle, fontSize = 11.sp, color = p.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(date, fontSize = 10.sp, color = p.textSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(statusRes), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        SourceTag(p, source, VerificationStatus.UNVERIFIED, small = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneRow(p: LungCarePalette, m: FollowUpMilestone, onComplete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = !m.isCompleted, onClick = onComplete),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (m.isCompleted) p.success else Color.Transparent)
                .border(2.dp, if (m.isCompleted) p.success else p.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (m.isCompleted) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                m.title, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = if (m.isCompleted) p.textSecondary else p.textPrimary,
                textDecoration = if (m.isCompleted) TextDecoration.LineThrough else null
            )
            m.dueDate?.let { Text(it, fontSize = 10.sp, color = p.textSecondary) }
        }
        if (!m.isCompleted) {
            Text(stringResource(R.string.mark_done), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = p.primary)
        }
    }
}

@Composable
private fun MedicationRow(p: LungCarePalette, med: Medication, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconTile(p, Icons.Default.Medication, size = 40.dp)
        Column(Modifier.weight(1f)) {
            Text(med.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.textPrimary)
            Text(med.instructions, fontSize = 11.sp, color = p.textSecondary)
            if (med.reminderTimes.isNotEmpty()) {
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    med.reminderTimes.forEach { t ->
                        Box(Modifier.clip(RoundedCornerShape(999.dp)).background(p.softMint).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Icon(Icons.Default.Notifications, null, tint = p.primary, modifier = Modifier.size(10.dp))
                                Text(t, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.primary)
                            }
                        }
                    }
                }
            }
        }
        if (med.isActive) SourceTag(p, med.sourceType, med.verificationStatus, small = true)
    }
}

@Composable
private fun SeverityStat(p: LungCarePalette, label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = p.textSecondary)
    }
}

@Composable
private fun QuickActionsGrid(
    p: LungCarePalette,
    onSideEffects: () -> Unit,
    onMeds: () -> Unit,
    onAppointments: () -> Unit,
    onQuestions: () -> Unit,
    onRecords: () -> Unit,
    onEducation: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(Modifier.weight(1f), p, Icons.Default.MonitorHeart, R.string.qa_side_effects, onSideEffects)
            QuickActionCard(Modifier.weight(1f), p, Icons.Default.Medication, R.string.qa_medications, onMeds)
            QuickActionCard(Modifier.weight(1f), p, Icons.Default.Event, R.string.qa_appointments, onAppointments)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(Modifier.weight(1f), p, Icons.Default.QuestionAnswer, R.string.qa_questions, onQuestions)
            QuickActionCard(Modifier.weight(1f), p, Icons.Default.FolderShared, R.string.qa_records, onRecords)
            QuickActionCard(Modifier.weight(1f), p, Icons.Default.MenuBook, R.string.qa_education, onEducation)
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    p: LungCarePalette,
    icon: ImageVector,
    labelRes: Int,
    onClick: () -> Unit
) {
    PressScale(modifier = modifier, onClick = onClick) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = p.surface,
            border = BorderStroke(1.dp, p.border)
        ) {
            Column(
                Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconTile(p, icon, size = 36.dp)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(labelRes), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = p.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CareJourneyPreview() {
    CareJourneyScreen()
}
