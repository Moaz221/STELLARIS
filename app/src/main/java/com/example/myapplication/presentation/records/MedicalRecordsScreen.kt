package com.example.myapplication.presentation.records

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.presentation.home.homePalette

// ---------------------------------------------------------------------------
// Main Screen
// ---------------------------------------------------------------------------
@Composable
fun MedicalRecordsScreen(
    viewModel: MedicalRecordsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val p = homePalette()
    val filteredRecords = viewModel.getFilteredRecords()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            p.bgGlow.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
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
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Bar
            item(key = "topbar") {
                RecordsTopBar(
                    onBack = onNavigateBack,
                    onAdd = { viewModel.toggleAddSheet(true) }
                )
            }

            // Stats Row
            item(key = "stats") {
                RecordsStatsRow(uiState)
            }

            // Type Filters
            item(key = "filters") {
                RecordTypeFilterRow(
                    selectedType = uiState.selectedTypeFilter,
                    onTypeSelected = { viewModel.setTypeFilter(it) }
                )
            }

            // Records List
            if (filteredRecords.isEmpty()) {
                item(key = "empty") {
                    EmptyRecordsState(
                        onAddRecord = { viewModel.toggleAddSheet(true) }
                    )
                }
            } else {
                items(
                    items = filteredRecords,
                    key = { it.id }
                ) { record ->
                    MedicalRecordCard(
                        record = record,
                        onClick = { viewModel.openRecordDetail(record) },
                        onEdit = { viewModel.setEditingRecord(record) },
                        onDelete = { viewModel.deleteRecord(record.id) }
                    )
                }
            }

            // Source explanation
            item(key = "source_info") {
                RecordSourceInfoCard()
            }

            // Medical disclaimer
            item(key = "disclaimer") {
                RecordsDisclaimerCard()
            }
        }
    }

    // Add/Edit Bottom Sheet
    if (uiState.showAddSheet) {
        AddRecordSheet(
            editingRecord = uiState.editingRecord,
            onDismiss = { viewModel.toggleAddSheet(false) },
            onSave = { title, type, status, date, doctor, facility, summary, notes, values ->
                if (uiState.editingRecord != null) {
                    viewModel.updateRecord(
                        uiState.editingRecord!!.copy(
                            title = title,
                            type = type,
                            status = status,
                            date = date,
                            doctor = doctor,
                            facility = facility,
                            summary = summary,
                            notes = notes,
                            values = values
                        )
                    )
                } else {
                    viewModel.addRecord(title, type, status, date, doctor, facility, summary, notes, values)
                }
            }
        )
    }

    // Detail Sheet
    if (uiState.showDetailSheet && uiState.selectedRecord != null) {
        RecordDetailSheet(
            record = uiState.selectedRecord!!,
            onDismiss = { viewModel.closeRecordDetail() }
        )
    }
}

