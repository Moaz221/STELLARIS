package com.example.myapplication.presentation.symptoms

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.presentation.home.JourneyType
import com.example.myapplication.presentation.home.homePalette

@Composable
fun SymptomTrackerScreen(
    onBack: () -> Unit = {},
    viewModel: SymptomTrackerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val p = homePalette()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.bgGlow.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 1.1f
                    )
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            SymptomTrackerTopBar(
                journeyType = uiState.journeyType,
                onBack = onBack,
                onEmergencyCall = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:123"))
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Emergency hotline unavailable", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 40.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Emergency Safety Alert Banner (if urgent)
                if (uiState.hasUrgentAlert) {
                    item(key = "urgent_banner") {
                        EmergencySafetyBanner(
                            message = uiState.urgentAlertMessage,
                            onEmergencyCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:123"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Emergency hotline unavailable", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // Journey-Aware Guidance Header
                item(key = "journey_guidance") {
                    JourneyGuidanceCard(journeyType = uiState.journeyType)
                }

                // Stats Overview Bar
                item(key = "stats_bar") {
                    SymptomStatsRow(
                        total = uiState.symptoms.size,
                        active = uiState.symptoms.count { !it.isResolved },
                        urgent = uiState.symptoms.count { !it.isResolved && (it.isUrgent || it.severity >= 4) },
                        onExportClick = { viewModel.generateDoctorSummary() }
                    )
                }

                // Filter & Add Symptom Header
                item(key = "filter_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.symptom_tracker_logged_symptoms),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.textPrimary
                        )

                        Button(
                            onClick = { viewModel.openAddDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = p.accent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.symptom_tracker_log_symptom),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Filter Tabs
                item(key = "filter_tabs") {
                    SymptomFilterRow(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.setFilter(it) }
                    )
                }

                // Symptom Items List
                val filteredList = when (uiState.selectedFilter) {
                    SymptomFilter.ALL -> uiState.symptoms
                    SymptomFilter.ACTIVE -> uiState.symptoms.filter { !it.isResolved }
                    SymptomFilter.URGENT -> uiState.symptoms.filter { it.isUrgent || it.severity >= 4 }
                    SymptomFilter.RESOLVED -> uiState.symptoms.filter { it.isResolved }
                }

                if (filteredList.isEmpty()) {
                    item(key = "empty_state") {
                        EmptySymptomsCard(filter = uiState.selectedFilter)
                    }
                } else {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { symptom ->
                        SymptomCard(
                            symptom = symptom,
                            onToggleResolved = { viewModel.toggleResolved(symptom.id) },
                            onEdit = { viewModel.openAddDialog(symptom) },
                            onDelete = { viewModel.deleteSymptom(symptom.id) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog Sheet
    if (uiState.showAddDialog) {
        AddEditSymptomDialog(
            symptomToEdit = uiState.symptomToEdit,
            presets = viewModel.presets,
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { name, category, severity, isUrgent, notes, frequency, triggers ->
                viewModel.saveSymptom(name, category, severity, isUrgent, notes, frequency, triggers)
            }
        )
    }

    // Doctor Export Dialog
    if (uiState.showExportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeExportDialog() },
            shape = RoundedCornerShape(24.dp),
            containerColor = p.cardBottom,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Assignment, contentDescription = null, tint = p.accent)
                    Text(
                        text = stringResource(R.string.symptom_tracker_doctor_report),
                        fontWeight = FontWeight.Bold,
                        color = p.textPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 340.dp)
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(12.dp))
                        .background(p.background)
                        .padding(14.dp)
                ) {
                    Text(
                        text = uiState.doctorExportText,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = p.textPrimary
                    )
                }
            },
            confirmButton = {
                val copiedMsg = stringResource(R.string.symptom_tracker_copied_toast)
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.doctorExportText))
                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                        viewModel.closeExportDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = p.accent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.symptom_tracker_copy_summary))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeExportDialog() }) {
                    Text(stringResource(R.string.home_close), color = p.textSecondary)
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Top Bar
// ---------------------------------------------------------------------------
@Composable
private fun SymptomTrackerTopBar(
    journeyType: JourneyType,
    onBack: () -> Unit,
    onEmergencyCall: () -> Unit
) {
    val p = homePalette()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(p.cardTop)
                .border(1.dp, p.borderDim, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = p.textPrimary
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.symptom_tracker_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = p.textPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(p.accent)
                )
                Text(
                    text = when (journeyType) {
                        JourneyType.EARLY_DETECTION -> stringResource(R.string.symptom_tracker_early_mode)
                        JourneyType.UNDER_EVALUATION -> stringResource(R.string.symptom_tracker_eval_mode)
                        JourneyType.DIAGNOSED -> stringResource(R.string.symptom_tracker_care_mode)
                    },
                    fontSize = 11.sp,
                    color = p.accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Emergency Call Button
        IconButton(
            onClick = onEmergencyCall,
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape, ambientColor = p.alert)
                .clip(CircleShape)
                .background(p.alert.copy(alpha = 0.15f))
                .border(1.5.dp, p.alert, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = stringResource(R.string.symptom_tracker_call_doctor),
                tint = p.alert,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Urgent Safety Banner
// ---------------------------------------------------------------------------
@Composable
private fun EmergencySafetyBanner(
    message: String,
    onEmergencyCall: () -> Unit
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = p.alert.copy(alpha = 0.4f))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        p.alert.copy(alpha = 0.22f),
                        p.alert.copy(alpha = 0.10f)
                    )
                )
            )
            .border(1.5.dp, p.alert, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(p.alert),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.symptom_tracker_emergency_alert),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.alert
                )
            }

            Text(
                text = message,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = p.textPrimary,
                fontWeight = FontWeight.Medium
            )

            Button(
                onClick = onEmergencyCall,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = p.alert),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.symptom_tracker_call_doctor),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Journey Guidance Card
