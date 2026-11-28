package com.example.myapplication.presentation.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var isAnswered: Boolean = false
)

@Composable
fun DoctorQuestionsScreen(
    onBack: () -> Unit = {}
) {
    val p = homePalette()
    var questionsList by remember {
        mutableStateOf(emptyList<QuestionItem>())
    }

    var loaded by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        questionsList = com.example.myapplication.data.local.AppStore.load("questions", QuestionItem.serializer())
        loaded = true
    }
    androidx.compose.runtime.LaunchedEffect(questionsList, loaded) {
        if (loaded) com.example.myapplication.data.local.AppStore.save("questions", QuestionItem.serializer(), questionsList)
    }

    var showDialog by remember { mutableStateOf(false) }
    var newQuestionText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .background(
                Brush.verticalGradient(
                    0f to p.bgGlow.copy(alpha = 0.45f),
                    0.35f to p.bgGlowSecondary.copy(alpha = 0.20f),
                    0.75f to Color.Transparent
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = com.example.myapplication.core.ui.loc("Back", "رجوع"),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.questions_title),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = com.example.myapplication.core.ui.loc("Prepare key topics to discuss during your appointment", "جهّز النقاط المهمة تسأل عنها في الموعد"),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(com.example.myapplication.core.ui.loc("Add", "إضافة"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (questionsList.isEmpty()) {
                item {
                    Text(
                        text = com.example.myapplication.core.ui.loc(
                            "No questions yet. Tap Add to write what you want to ask your doctor.",
                            "لسه مفيش أسئلة. اضغط إضافة واكتب اللي عايز تسأل عنه الدكتور."
                        ),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            }
            items(questionsList) { item ->
                LungCareCard(
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = item.isAnswered,
                                onCheckedChange = { checked ->
                                    questionsList = questionsList.map {
                                        if (it.id == item.id) it.copy(isAnswered = checked) else it
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Column {
                                Text(
                                    text = item.text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (item.isAnswered) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LungCareTag(
                                    text = item.category,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
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
                    questionsList = questionsList + QuestionItem(
                        id = System.currentTimeMillis().toString(),
                        text = newQuestionText.trim(),
                        category = "General",
                        isAnswered = false
                    )
                    newQuestionText = ""
                    showDialog = false
                }
            )
            com.example.myapplication.core.ui.AppSecondaryButton(
                com.example.myapplication.core.ui.loc("Cancel", "إلغاء"), { showDialog = false })
        }
    }
}
}