// ---------------------------------------------------------------------------
// Top Bar
// ---------------------------------------------------------------------------
@Composable
private fun RecordsTopBar(
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    val p = homePalette()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(p.cardTop)
                .border(1.dp, p.borderDim, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.records_back),
                tint = p.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.records_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = p.textPrimary
            )
            Text(
                text = stringResource(R.string.records_subtitle),
                fontSize = 13.sp,
                color = p.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(6.dp, CircleShape, ambientColor = p.accent.copy(alpha = 0.3f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(p.accent, p.accentDeep)))
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.records_add),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Stats Row
// ---------------------------------------------------------------------------
@Composable
private fun RecordsStatsRow(state: RecordsUiState) {
    val p = homePalette()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RecordStatChip(
            value = "${state.totalCount}",
            label = stringResource(R.string.records_total),
            color = p.accent,
            icon = Icons.Outlined.FolderOpen,
            modifier = Modifier.weight(1f)
        )
        RecordStatChip(
            value = "${state.pendingCount}",
            label = stringResource(R.string.records_pending),
            color = p.warning,
            icon = Icons.Outlined.HourglassEmpty,
            modifier = Modifier.weight(1f)
        )
        RecordStatChip(
            value = "${state.abnormalCount}",
            label = stringResource(R.string.records_abnormal),
            color = p.alert,
            icon = Icons.Outlined.Warning,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RecordStatChip(
    value: String,
    label: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val p = homePalette()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 11.sp, color = p.textSecondary, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------------------
// Type Filter Row
// ---------------------------------------------------------------------------
@Composable
private fun RecordTypeFilterRow(
    selectedType: RecordType?,
    onTypeSelected: (RecordType?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                label = stringResource(R.string.records_all),
                icon = Icons.Outlined.ViewList,
                isSelected = selectedType == null,
                onClick = { onTypeSelected(null) }
            )
        }
        items(RecordType.entries.toList()) { type ->
            val icon = when (type) {
                RecordType.LAB -> Icons.Outlined.Science
                RecordType.IMAGING -> Icons.Outlined.Image
                RecordType.PATHOLOGY -> Icons.Outlined.Biotech
                RecordType.VISIT -> Icons.Outlined.Person
                RecordType.PROCEDURE -> Icons.Outlined.LocalHospital
                RecordType.PRESCRIPTION -> Icons.Outlined.Medication
            }
            FilterChip(
                label = type.label,
                icon = icon,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val p = homePalette()

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) p.accent.copy(alpha = 0.15f) else p.cardTop)
            .border(
                1.dp,
                if (isSelected) p.accent.copy(alpha = 0.5f) else p.borderDim,
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 14.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) p.accent else p.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) p.accent else p.textSecondary
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Medical Record Card
// ---------------------------------------------------------------------------
@Composable
private fun MedicalRecordCard(
    record: MedicalRecord,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val p = homePalette()

    val statusColor = when (record.status) {
        RecordStatus.NORMAL -> p.success
        RecordStatus.ABNORMAL -> p.alert
        RecordStatus.PENDING -> p.warning
        RecordStatus.UNKNOWN -> p.textTertiary
    }

    val statusLabel = when (record.status) {
        RecordStatus.NORMAL -> stringResource(R.string.records_status_normal)
        RecordStatus.ABNORMAL -> stringResource(R.string.records_status_abnormal)
        RecordStatus.PENDING -> stringResource(R.string.records_status_pending)
        RecordStatus.UNKNOWN -> stringResource(R.string.records_status_unknown)
    }

    val typeIcon = when (record.type) {
        RecordType.LAB -> Icons.Outlined.Science
        RecordType.IMAGING -> Icons.Outlined.Image
        RecordType.PATHOLOGY -> Icons.Outlined.Biotech
        RecordType.VISIT -> Icons.Outlined.Person
        RecordType.PROCEDURE -> Icons.Outlined.LocalHospital
        RecordType.PRESCRIPTION -> Icons.Outlined.Medication
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(
                1.dp,
                if (record.status == RecordStatus.ABNORMAL)
                    p.alert.copy(alpha = 0.3f) else p.borderDim,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = record.type.label,
                            fontSize = 11.sp,
                            color = p.textSecondary
                        )
                        Text(text = "·", fontSize = 11.sp, color = p.textTertiary)
                        Text(
                            text = record.date,
                            fontSize = 11.sp,
                            color = p.textTertiary
                        )
                    }
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            // Summary
            if (record.summary.isNotBlank()) {
                Text(
                    text = record.summary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = p.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Doctor + Facility
            if (record.doctor.isNotBlank() || record.facility.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (record.doctor.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = p.textTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = record.doctor,
                                fontSize = 11.sp,
                                color = p.textTertiary
                            )
                        }
                    }
                    if (record.facility.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LocalHospital,
                                contentDescription = null,
                                tint = p.textTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = record.facility,
                                fontSize = 11.sp,
                                color = p.textTertiary
                            )
                        }
                    }
                }
            }

            // Verification badge
            if (record.isVerified) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = p.success,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.records_clinician_verified),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = p.success
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = p.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.records_patient_entered),
                        fontSize = 10.sp,
                        color = p.textTertiary
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Empty State
// ---------------------------------------------------------------------------
@Composable
private fun EmptyRecordsState(onAddRecord: () -> Unit) {
    val p = homePalette()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(p.accent.copy(alpha = 0.08f))
                .border(1.dp, p.borderDim, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                tint = p.textTertiary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = stringResource(R.string.records_empty_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = p.textPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(R.string.records_empty_hint),
            fontSize = 13.sp,
            color = p.textSecondary,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onAddRecord,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = p.accent.copy(alpha = 0.15f),
                contentColor = p.accent
            ),
            border = BorderStroke(1.dp, p.accent.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.records_add_first),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Record Detail Sheet
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordDetailSheet(
    record: MedicalRecord,
    onDismiss: () -> Unit
) {
    val p = homePalette()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val statusColor = when (record.status) {
        RecordStatus.NORMAL -> p.success
        RecordStatus.ABNORMAL -> p.alert
        RecordStatus.PENDING -> p.warning
        RecordStatus.UNKNOWN -> p.textTertiary
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.cardTop,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(p.borderDim)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title + Status
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = record.title,
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = p.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = when (record.status) {
                                    RecordStatus.NORMAL -> stringResource(R.string.records_status_normal)
                                    RecordStatus.ABNORMAL -> stringResource(R.string.records_status_abnormal)
                                    RecordStatus.PENDING -> stringResource(R.string.records_status_pending)
                                    RecordStatus.UNKNOWN -> stringResource(R.string.records_status_unknown)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = record.type.label, fontSize = 13.sp, color = p.accent, fontWeight = FontWeight.Medium)
                        Text(text = "·", color = p.textTertiary)
                        Text(text = record.date, fontSize = 13.sp, color = p.textSecondary)
                    }
                }
            }

            // Doctor + Facility
            if (record.doctor.isNotBlank() || record.facility.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(p.cardBottom)
                            .border(1.dp, p.borderDim, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (record.doctor.isNotBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.Person,
                                    label = stringResource(R.string.records_doctor),
                                    value = record.doctor
                                )
                            }
                            if (record.facility.isNotBlank()) {
                                DetailRow(
                                    icon = Icons.Outlined.LocalHospital,
                                    label = stringResource(R.string.records_facility),
                                    value = record.facility
                                )
                            }
                        }
                    }
                }
            }

            // Summary
            if (record.summary.isNotBlank()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.records_summary),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = p.textPrimary
                        )
                        Text(
                            text = record.summary,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = p.textSecondary
                        )
                    }
                }
            }

            // Lab Values Table
            if (record.values.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.records_lab_values),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = p.textPrimary
                        )

                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(p.accent.copy(alpha = 0.08f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.records_test),
                                modifier = Modifier.weight(2f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.accent
                            )
                            Text(
                                text = stringResource(R.string.records_result),
                                modifier = Modifier.weight(1.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.accent
                            )
                            Text(
                                text = stringResource(R.string.records_reference),
                                modifier = Modifier.weight(1.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.accent
                            )
                        }

                        // Table rows
                        record.values.forEachIndexed { _, labValue ->
                            val rowColor = if (labValue.isAbnormal)
                                p.alert.copy(alpha = 0.06f) else Color.Transparent

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowColor)
                                    .border(
                                        width = 0.5.dp,
                                        color = p.borderDim.copy(alpha = 0.5f)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = labValue.name,
                                    modifier = Modifier.weight(2f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = p.textPrimary
                                )
                                Row(
                                    modifier = Modifier.weight(1.5f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = labValue.result,
                                        fontSize = 13.sp,
                                        fontWeight = if (labValue.isAbnormal) FontWeight.Bold else FontWeight.Normal,
                                        color = if (labValue.isAbnormal) p.alert else p.textPrimary
                                    )
                                    if (labValue.isAbnormal) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = p.alert,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = labValue.referenceRange,
                                    modifier = Modifier.weight(1.5f),
                                    fontSize = 12.sp,
                                    color = p.textTertiary
                                )
                            }
                        }
                    }
                }
            }

            // Notes
            if (record.notes.isNotBlank()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.records_notes),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = p.textPrimary
                        )
                        Text(
                            text = record.notes,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = p.textSecondary
                        )
                    }
                }
            }

            // Source + Verification
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (record.isVerified) p.success.copy(alpha = 0.06f)
                            else p.warning.copy(alpha = 0.06f)
                        )
                        .border(
                            1.dp,
                            if (record.isVerified) p.success.copy(alpha = 0.2f)
                            else p.warning.copy(alpha = 0.2f),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (record.isVerified) Icons.Default.Verified
                            else Icons.Outlined.Info,
                            contentDescription = null,
                            tint = if (record.isVerified) p.success else p.warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (record.isVerified)
                                stringResource(R.string.records_verified_info)
                            else
                                stringResource(R.string.records_unverified_info),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = p.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    val p = homePalette()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = p.textTertiary, modifier = Modifier.size(16.dp))
        Text(text = "$label:", fontSize = 12.sp, color = p.textTertiary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = p.textPrimary)
    }
}

