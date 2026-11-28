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
import com.example.myapplication.core.ui.*
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
        "diagnosis" -> tr("Diagnosis from your doctor", "التشخيص من الطبيب")
        else -> tr("Side effects", "الأعراض الجانبية")
    }
    val p = com.example.myapplication.core.design.lungCarePalette()

    FullScreenForm(title = heading, onClose = onDismiss) {
        if (kind != "diagnosis") {
            val rows: List<Triple<String, String, String>> = when (kind) {
                "appointment" -> data.appointments.map { Triple(it.id, it.title, "${it.dateTime} ${it.facility}") }
                "medication" -> data.medications.map { Triple(it.id, it.name, (it.reminderTimes.joinToString(", ") + " " + it.instructions).trim()) }
                else -> data.sideEffects.map { Triple(it.id, it.name, "${it.severity} ${it.date}") }
            }
            Text(tr("Saved", "المحفوظ"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = p.textPrimary)
            if (rows.isEmpty()) {
                Text(tr("Nothing added yet.", "لسه مفيش حاجة مضافة."), fontSize = 14.sp, color = p.textSecondary)
            }
            rows.forEach { (id, t, sub) ->
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp), color = p.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, p.border)) {
                    Row(Modifier.padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = p.textPrimary)
                            if (sub.isNotBlank()) Text(sub, fontSize = 13.sp, color = p.textSecondary)
                        }
                        IconButton(onClick = { viewModel.delete(kind, id) }) {
                            Icon(Icons.Default.Delete, contentDescription = tr("Delete", "حذف"), tint = p.urgent)
                        }
                    }
                }
            }
            HorizontalDivider(color = p.border)
            Text(tr("Add new", "إضافة جديد"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = p.textPrimary)
        }
        AppField(title, { title = it }, singleLine = true,
            label = when (kind) { "appointment" -> tr("Title (e.g. Chest clinic)", "العنوان (مثلا عيادة صدر)"); "medication" -> tr("Medicine name", "اسم الدواء"); "diagnosis" -> tr("Diagnosis (as written by your doctor)", "التشخيص (زي ما كتبه الدكتور)"); else -> tr("Side effect", "العرض الجانبي") })
        when (kind) {
            "appointment" -> {
                AppField(detail, { detail = it }, singleLine = true, label = tr("Date and time (e.g. 2026-12-05 10:30)", "التاريخ والوقت (مثلا 2026-12-05 10:30)"))
                AppField(extra, { extra = it }, singleLine = true, label = tr("Place / doctor", "المكان / الدكتور"))
            }
            "medication" -> {
                AppField(detail, { detail = it }, singleLine = true, label = tr("Dose / instructions", "الجرعة / التعليمات"))
                AppField(extra, { extra = it }, singleLine = true, label = tr("Reminder times 24h, comma separated (08:00, 20:00)", "مواعيد التنبيه 24 ساعة بفاصلة (08:00, 20:00)"))
            }
            "diagnosis" -> {
                AppField(detail, { detail = it }, minLines = 3, label = tr("Details", "تفاصيل"))
                AppField(extra, { extra = it }, singleLine = true, label = tr("Treating doctor", "الدكتور المعالج"))
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SideEffectSeverity.values().forEach { s ->
                        val sel = severity == s
                        Surface(
                            onClick = { severity = s },
                            modifier = Modifier.weight(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                            color = if (sel) p.primary else p.surface,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) p.primary else p.textSecondary)
                        ) {
                            Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(when (s) { SideEffectSeverity.MILD -> tr("Mild", "خفيف"); SideEffectSeverity.MODERATE -> tr("Moderate", "متوسط"); else -> tr("Severe", "شديد") },
                                    fontWeight = FontWeight.Bold, color = if (sel) p.onPrimary else p.textPrimary)
                            }
                        }
                    }
                }
                AppField(detail, { detail = it }, minLines = 2, label = tr("Notes", "ملاحظات"))
            }
        }
        error?.let { Text(it, color = p.urgent, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
        AppPrimaryButton(text = if (kind == "diagnosis") tr("Save", "حفظ") else tr("Add", "إضافة"), onClick = {
            error = null
            if (title.isBlank()) { error = tr("Enter a name.", "اكتب الاسم."); return@AppPrimaryButton }
            when (kind) {
                "appointment" -> {
                    if (detail.isBlank()) { error = tr("Enter date and time.", "اكتب التاريخ والوقت."); return@AppPrimaryButton }
                    viewModel.addAppointment(title.trim(), extra.trim(), detail.trim(), "")
                }
                "medication" -> {
                    val times = extra.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val ok = times.all { t -> Regex("^([01]?\\d|2[0-3]):[0-5]\\d$").matches(t) }
                    if (!ok) { error = tr("Times must look like 08:00 or 20:30.", "الوقت لازم يبقى زي 08:00 أو 20:30."); return@AppPrimaryButton }
                    viewModel.addMedication(title.trim(), detail.trim(), times)
                    if (times.isNotEmpty() && Build.VERSION.SDK_INT >= 33 && !ReminderScheduler.canNotify(context)) {
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                "diagnosis" -> viewModel.setDiagnosis(title.trim(), detail.trim(), extra.trim())
                else -> viewModel.addSideEffect(title.trim(), severity, detail.trim())
            }
            android.widget.Toast.makeText(context, tr("Saved", "تم الحفظ"), android.widget.Toast.LENGTH_SHORT).show()
            onDismiss()
        })
        AppSecondaryButton(tr("Close", "إغلاق"), onDismiss)
    }
}