// ---------------------------------------------------------------------------
@Composable
private fun JourneyGuidanceCard(journeyType: JourneyType) {
    val p = homePalette()

    val (title, text, icon) = when (journeyType) {
        JourneyType.EARLY_DETECTION -> Triple(
            stringResource(R.string.home_step_early_title),
            stringResource(R.string.hero_subtitle),
            Icons.Outlined.Shield
        )
        JourneyType.UNDER_EVALUATION -> Triple(
            stringResource(R.string.home_step_eval_title),
            stringResource(R.string.hero_subtitle_under_eval),
            Icons.Outlined.MedicalServices
        )
        JourneyType.DIAGNOSED -> Triple(
            stringResource(R.string.home_step_care_title),
            stringResource(R.string.hero_subtitle_diagnosed),
            Icons.Outlined.MonitorHeart
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, p.borderDim, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(p.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = p.accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )
                Text(
                    text = text,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = p.textSecondary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Symptom Stats Row
// ---------------------------------------------------------------------------
@Composable
private fun SymptomStatsRow(
    total: Int,
    active: Int,
    urgent: Int,
    onExportClick: () -> Unit
) {
    val p = homePalette()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatPill(
            label = stringResource(R.string.symptom_tracker_stats_active),
            value = "$active",
            color = p.accent,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            label = stringResource(R.string.symptom_tracker_stats_urgent),
            value = "$urgent",
            color = if (urgent > 0) p.alert else p.textSecondary,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            label = stringResource(R.string.symptom_tracker_stats_total),
            value = "$total",
            color = p.textSecondary,
            modifier = Modifier.weight(1f)
        )

        IconButton(
            onClick = onExportClick,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(p.accent.copy(alpha = 0.12f))
                .border(1.dp, p.accent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = Icons.Outlined.IosShare,
                contentDescription = stringResource(R.string.symptom_tracker_export_summary),
                tint = p.accent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val p = homePalette()

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = p.textSecondary,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Filter Row
// ---------------------------------------------------------------------------
@Composable
private fun SymptomFilterRow(
    selectedFilter: SymptomFilter,
    onFilterSelected: (SymptomFilter) -> Unit
) {
    val p = homePalette()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SymptomFilter.entries.toTypedArray()) { filter ->
            val isSelected = filter == selectedFilter
            val label = when (filter) {
                SymptomFilter.ALL -> stringResource(R.string.symptom_tracker_filter_all)
                SymptomFilter.ACTIVE -> stringResource(R.string.symptom_tracker_filter_active)
                SymptomFilter.URGENT -> stringResource(R.string.symptom_tracker_filter_urgent)
                SymptomFilter.RESOLVED -> stringResource(R.string.symptom_tracker_filter_resolved)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) p.accent else p.cardTop
                    )
                    .border(
                        1.dp,
                        if (isSelected) p.accent else p.borderDim,
                        RoundedCornerShape(50)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else p.textSecondary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Symptom Card Item
// ---------------------------------------------------------------------------
@Composable
private fun SymptomCard(
    symptom: TrackedSymptom,
    onToggleResolved: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = homePalette()
    var showMenu by remember { mutableStateOf(false) }

    val severityColor = when (symptom.severity) {
        1 -> p.success
        2 -> p.accentSoft
        3 -> p.warning
        4 -> p.alert
        else -> Color(0xFFD32F2F) // Critical Red
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = severityColor.copy(alpha = 0.15f))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(
                1.dp,
                if (symptom.isUrgent) p.alert.copy(alpha = 0.6f) else p.borderDim,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Severity Indicator Bar
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(severityColor)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = symptom.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (symptom.isResolved) p.textTertiary else p.textPrimary
                        )

                        if (symptom.isUrgent) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Urgent",
                                tint = p.alert,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "${symptom.dateLogged} · ${symptom.frequency}",
                        fontSize = 11.sp,
                        color = p.textSecondary
                    )
                }

                // Severity Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Severity ${symptom.severity}/5",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }

                // Options Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = p.textSecondary)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(p.cardElevated)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (symptom.isResolved) stringResource(R.string.symptom_tracker_mark_active) else stringResource(R.string.symptom_tracker_mark_resolved),
                                    color = p.textPrimary
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = p.accent) },
                            onClick = {
                                showMenu = false
                                onToggleResolved()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.symptom_tracker_edit_entry), color = p.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = p.accent) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.symptom_tracker_delete_log), color = p.alert) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = p.alert) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Notes / Triggers
            if (symptom.notes.isNotBlank() || symptom.triggers.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(p.background.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (symptom.triggers.isNotBlank()) {
                        Text(
                            text = "Triggers: ${symptom.triggers}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = p.accent
                        )
                    }
                    if (symptom.notes.isNotBlank()) {
                        Text(
                            text = symptom.notes,
                            fontSize = 12.sp,
                            color = p.textSecondary
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty State Card
// ---------------------------------------------------------------------------
@Composable
private fun EmptySymptomsCard(filter: SymptomFilter) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(p.cardTop)
            .border(1.dp, p.borderDim, RoundedCornerShape(20.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = p.success,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = when (filter) {
                    SymptomFilter.ALL -> stringResource(R.string.symptom_tracker_no_symptoms)
                    SymptomFilter.ACTIVE -> stringResource(R.string.symptom_tracker_no_active)
                    SymptomFilter.URGENT -> stringResource(R.string.symptom_tracker_no_urgent)
                    SymptomFilter.RESOLVED -> stringResource(R.string.symptom_tracker_no_resolved)
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = p.textPrimary
            )
            Text(
                text = stringResource(R.string.symptom_tracker_empty_hint),
                fontSize = 12.sp,
                color = p.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Add / Edit Symptom Dialog Sheet
// ---------------------------------------------------------------------------
@Composable
private fun AddEditSymptomDialog(
    symptomToEdit: TrackedSymptom?,
    presets: List<SymptomPreset>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: SymptomCategory,
        severity: Int,
        isUrgent: Boolean,
        notes: String,
        frequency: String,
        triggers: String
    ) -> Unit
) {
    val p = homePalette()
    val isArabic = LocalConfiguration.current.locales[0].language.equals("ar", ignoreCase = true)

    var name by remember { mutableStateOf(symptomToEdit?.name ?: "") }
    var category by remember { mutableStateOf(symptomToEdit?.category ?: SymptomCategory.RESPIRATORY) }
    var severity by remember { mutableIntStateOf(symptomToEdit?.severity ?: 2) }
    var isUrgent by remember { mutableStateOf(symptomToEdit?.isUrgent ?: false) }
    var notes by remember { mutableStateOf(symptomToEdit?.notes ?: "") }
    var frequency by remember { mutableStateOf(symptomToEdit?.frequency ?: "Occasional") }
    var triggers by remember { mutableStateOf(symptomToEdit?.triggers ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = p.cardBottom,
        title = {
            Text(
                text = if (symptomToEdit == null) stringResource(R.string.symptom_tracker_log_new) else stringResource(R.string.symptom_tracker_edit_entry),
                fontWeight = FontWeight.Bold,
                color = p.textPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Preset Quick Pills
                if (symptomToEdit == null) {
                    Text(
                        text = stringResource(R.string.symptom_tracker_quick_preset),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.textSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presets) { preset ->
                            val presetName = if (isArabic) preset.nameAr else preset.nameEn
                            val isSelected = name == presetName || name == preset.nameEn || name == preset.nameAr

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) p.accent else p.background)
                                    .border(1.dp, p.borderDim, RoundedCornerShape(8.dp))
                                    .clickable {
                                        name = presetName
                                        category = preset.category
                                        severity = preset.defaultSeverity
                                        isUrgent = preset.isUrgentByDefault
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = presetName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else p.textPrimary
                                )
                            }
                        }
                    }
                }

                // Symptom Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.symptom_tracker_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Severity Slider (1 - 5)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.symptom_tracker_severity_label),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = p.textSecondary
                        )
                        Text(
                            text = "Severity $severity/5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (severity) {
                                1 -> p.success
                                2 -> p.accent
                                3 -> p.warning
                                else -> p.alert
                            }
                        )
                    }

                    Slider(
                        value = severity.toFloat(),
                        onValueChange = {
                            severity = it.toInt()
                            if (severity >= 4) isUrgent = true
                        },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = p.accent,
                            activeTrackColor = p.accent
                        )
                    )
                }

                // Frequency Segment Pills
                Text(
                    text = stringResource(R.string.symptom_tracker_frequency_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = p.textSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Occasional", "Frequent", "Constant").forEach { freq ->
                        val isSelected = frequency == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) p.accent.copy(alpha = 0.2f) else p.background)
                                .border(1.dp, if (isSelected) p.accent else p.borderDim, RoundedCornerShape(8.dp))
                                .clickable { frequency = freq }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) p.accent else p.textPrimary
                            )
                        }
                    }
                }

                // Triggers Field
                OutlinedTextField(
                    value = triggers,
                    onValueChange = { triggers = it },
                    label = { Text(stringResource(R.string.symptom_tracker_triggers_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.symptom_tracker_notes_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, category, severity, isUrgent, notes, frequency, triggers)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = p.accent)
            ) {
                Text(
                    text = stringResource(R.string.symptom_tracker_save_entry),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = p.textSecondary
                )
            }
        }
    )
}
