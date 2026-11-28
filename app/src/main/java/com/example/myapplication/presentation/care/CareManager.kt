package com.example.myapplication.presentation.care

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.model.*
import com.example.myapplication.core.reminders.ReminderScheduler

/** List + add form for appointments, medications and side effects. kind: appointment | medication | side_effect */
@Composable
fun CareManagerDialog(
    kind: String,
    data: CareJourneyState,
    viewModel: CareJourneyViewModel,
    onDismiss: () -> Unit
) {
    val ar = LocalConfiguration.current.locales[0].language == "ar"
    fun tr(en: String, arText: String) = if (ar) arText else en
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf(SideEffectSeverity.MILD) }
    var error by remember { mutableStateOf<String?>(null) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    val heading = when (kind) {
        "appointment" -> tr("Appointments", "المواعيد")
        "medication" -> tr("Medications", "الأدوية")
        else -> tr("Side effects", "الأعراض الجانبية")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(heading) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // existing items
                val rows: List<Triple<String, String, String>> = when (kind) {
                    "appointment" -> data.appointments.map { Triple(it.id, it.title, "${it.dateTime} ${it.facility}") }
                    "medication" -> data.medications.map { Triple(it.id, it.name, (it.reminderTimes.joinToString(", ") + " " + it.instructions).trim()) }
                    else -> data.sideEffects.map { Triple(it.id, it.name, "${it.severity} ${it.date}") }
                }
                if (rows.isEmpty()) {
                    Text(tr("Nothing added yet.", "لسه مفيش حاجة مضافة."), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                rows.forEach { (id, t, sub) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (sub.isNotBlank()) Text(sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.delete(kind, id) }) {
                            Icon(Icons.Default.Delete, contentDescription = tr("Delete", "حذف"))
                        }
                    }
                }
                HorizontalDivider()
                Text(tr("Add new", "إضافة جديد"), fontWeight = FontWeight.Bold)
                OutlinedTextField(title, { title = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    label = { Text(when (kind) { "appointment" -> tr("Title (e.g. Chest clinic)", "العنوان (مثلا عيادة صدر)"); "medication" -> tr("Medicine name", "اسم الدواء"); else -> tr("Side effect", "العرض الجانبي") }) })
                when (kind) {
                    "appointment" -> {
                        OutlinedTextField(detail, { detail = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(tr("Date and time (e.g. 2026-12-05 10:30)", "التاريخ والوقت (مثلا 2026-12-05 10:30)")) })
                        OutlinedTextField(extra, { extra = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(tr("Place / doctor", "المكان / الدكتور")) })
                    }
                    "medication" -> {
                        OutlinedTextField(detail, { detail = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text(tr("Dose / instructions", "الجرعة / التعليمات")) })
                        OutlinedTextField(extra, { extra = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            label = { Text(tr("Reminder times 24h, comma separated (08:00, 20:00)", "مواعيد التنبيه 24 ساعة بفاصلة (08:00, 20:00)")) })
                    }
                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SideEffectSeverity.values().forEach { s ->
                                FilterChip(selected = severity == s, onClick = { severity = s }, label = {
                                    Text(when (s) { SideEffectSeverity.MILD -> tr("Mild", "خفيف"); SideEffectSeverity.MODERATE -> tr("Moderate", "متوسط"); else -> tr("Severe", "شديد") })
                                })
                            }
                        }
                        OutlinedTextField(detail, { detail = it }, modifier = Modifier.fillMaxWidth(), label = { Text(tr("Notes", "ملاحظات")) })
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = null
                if (title.isBlank()) { error = tr("Enter a name.", "اكتب الاسم."); return@TextButton }
                when (kind) {
                    "appointment" -> {
                        if (detail.isBlank()) { error = tr("Enter date and time.", "اكتب التاريخ والوقت."); return@TextButton }
                        viewModel.addAppointment(title.trim(), extra.trim(), detail.trim(), "")
                    }
                    "medication" -> {
                        val times = extra.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val ok = times.all { t -> Regex("^([01]?\\d|2[0-3]):[0-5]\\d$").matches(t) }
                        if (!ok) { error = tr("Times must look like 08:00 or 20:30.", "الوقت لازم يبقى زي 08:00 أو 20:30."); return@TextButton }
                        viewModel.addMedication(title.trim(), detail.trim(), times)
                        if (times.isNotEmpty() && Build.VERSION.SDK_INT >= 33 && !ReminderScheduler.canNotify(context)) {
                            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    else -> viewModel.addSideEffect(title.trim(), severity, detail.trim())
                }
                title = ""; detail = ""; extra = ""
            }) { Text(tr("Add", "إضافة")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Close", "إغلاق")) } }
    )
}