// ---------------------------------------------------------------------------
// Add/Edit Record Bottom Sheet
// ---------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRecordSheet(
    editingRecord: MedicalRecord?,
    onDismiss: () -> Unit,
    onSave: (String, RecordType, RecordStatus, String, String, String, String, String, List<LabValue>) -> Unit
) {
    val p = homePalette()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(editingRecord?.title ?: "") }
    var selectedType by remember { mutableStateOf(editingRecord?.type ?: RecordType.LAB) }
    var selectedStatus by remember { mutableStateOf(editingRecord?.status ?: RecordStatus.PENDING) }
    var doctor by remember { mutableStateOf(editingRecord?.doctor ?: "") }
    var facility by remember { mutableStateOf(editingRecord?.facility ?: "") }
    var summary by remember { mutableStateOf(editingRecord?.summary ?: "") }
    var notes by remember { mutableStateOf(editingRecord?.notes ?: "") }

    val isEditing = editingRecord != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = p.cardTop,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(p.borderDim)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    text = if (isEditing) stringResource(R.string.records_edit_title)
                    else stringResource(R.string.records_add_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = p.textPrimary
                )
            }

            // Title
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.records_field_title)) },
                    placeholder = { Text("e.g., Complete Blood Count") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = recordTextFieldColors()
                )
            }

            // Type Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.records_field_type),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.textPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(RecordType.entries.toList()) { type ->
                            val isSelected = selectedType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) p.accent.copy(alpha = 0.12f)
                                        else p.cardBottom
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) p.accent.copy(alpha = 0.5f) else p.borderDim,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = type.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) p.accent else p.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Status Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.records_field_status),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = p.textPrimary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecordStatus.entries.forEach { status ->
                            val isSelected = selectedStatus == status
                            val color = when (status) {
                                RecordStatus.NORMAL -> p.success
                                RecordStatus.ABNORMAL -> p.alert
                                RecordStatus.PENDING -> p.warning
                                RecordStatus.UNKNOWN -> p.textTertiary
                            }
                            val label = when (status) {
                                RecordStatus.NORMAL -> stringResource(R.string.records_status_normal)
                                RecordStatus.ABNORMAL -> stringResource(R.string.records_status_abnormal)
                                RecordStatus.PENDING -> stringResource(R.string.records_status_pending)
                                RecordStatus.UNKNOWN -> stringResource(R.string.records_status_unknown)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.15f)
                                        else p.cardBottom
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSelected) color else p.borderDim,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedStatus = status }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else p.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Doctor + Facility
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = doctor,
                        onValueChange = { doctor = it },
                        label = { Text(stringResource(R.string.records_doctor)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = recordTextFieldColors()
                    )
                    OutlinedTextField(
                        value = facility,
                        onValueChange = { facility = it },
                        label = { Text(stringResource(R.string.records_facility)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = recordTextFieldColors()
                    )
                }
            }

            // Summary
            item {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text(stringResource(R.string.records_summary)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    colors = recordTextFieldColors()
                )
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.records_notes)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    colors = recordTextFieldColors()
                )
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        onSave(
                            title, selectedType, selectedStatus,
                            "Today", doctor, facility, summary, notes,
                            emptyList()
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = p.accent,
                        contentColor = Color.White,
                        disabledContainerColor = p.accent.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) stringResource(R.string.records_save_changes)
                        else stringResource(R.string.records_save),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun recordTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = homePalette().accent,
    unfocusedBorderColor = homePalette().borderDim,
    focusedTextColor = homePalette().textPrimary,
    unfocusedTextColor = homePalette().textPrimary,
    cursorColor = homePalette().accent
)

