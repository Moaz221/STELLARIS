package com.example.myapplication.presentation.health

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareNotice
import com.example.myapplication.presentation.components.LungCareTag

data class SymptomItem(
    val id: String,
    val name: String,
    val severity: String,
    val date: String,
    val isUrgent: Boolean = false
)

data class MedicalRecordItem(
    val id: String,
    val title: String,
    val type: String,
    val date: String,
    val source: String
)

@Composable
fun HealthScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var symptomsList by remember {
        mutableStateOf(
            listOf(
                SymptomItem("1", "Persistent Cough", "Moderate", "Today, 10:30 AM"),
                SymptomItem("2", "Mild Chest Tightness", "Mild", "Yesterday, 4:15 PM"),
                SymptomItem("3", "Shortness of Breath", "Severe", "2 days ago", isUrgent = true)
            )
        )
    }

    var recordsList by remember {
        mutableStateOf(
            listOf(
                MedicalRecordItem("1", "Chest X-Ray Digital Copy", "Imaging", "Oct 12, 2024", "Patient Uploaded"),
                MedicalRecordItem("2", "LDCT Screening Report", "Scan Report", "Sep 04, 2024", "Clinician Confirmed"),
                MedicalRecordItem("3", "Pulmonology Consult Notes", "Doctor Notes", "Aug 18, 2024", "Clinician Confirmed")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newSymptomName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Tab Row Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        stringResource(R.string.tab_symptoms),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        stringResource(R.string.tab_records),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safety Notice
        LungCareNotice(
            title = stringResource(R.string.urgent_notice_title),
            message = stringResource(R.string.urgent_notice_desc),
            isUrgent = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == 0) stringResource(R.string.recent_symptoms) else stringResource(R.string.tab_records),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedTab == 0) stringResource(R.string.add_symptom) else stringResource(R.string.add_record),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(symptomsList) { symptom ->
                    SymptomCard(symptom)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(recordsList) { record ->
                    RecordCard(record)
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    if (selectedTab == 0) stringResource(R.string.add_symptom) else stringResource(R.string.add_record),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newSymptomName,
                    onValueChange = { newSymptomName = it },
                    label = { Text("Title / Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newSymptomName.isNotBlank()) {
                            if (selectedTab == 0) {
                                symptomsList = listOf(
                                    SymptomItem(
                                        id = System.currentTimeMillis().toString(),
                                        name = newSymptomName,
                                        severity = "Mild",
                                        date = "Just now"
                                    )
                                ) + symptomsList
                            } else {
                                recordsList = listOf(
                                    MedicalRecordItem(
                                        id = System.currentTimeMillis().toString(),
                                        title = newSymptomName,
                                        type = "General",
                                        date = "Just now",
                                        source = "Patient Entered"
                                    )
                                ) + recordsList
                            }
                            newSymptomName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SymptomCard(item: SymptomItem) {
    LungCareCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.isUrgent) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Urgent",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Logged: ${item.date}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LungCareTag(
                text = item.severity,
                containerColor = if (item.isUrgent) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = if (item.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RecordCard(item: MedicalRecordItem) {
    LungCareCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.type} • ${item.date}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LungCareTag(
                text = item.source,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
