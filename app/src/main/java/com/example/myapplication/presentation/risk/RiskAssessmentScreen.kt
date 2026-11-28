package com.example.myapplication.presentation.risk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.AppStore
import com.example.myapplication.presentation.components.LungCareButton
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareNotice
import com.example.myapplication.presentation.home.homePalette
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class RiskResult(
    val date: String,
    val level: String,
    val eligibleForScreening: Boolean,
    val redFlags: Boolean,
    val summary: String
)

private enum class Smoking { NEVER, FORMER, CURRENT }

/** Four-step lung screening risk check. Educational only: it does not diagnose anything. */
@Composable
fun RiskAssessmentScreen(onBack: () -> Unit = {}) {
    val p = homePalette()
    val ar = LocalConfiguration.current.locales[0].language == "ar"
    fun tr(en: String, arText: String) = if (ar) arText else en

    var step by remember { mutableIntStateOf(0) }
    var age by remember { mutableStateOf("") }
    var smoking by remember { mutableStateOf<Smoking?>(null) }
    var packsPerDay by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    var quitYearsAgo by remember { mutableStateOf("") }
    var family by remember { mutableStateOf(false) }
    var copd by remember { mutableStateOf(false) }
    var exposure by remember { mutableStateOf(false) }
    var priorCancer by remember { mutableStateOf(false) }
    var cough by remember { mutableStateOf(false) }
    var blood by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf(false) }
    var breath by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<RiskResult?>(null) }
    var previous by remember { mutableStateOf<RiskResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { previous = AppStore.load("risk", RiskResult.serializer()).firstOrNull() }

    fun compute() {
        val a = age.toIntOrNull() ?: 0
        val packYears = (packsPerDay.toFloatOrNull() ?: 0f) * (years.toFloatOrNull() ?: 0f)
        val quit = quitYearsAgo.toIntOrNull() ?: 0
        val smokedEnough = packYears >= 20f
        val recentEnough = smoking == Smoking.CURRENT || (smoking == Smoking.FORMER && quit <= 15)
        val eligible = a in 50..80 && smokedEnough && recentEnough
        var points = 0
        if (a >= 50) points++
        if (smoking == Smoking.CURRENT) points += 2 else if (smoking == Smoking.FORMER) points++
        if (packYears >= 30f) points++
        if (family) points++
        if (copd) points++
        if (exposure) points++
        if (priorCancer) points++
        val red = blood || (cough && weight) || (breath && weight)
        val level = when {
            points >= 5 || eligible -> "HIGH"
            points >= 3 -> "MODERATE"
            else -> "LOW"
        }
        val levelText = when (level) {
            "HIGH" -> tr("Higher", "أعلى")
            "MODERATE" -> tr("Moderate", "متوسط")
            else -> tr("Lower", "أقل")
        }
        val sb = StringBuilder()
        sb.append(tr("Age: ", "السن: ")).append(a).append("\n")
        sb.append(tr("Smoking: ", "التدخين: ")).append(
            when (smoking) {
                Smoking.NEVER -> tr("never", "عمره ما دخن")
                Smoking.FORMER -> tr("former, quit $quit years ago, ", "سابقًا، بطّل من $quit سنة، ")
                Smoking.CURRENT -> tr("current, ", "حاليًا، ")
                null -> ""
            }
        )
        if (smoking != Smoking.NEVER) sb.append(String.format(Locale.US, "%.1f pack-years", packYears))
        sb.append("\n")
        val factors = listOfNotNull(
            if (family) tr("family history of lung cancer", "تاريخ عائلي لسرطان الرئة") else null,
            if (copd) tr("COPD or chronic lung disease", "انسداد رئوي أو مرض صدر مزمن") else null,
            if (exposure) tr("asbestos/radon/work exposure", "تعرض لأسبستوس/رادون/مواد في الشغل") else null,
            if (priorCancer) tr("previous cancer", "سرطان سابق") else null
        )
        if (factors.isNotEmpty()) sb.append(tr("Risk factors: ", "عوامل الخطر: ")).append(factors.joinToString(", ")).append("\n")
        val symptoms = listOfNotNull(
            if (cough) tr("cough for 3+ weeks", "كحة أكتر من 3 أسابيع") else null,
            if (blood) tr("coughing blood", "كحة بدم") else null,
            if (weight) tr("unexplained weight loss", "نزول وزن بدون سبب") else null,
            if (breath) tr("shortness of breath", "ضيق نفس") else null
        )
        if (symptoms.isNotEmpty()) sb.append(tr("Symptoms: ", "الأعراض: ")).append(symptoms.joinToString(", ")).append("\n")
        sb.append(tr("Estimated risk level: ", "مستوى الخطورة التقديري: ")).append(levelText).append("\n")
        sb.append(
            if (eligible) tr(
                "You may qualify for yearly low-dose CT screening (age 50-80, 20+ pack-years, smoking now or quit within 15 years). Talk to your doctor.",
                "ممكن تكون مؤهل لأشعة مقطعية سنوية بجرعة منخفضة (سن 50-80، 20 باكو/سنة أو أكتر، مدخن حاليًا أو بطّل من أقل من 15 سنة). اتكلم مع دكتورك."
            ) else tr(
                "Based on these answers you do not meet the usual screening criteria. This does not rule anything out.",
                "حسب إجاباتك مش مطابق لمعايير الفحص المعتادة. ده مش بيستبعد أي حاجة."
            )
        )
        val r = RiskResult(
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date()),
            level, eligible, red, sb.toString()
        )
        result = r
    }

    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(p.background)
            .background(Brush.verticalGradient(0f to p.bgGlow.copy(alpha = 0.45f), 0.35f to p.bgGlowSecondary.copy(alpha = 0.2f), 0.75f to Color.Transparent))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Back", "رجوع"), tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(tr("Lung risk check", "فحص مخاطر الرئة"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }

            val r = result
            if (r != null) {
                LungCareNotice(
                    title = tr("Your result", "نتيجتك"),
                    message = r.summary,
                    isUrgent = r.eligibleForScreening || r.level == "HIGH"
                )
                if (r.redFlags) {
                    LungCareNotice(
                        title = tr("See a doctor soon", "كلّم دكتور في أقرب وقت"),
                        message = tr(
                            "Coughing blood, or cough/breathlessness with weight loss, needs medical evaluation. If it is severe, go to emergency care.",
                            "كحة بدم، أو كحة/ضيق نفس مع نزول وزن، محتاجين كشف طبي. لو شديد روح الطوارئ."
                        ),
                        isUrgent = true
                    )
                }
                Text(
                    tr("Educational only. This is not a diagnosis and does not replace a doctor.", "للتوعية فقط. ده مش تشخيص ومش بديل عن الدكتور."),
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LungCareButton(text = tr("Save result", "احفظ النتيجة"), onClick = {
                    scope.launch { AppStore.save("risk", RiskResult.serializer(), listOf(r)); previous = r }
                    onBack()
                })
                OutlinedButton(onClick = { result = null; step = 0 }, modifier = Modifier.fillMaxWidth()) {
                    Text(tr("Start over", "ابدأ من الأول"))
                }
            } else {
                LinearProgressIndicator(progress = { (step + 1) / 4f }, modifier = Modifier.fillMaxWidth().height(6.dp), color = com.example.myapplication.core.design.lungCarePalette().primary, trackColor = com.example.myapplication.core.design.lungCarePalette().border)
                Text(tr("Step ${step + 1} of 4", "خطوة ${step + 1} من 4"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                previous?.let {
                    Text(tr("Last saved result: ${it.date}", "آخر نتيجة محفوظة: ${it.date}"), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LungCareCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        when (step) {
                            0 -> {
                                Text(tr("About you", "عنك"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                OutlinedTextField(age, { age = it.filter(Char::isDigit).take(3) }, label = { Text(tr("Age", "السن")) },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = com.example.myapplication.core.ui.appFieldColors(), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                            }
                            1 -> {
                                Text(tr("Smoking", "التدخين"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                listOf(Smoking.NEVER to tr("Never smoked", "عمري ما دخنت"), Smoking.FORMER to tr("Quit smoking", "بطّلت تدخين"), Smoking.CURRENT to tr("I smoke now", "بدخن حاليًا")).forEach { (s, label) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = smoking == s, onClick = { smoking = s })
                                        Text(label, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                if (smoking != null && smoking != Smoking.NEVER) {
                                    OutlinedTextField(packsPerDay, { packsPerDay = it.filter { c -> c.isDigit() || c == '.' }.take(4) }, label = { Text(tr("Packs per day (20 cigarettes = 1)", "علب في اليوم (20 سيجارة = علبة)")) },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = com.example.myapplication.core.ui.appFieldColors(), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                                    OutlinedTextField(years, { years = it.filter { c -> c.isDigit() || c == '.' }.take(4) }, label = { Text(tr("Years smoked", "عدد سنين التدخين")) },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = com.example.myapplication.core.ui.appFieldColors(), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                                }
                                if (smoking == Smoking.FORMER) {
                                    OutlinedTextField(quitYearsAgo, { quitYearsAgo = it.filter(Char::isDigit).take(2) }, label = { Text(tr("Years since you quit", "بقالك كام سنة بطّلت")) },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = com.example.myapplication.core.ui.appFieldColors(), shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                                }
                            }
                            2 -> {
                                Text(tr("History and exposure", "التاريخ والتعرض"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                CheckRow(tr("Family history of lung cancer", "تاريخ عائلي لسرطان الرئة"), family) { family = it }
                                CheckRow(tr("COPD or chronic lung disease", "انسداد رئوي أو مرض صدر مزمن"), copd) { copd = it }
                                CheckRow(tr("Asbestos, radon or heavy work exposure", "تعرض لأسبستوس أو رادون أو مواد في الشغل"), exposure) { exposure = it }
                                CheckRow(tr("Previous cancer", "سرطان سابق"), priorCancer) { priorCancer = it }
                            }
                            else -> {
                                Text(tr("Symptoms now", "الأعراض حاليًا"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                CheckRow(tr("Cough for more than 3 weeks", "كحة أكتر من 3 أسابيع"), cough) { cough = it }
                                CheckRow(tr("Coughing up blood", "كحة بدم"), blood) { blood = it }
                                CheckRow(tr("Unexplained weight loss", "نزول وزن بدون سبب"), weight) { weight = it }
                                CheckRow(tr("Shortness of breath", "ضيق نفس"), breath) { breath = it }
                            }
                        }
                    }
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (step > 0) {
                        OutlinedButton(onClick = { step--; error = null }, modifier = Modifier.weight(1f)) { Text(tr("Back", "رجوع")) }
                    }
                    Button(onClick = {
                        error = null
                        val a = age.toIntOrNull()
                        when {
                            step == 0 && (a == null || a !in 18..110) -> error = tr("Enter a valid age (18-110).", "اكتب سن صحيح (18-110).")
                            step == 1 && smoking == null -> error = tr("Choose one option.", "اختار إجابة.")
                            step == 1 && smoking != Smoking.NEVER && ((packsPerDay.toFloatOrNull() ?: 0f) <= 0f || (years.toFloatOrNull() ?: 0f) <= 0f) ->
                                error = tr("Enter packs per day and years smoked.", "اكتب عدد العلب والسنين.")
                            step == 1 && smoking == Smoking.FORMER && quitYearsAgo.isBlank() -> error = tr("Enter years since you quit.", "اكتب بقالك كام سنة بطّلت.")
                            step < 3 -> step++
                            else -> compute()
                        }
                    }, modifier = Modifier.weight(1f)) { Text(if (step < 3) tr("Next", "التالي") else tr("See result", "شوف النتيجة")) }
                }
            }
        }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChange)
        Text(label, color = MaterialTheme.colorScheme.onSurface)
    }
}
