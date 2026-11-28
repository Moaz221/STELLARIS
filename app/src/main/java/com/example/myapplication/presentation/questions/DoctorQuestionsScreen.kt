package com.example.myapplication.presentation.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareTag
import com.example.myapplication.presentation.home.homePalette

@kotlinx.serialization.Serializable
data class QuestionItem(
    val id: String,
    val text: String,
    val category: String,
    var isAnswered: Boolean = false,
    // Added in build 12. Defaults keep old saved questions loading as active (no data loss).
    val status: String = "ACTIVE",      // ACTIVE | DONE | DELETED
    val createdAt: Long = 0L,
    val completedAt: Long? = null,
    val deletedAt: Long? = null
)

private fun fmtDate(ms: Long?): String =
    if (ms == null || ms <= 0L) "" else java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ms))

@Composable
fun DoctorQuestionsScreen(
    onBack: () -> Unit = {}
) {
    val p = com.example.myapplication.core.design.lungCarePalette()
    var questionsList by remember { mutableStateOf(emptyList<QuestionItem>()) }
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        questionsList = com.example.myapplication.data.local.AppStore.load("questions", QuestionItem.serializer())
            .map { if (it.isAnswered && it.status == "ACTIVE") it.copy(status = "DONE", completedAt = it.completedAt ?: it.createdAt) else it }
        loaded = true
    }
    LaunchedEffect(questionsList, loaded) {
        if (loaded) com.example.myapplication.data.local.AppStore.save("questions", QuestionItem.serializer(), questionsList)
    }

    var showDialog by remember { mutableStateOf(false) }
    var newQuestionText by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val msgDone = com.example.myapplication.core.ui.loc("Moved to history as asked", "اتنقل للسجل كـ \"تم السؤال\"")
    val msgDeleted = com.example.myapplication.core.ui.loc("Question deleted", "اتحذف السؤال")
    val undo = com.example.myapplication.core.ui.loc("Undo", "تراجع")

    fun update(id: String, f: (QuestionItem) -> QuestionItem) {
        questionsList = questionsList.map { if (it.id == id) f(it) else it }
    }
    fun withUndo(id: String, message: String) {
        scope.launch {
            val r = snackbar.showSnackbar(message, actionLabel = undo, duration = SnackbarDuration.Short)
            if (r == SnackbarResult.ActionPerformed) update(id) { it.copy(status = "ACTIVE", isAnswered = false, completedAt = null, deletedAt = null) }
        }
    }

    val active = questionsList.filter { it.status == "ACTIVE" }
    val history = questionsList.filter { it.status != "ACTIVE" }
        .sortedByDescending { it.deletedAt ?: it.completedAt ?: 0L }

    Box(Modifier.fillMaxSize().background(p.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = com.example.myapplication.core.ui.loc("Back", "رجوع"), tint = p.textPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.questions_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                    Text(
                        com.example.myapplication.core.ui.loc("Prepare key topics to discuss during your appointment", "جهّز النقاط المهمة تسأل عنها في الموعد"),
                        fontSize = 12.sp, color = p.textSecondary
                    )
                }
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = p.primary, contentColor = p.onPrimary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(com.example.myapplication.core.ui.loc("Add", "إضافة"), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    com.example.myapplication.core.ui.loc("Active (${active.size})", "الحالية (${active.size})"),
                    com.example.myapplication.core.ui.loc("History (${history.size})", "السجل (${history.size})")
                ).forEachIndexed { i, label ->
                    val sel = tab == i
                    Surface(
                        onClick = { tab = i }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        color = if (sel) p.primary else p.surface,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) p.primary else p.textSecondary)
                    ) {
                        Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontWeight = FontWeight.Bold, color = if (sel) p.onPrimary else p.textPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                if (tab == 0) {
                    if (active.isEmpty()) item {
                        Text(
                            com.example.myapplication.core.ui.loc(
                                "No questions yet. Tap Add to write what you want to ask your doctor.",
                                "لسه مفيش أسئلة. اضغط إضافة واكتب اللي عايز تسأل عنه الدكتور."
                            ), fontSize = 14.sp, color = p.textSecondary, modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                    items(active, key = { it.id }) { item ->
                        QuestionCard(p) {
                            Checkbox(
                                checked = false,
                                onCheckedChange = {
                                    update(item.id) { it.copy(status = "DONE", isAnswered = true, completedAt = System.currentTimeMillis()) }
                                    withUndo(item.id, msgDone)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = p.primary, uncheckedColor = p.textSecondary)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(item.text, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = p.textPrimary, lineHeight = 21.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(categoryLabel(item.category) + (fmtDate(item.createdAt).let { if (it.isBlank()) "" else "  •  $it" }), fontSize = 12.sp, color = p.textSecondary)
                            }
                            IconButton(onClick = {
                                update(item.id) { it.copy(status = "DELETED", deletedAt = System.currentTimeMillis()) }
                                withUndo(item.id, msgDeleted)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = com.example.myapplication.core.ui.loc("Delete", "حذف"), tint = p.urgent)
                            }
                        }
                    }
                } else {
                    if (history.isEmpty()) item {
                        Text(
                            com.example.myapplication.core.ui.loc(
                                "History is empty. Questions you check as asked or delete appear here with their date.",
                                "السجل فاضي. الأسئلة اللي تعلّم عليها \"تم السؤال\" أو تحذفها هتظهر هنا بتاريخها."
                            ), fontSize = 14.sp, color = p.textSecondary, modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                    items(history, key = { it.id }) { item ->
                        val isDeleted = item.status == "DELETED"
                        QuestionCard(p) {
                            Column(Modifier.weight(1f).padding(start = 6.dp)) {
                                Text(item.text, fontSize = 15.sp, color = p.textPrimary, lineHeight = 21.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    (if (isDeleted) com.example.myapplication.core.ui.loc("Deleted", "محذوف") else com.example.myapplication.core.ui.loc("Asked", "تم السؤال")) +
                                        "  •  " + fmtDate(if (isDeleted) item.deletedAt else item.completedAt),
                                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                    color = if (isDeleted) p.urgent else p.success
                                )
                            }
                            IconButton(onClick = { update(item.id) { it.copy(status = "ACTIVE", isAnswered = false, completedAt = null, deletedAt = null) } }) {
                                Icon(Icons.Default.Restore, contentDescription = com.example.myapplication.core.ui.loc("Restore", "استعادة"), tint = p.primary)
                            }
                            IconButton(onClick = { questionsList = questionsList.filter { it.id != item.id } }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = com.example.myapplication.core.ui.loc("Delete permanently", "حذف نهائي"), tint = p.urgent)
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 80.dp))
    }

    if (showDialog) {
        com.example.myapplication.core.ui.FullScreenForm(
            title = stringResource(R.string.add_question),
            onClose = { showDialog = false }
        ) {
            com.example.myapplication.core.ui.AppField(
                value = newQuestionText,
                onValueChange = { newQuestionText = it },
                label = com.example.myapplication.core.ui.loc("Question text", "نص السؤال"),
                minLines = 3
            )
            com.example.myapplication.core.ui.AppPrimaryButton(
                text = com.example.myapplication.core.ui.loc("Save", "حفظ"),
                enabled = newQuestionText.isNotBlank(),
                onClick = {
                    questionsList = listOf(
                        QuestionItem(
                            id = System.currentTimeMillis().toString(),
                            text = newQuestionText.trim(),
                            category = "General",
                            createdAt = System.currentTimeMillis()
                        )
                    ) + questionsList
                    newQuestionText = ""
                    showDialog = false
                    tab = 0
                }
            )
            com.example.myapplication.core.ui.AppSecondaryButton(
                com.example.myapplication.core.ui.loc("Cancel", "إلغاء"), { showDialog = false })
        }
    }
}

@Composable
private fun categoryLabel(c: String): String =
    if (c == "General") com.example.myapplication.core.ui.loc("General", "عام") else c

@Composable
private fun QuestionCard(p: com.example.myapplication.core.design.LungCarePalette, content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp), color = p.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, p.border), modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 6.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, content = content)
    }
}
