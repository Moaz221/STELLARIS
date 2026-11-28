package com.example.myapplication.presentation.myhealth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.ui.loc
import com.example.myapplication.data.local.AppStore
import com.example.myapplication.presentation.home.homePalette
import com.example.myapplication.presentation.questions.QuestionItem
import com.example.myapplication.presentation.records.MedicalRecord as SavedRecord
import com.example.myapplication.presentation.records.RecordStatus
import com.example.myapplication.presentation.records.RecordType
import com.example.myapplication.presentation.symptoms.SymptomCategory
import com.example.myapplication.presentation.symptoms.TrackedSymptom
import kotlinx.coroutines.launch

private enum class Kind { QUESTION, RECORD, SYMPTOM }

/** Add, edit and delete questions, records and symptoms without leaving My Health. Saved on the phone only. */
@Composable
fun QuickManageCard(version: Int, onChanged: () -> Unit) {
    val p = homePalette()
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf(listOf<QuestionItem>()) }
    var records by remember { mutableStateOf(listOf<SavedRecord>()) }
    var symptoms by remember { mutableStateOf(listOf<TrackedSymptom>()) }
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(version, tick) {
        questions = AppStore.load("questions", QuestionItem.serializer()).filter { it.status == "ACTIVE" }
        records = AppStore.load("records", SavedRecord.serializer())
        symptoms = AppStore.load("symptoms", TrackedSymptom.serializer())
    }
    // dialog state: kind + id (null = new)
    var dKind by remember { mutableStateOf<Kind?>(null) }
    var dId by remember { mutableStateOf<String?>(null) }
    var text by remember { mutableStateOf("") }
    var level by remember { mutableIntStateOf(2) }       // symptom severity 1-5
    var rType by remember { mutableStateOf(RecordType.LAB) }
    var rStatus by remember { mutableStateOf(RecordStatus.PENDING) }

    fun done() { tick++; onChanged() }
    fun today() = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(p.cardTop, p.cardBottom)))
            .border(1.dp, p.borderBright, RoundedCornerShape(20.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(loc("Quick add and edit", "إضافة وتعديل سريع"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { text = ""; dId = null; dKind = Kind.QUESTION }) { Text("+ " + loc("Question", "سؤال"), color = p.accent, fontSize = 12.sp) }
            OutlinedButton(onClick = { text = ""; dId = null; rType = RecordType.LAB; rStatus = RecordStatus.PENDING; dKind = Kind.RECORD }) { Text("+ " + loc("Record", "سجل"), color = p.accent, fontSize = 12.sp) }
            OutlinedButton(onClick = { text = ""; dId = null; level = 2; dKind = Kind.SYMPTOM }) { Text("+ " + loc("Symptom", "عرض"), color = p.accent, fontSize = 12.sp) }
        }
        @Composable
        fun rowFor(label: String, onEdit: () -> Unit, onDelete: () -> Unit) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, fontSize = 13.sp, color = p.textPrimary, maxLines = 1, modifier = Modifier.weight(1f))
                TextButton(onClick = onEdit) { Text(loc("Edit", "تعديل"), color = p.accent, fontSize = 12.sp) }
                TextButton(onClick = onDelete) { Text(loc("Delete", "حذف"), color = p.alert, fontSize = 12.sp) }
            }
        }
        if (questions.isNotEmpty()) Text(loc("Questions", "الأسئلة"), fontSize = 12.sp, color = p.textSecondary)
        questions.take(3).forEach { q ->
            rowFor(q.text, { text = q.text; dId = q.id; dKind = Kind.QUESTION }) {
                scope.launch {
                    val all = AppStore.load("questions", QuestionItem.serializer())
                    AppStore.save("questions", QuestionItem.serializer(), all.map { if (it.id == q.id) it.copy(status = "DELETED", deletedAt = System.currentTimeMillis()) else it })
                    done()
                }
            }
        }
        if (records.isNotEmpty()) Text(loc("Records", "السجلات"), fontSize = 12.sp, color = p.textSecondary)
        records.take(3).forEach { r ->
            rowFor(r.title, { text = r.title; dId = r.id; rType = r.type; rStatus = r.status; dKind = Kind.RECORD }) {
                scope.launch {
                    AppStore.save("records", SavedRecord.serializer(), AppStore.load("records", SavedRecord.serializer()).filter { it.id != r.id })
                    done()
                }
            }
        }
        if (symptoms.isNotEmpty()) Text(loc("Symptoms", "الأعراض"), fontSize = 12.sp, color = p.textSecondary)
        symptoms.sortedByDescending { it.timestamp }.take(3).forEach { s ->
            rowFor(s.name, { text = s.name; dId = s.id; level = s.severity.coerceIn(1, 5); dKind = Kind.SYMPTOM }) {
                scope.launch {
                    AppStore.save("symptoms", TrackedSymptom.serializer(), AppStore.load("symptoms", TrackedSymptom.serializer()).filter { it.id != s.id })
                    done()
                }
            }
        }
    }

    val kind = dKind
    if (kind != null) {
        val isNew = dId == null
        AlertDialog(
            onDismissRequest = { dKind = null },
            title = {
                Text(when (kind) {
                    Kind.QUESTION -> if (isNew) loc("Add question", "إضافة سؤال") else loc("Edit question", "تعديل السؤال")
                    Kind.RECORD -> if (isNew) loc("Add record", "إضافة سجل") else loc("Edit record", "تعديل السجل")
                    Kind.SYMPTOM -> if (isNew) loc("Add symptom", "إضافة عرض") else loc("Edit symptom", "تعديل العرض")
                })
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = kind != Kind.QUESTION,
                        label = { Text(when (kind) { Kind.QUESTION -> loc("Question", "السؤال"); Kind.RECORD -> loc("Title", "العنوان"); Kind.SYMPTOM -> loc("Symptom", "العرض") }) })
                    if (kind == Kind.SYMPTOM) {
                        Text(loc("Severity (1 mild to 5 severe)", "الشدة (1 خفيف إلى 5 شديد)"), fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            (1..5).forEach { n -> FilterChip(selected = level == n, onClick = { level = n }, label = { Text("$n") }) }
                        }
                    }
                    if (kind == Kind.RECORD) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RecordType.values().take(3).forEach { t -> FilterChip(selected = rType == t, onClick = { rType = t }, label = { Text(loc(t.label, t.labelAr), fontSize = 11.sp) }) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RecordType.values().drop(3).forEach { t -> FilterChip(selected = rType == t, onClick = { rType = t }, label = { Text(loc(t.label, t.labelAr), fontSize = 11.sp) }) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(RecordStatus.NORMAL to loc("Normal", "طبيعي"), RecordStatus.ABNORMAL to loc("Abnormal", "غير طبيعي"), RecordStatus.PENDING to loc("Pending", "قيد الانتظار")).forEach { (s, l) ->
                                FilterChip(selected = rStatus == s, onClick = { rStatus = s }, label = { Text(l, fontSize = 11.sp) })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val t = text.trim()
                    val id = dId
                    dKind = null
                    if (t.isEmpty()) return@TextButton
                    scope.launch {
                        when (kind) {
                            Kind.QUESTION -> {
                                val all = AppStore.load("questions", QuestionItem.serializer())
                                val out = if (id == null) listOf(QuestionItem(java.util.UUID.randomUUID().toString(), t, "General", createdAt = System.currentTimeMillis())) + all
                                else all.map { if (it.id == id) it.copy(text = t) else it }
                                AppStore.save("questions", QuestionItem.serializer(), out)
                            }
                            Kind.RECORD -> {
                                val all = AppStore.load("records", SavedRecord.serializer())
                                val out = if (id == null) listOf(SavedRecord(title = t, type = rType, status = rStatus, date = today())) + all
                                else all.map { if (it.id == id) it.copy(title = t, type = rType, status = rStatus) else it }
                                AppStore.save("records", SavedRecord.serializer(), out)
                            }
                            Kind.SYMPTOM -> {
                                val all = AppStore.load("symptoms", TrackedSymptom.serializer())
                                val out = if (id == null) listOf(TrackedSymptom(java.util.UUID.randomUUID().toString(), t, SymptomCategory.RESPIRATORY, level, today(), isUrgent = level >= 5)) + all
                                else all.map { if (it.id == id) it.copy(name = t, severity = level) else it }
                                AppStore.save("symptoms", TrackedSymptom.serializer(), out)
                            }
                        }
                        done()
                    }
                }) { Text(loc("Save", "حفظ")) }
            },
            dismissButton = { TextButton(onClick = { dKind = null }) { Text(loc("Cancel", "إلغاء")) } }
        )
    }
}