// ---------------------------------------------------------------------------
// Source Info Card
// ---------------------------------------------------------------------------
@Composable
private fun RecordSourceInfoCard() {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(p.cardTop)
            .border(1.dp, p.borderDim, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.records_source_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = p.textPrimary
            )

            SourceRow(
                icon = Icons.Default.Verified,
                color = p.success,
                label = stringResource(R.string.records_source_clinician),
                desc = stringResource(R.string.records_source_clinician_desc)
            )
            SourceRow(
                icon = Icons.Outlined.Edit,
                color = p.accent,
                label = stringResource(R.string.records_source_patient),
                desc = stringResource(R.string.records_source_patient_desc)
            )
        }
    }
}

@Composable
private fun SourceRow(
    icon: ImageVector,
    color: Color,
    label: String,
    desc: String
) {
    val p = homePalette()

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = p.textPrimary)
            Text(text = desc, fontSize = 11.sp, color = p.textTertiary)
        }
    }
}

// ---------------------------------------------------------------------------
// Disclaimer
// ---------------------------------------------------------------------------
@Composable
private fun RecordsDisclaimerCard() {
    val p = homePalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(p.accent.copy(alpha = 0.06f))
            .border(1.dp, p.borderDim.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = p.textTertiary,
                modifier = Modifier.size(18.dp).padding(top = 1.dp)
            )
            Text(
                text = stringResource(R.string.records_disclaimer),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = p.textTertiary
            )
        }
    }
}
