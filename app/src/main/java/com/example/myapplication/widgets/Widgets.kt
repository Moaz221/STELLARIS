package com.example.myapplication.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.core.model.Appointment
import com.example.myapplication.core.model.AppointmentStatus
import com.example.myapplication.core.model.Medication
import com.example.myapplication.data.local.AppStore
import com.example.myapplication.presentation.questions.QuestionItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------- shared helpers ----------
internal object W {
    val bg = Color(0xFF0A0F1A)
    val neon = Color(0xFF2DE2C4)
    val text = Color(0xFFE6EDF7)
    val mute = Color(0xFF93A3BD)
    val amber = Color(0xFFFFB020)
    val red = Color(0xFFFF5C7A)
    val violet = Color(0xFF7C5CFF)
    fun cp(c: Color) = ColorProvider(c)
    fun isAr(): Boolean {
        val l = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        val lang = if (l.isEmpty) Locale.getDefault().language else l.get(0)?.language
        return lang == "ar"
    }
    fun t(en: String, ar: String) = if (isAr()) ar else en
    fun open(context: Context, id: Int, overlay: String? = null, extra: String? = null) =
        actionStartActivity(Intent(context, MainActivity::class.java).apply {
            action = "stellaris.widget.$id"
            data = Uri.parse("stellaris://widget/$id")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            if (overlay != null) putExtra("overlay", overlay)
            if (extra != null) putExtra("extra", extra)
        })
    fun prefs(context: Context) = context.getSharedPreferences("stellaris_prefs", Context.MODE_PRIVATE)
    fun journeyColor(j: String?) = when (j) {
        "EARLY_DETECTION" -> neon
        "UNDER_EVALUATION" -> amber
        "DIAGNOSED" -> red
        else -> violet
    }
    fun journeyLabel(j: String?) = when (j) {
        "EARLY_DETECTION" -> t("Early detection", "فحص مبكر")
        "UNDER_EVALUATION" -> t("Under evaluation", "تحت التقييم")
        "DIAGNOSED" -> t("Diagnosed", "مشخّص")
        else -> t("Choose your journey", "اختار رحلتك")
    }
}

@Composable
internal fun Card(modifier: GlanceModifier = GlanceModifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg)).padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) { content() }
}

@Composable
internal fun Title(s: String, c: Color = W.neon) = Text(s, style = TextStyle(color = W.cp(c), fontSize = 12.sp, fontWeight = FontWeight.Bold))
@Composable
internal fun Big(s: String, size: Int = 18) = Text(s, maxLines = 2, style = TextStyle(color = W.cp(W.text), fontSize = size.sp, fontWeight = FontWeight.Bold))
@Composable
internal fun Small(s: String) = Text(s, maxLines = 2, style = TextStyle(color = W.cp(W.mute), fontSize = 12.sp))

@Composable
internal fun Pill(s: String, bg: Color, fg: Color, modifier: GlanceModifier) =
    Box(modifier = modifier.background(W.cp(bg)).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(s, style = TextStyle(color = W.cp(fg), fontSize = 13.sp, fontWeight = FontWeight.Bold))
    }

suspend fun updateAllWidgets(context: Context) {
    runCatching { NextWidget().updateAll(context) }
    runCatching { JourneyWidget().updateAll(context) }
    runCatching { SymptomWidget().updateAll(context) }
    runCatching { QuestionsWidget().updateAll(context) }
    runCatching { TipWidget().updateAll(context) }
    runCatching { EmergencyWidget().updateAll(context) }
}

// ---------- 1. Next medication / appointment ----------
private val KEY_TAKEN = ActionParameters.Key<String>("taken_key")

class TakenAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val k = parameters[KEY_TAKEN] ?: return
        W.prefs(context).edit().putBoolean(k, true).apply()
        NextWidget().updateAll(context)
    }
}

class NextWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(110.dp, 110.dp), DpSize(250.dp, 110.dp), DpSize(250.dp, 180.dp)))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AppStore.init(context)
        val meds = AppStore.load("care_medications", Medication.serializer()).filter { it.isActive }
        val appts = AppStore.load("care_appointments", Appointment.serializer())
            .filter { it.status == AppointmentStatus.UPCOMING }.sortedBy { it.dateTime }
        val day = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val now = Calendar.getInstance().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }
        fun mins(s: String) = s.split(":").let { (it.getOrNull(0)?.trim()?.toIntOrNull() ?: 0) * 60 + (it.getOrNull(1)?.trim()?.toIntOrNull() ?: 0) }
        val todayDue = meds.flatMap { m -> m.reminderTimes.map { m to it } }
            .filter { (m, tm) -> !W.prefs(context).getBoolean("taken_${day}_${m.id}_$tm", false) }
            .sortedBy { mins(it.second) }
        val next = todayDue.firstOrNull()
        val tomorrow = if (next == null) meds.flatMap { m -> m.reminderTimes.map { m to it } }.minByOrNull { mins(it.second) } else null
        val appt = appts.firstOrNull()
        provideContent {
            val wide = LocalSize.current.width >= 200.dp
            Card(GlanceModifier.clickable(W.open(context, 1))) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Title(W.t("NEXT MEDICINE", "الدوا القادم"))
                    Spacer(GlanceModifier.height(4.dp))
                    when {
                        next != null -> {
                            Big(next.first.name + "  " + next.second, if (wide) 20 else 16)
                            if (wide) Small(next.first.instructions)
                            Spacer(GlanceModifier.height(6.dp))
                            Pill(W.t("Taken", "تم"), W.neon, W.bg,
                                GlanceModifier.clickable(actionRunCallback<TakenAction>(actionParametersOf(KEY_TAKEN to "taken_${day}_${next.first.id}_${next.second}"))))
                        }
                        tomorrow != null -> {
                            Big(W.t("All taken today", "خلصت جرعات النهاردة"), 16)
                            Small(W.t("Tomorrow: ", "بكرة: ") + tomorrow.first.name + " " + tomorrow.second)
                        }
                        else -> Big(W.t("No medicines yet", "لا توجد أدوية بعد"), 16)
                    }
                    if (wide) {
                        Spacer(GlanceModifier.height(8.dp))
                        Title(W.t("NEXT APPOINTMENT", "الموعد القادم"), W.violet)
                        if (appt != null) Small(appt.title + " - " + appt.dateTime) else Small(W.t("No upcoming appointment", "لا يوجد موعد قادم"))
                    }
                }
            }
        }
    }
}
class NextReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = NextWidget() }

// ---------- 2. Journey status ----------
class JourneyWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(100.dp, 100.dp), DpSize(200.dp, 100.dp)))
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val j = W.prefs(context).getString("journey", null)
        provideContent {
            val c = W.journeyColor(j)
            Card(GlanceModifier.clickable(W.open(context, 2))) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = GlanceModifier.size(14.dp).background(W.cp(c))) {}
                    Spacer(GlanceModifier.width(10.dp))
                    Column {
                        Title(W.t("MY JOURNEY", "رحلتي"), c)
                        Big(W.journeyLabel(j), 17)
                    }
                }
            }
        }
    }
}
class JourneyReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = JourneyWidget() }

// ---------- 3. Quick symptom log ----------
class SymptomWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Card(GlanceModifier.clickable(W.open(context, 3, "SYMPTOMS_TRACKER"))) {
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Title(W.t("HOW DO YOU FEEL?", "حاسس بإيه؟"))
                        Small(W.t("Log a symptom", "سجّل عرض"))
                    }
                    Pill("+  " + W.t("Log", "سجّل"), W.neon, W.bg, GlanceModifier)
                }
            }
        }
    }
}
class SymptomReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = SymptomWidget() }

// ---------- 4. Doctor questions count ----------
class QuestionsWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Single
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        AppStore.init(context)
        val n = AppStore.load("questions", QuestionItem.serializer()).count { it.status == "ACTIVE" }
        provideContent {
            Card(GlanceModifier.clickable(W.open(context, 4, "DOCTOR_QUESTIONS"))) {
                Column {
                    Title(W.t("DOCTOR QUESTIONS", "أسئلة الدكتور"), W.violet)
                    Text(n.toString(), style = TextStyle(color = W.cp(W.text), fontSize = 36.sp, fontWeight = FontWeight.Bold))
                    Small(W.t("waiting for your visit", "مستنية زيارتك"))
                }
            }
        }
    }
}
class QuestionsReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = QuestionsWidget() }

// ---------- 5. Daily tip ----------
private val tipsEn = listOf(
    "Regular screening and early detection improve lung health outcomes.",
    "If you smoke, ask your doctor about help to quit. Every day without smoking counts.",
    "Write your questions before a visit so you do not forget them.",
    "Take medicines at the same times each day. Reminders help.",
    "A cough lasting more than three weeks is worth showing to a doctor.",
    "Coughing blood or severe breathlessness is an emergency. Call 123 or go to a hospital.",
    "Keep your reports and scans together so you can show them quickly."
)
private val tipsAr = listOf(
    "الفحص الدوري والاكتشاف المبكر بيحسّنوا نتايج صحة الرئة.",
    "لو بتدخن اسأل دكتورك عن مساعدة للإقلاع. كل يوم من غير تدخين بيفرق.",
    "اكتب أسئلتك قبل الزيارة عشان متنساهاش.",
    "خد أدويتك في نفس المواعيد كل يوم. التنبيهات بتساعد.",
    "الكحة اللي بتستمر أكتر من 3 أسابيع تستاهل تتعرض على دكتور.",
    "الكحة بدم أو ضيق التنفس الشديد حالة طوارئ. اتصل 123 أو روح مستشفى.",
    "احتفظ بتقاريرك وأشعتك مع بعض عشان تعرضها بسرعة."
)
class TipWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(110.dp, 110.dp), DpSize(250.dp, 110.dp)))
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val i = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % tipsEn.size
        provideContent {
            val wide = LocalSize.current.width >= 200.dp
            Card(GlanceModifier.clickable(W.open(context, 5, "EDUCATION_HUB"))) {
                Column {
                    Title(W.t("DAILY TIP", "نصيحة اليوم"))
                    Spacer(GlanceModifier.height(4.dp))
                    Text(W.t(tipsEn[i], tipsAr[i]), maxLines = if (wide) 5 else 6, style = TextStyle(color = W.cp(W.text), fontSize = if (wide) 15.sp else 12.sp))
                }
            }
        }
    }
}
class TipReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = TipWidget() }

// ---------- 6. Emergency shortcut ----------
class EmergencyWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(100.dp, 100.dp), DpSize(250.dp, 60.dp)))
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier.fillMaxSize().background(ImageProvider(R.drawable.widget_bg_red)).padding(12.dp)
                    .clickable(W.open(context, 6, "SYMPTOMS_TRACKER", "emergency")),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(W.t("EMERGENCY", "طوارئ"), style = TextStyle(color = W.cp(W.red), fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Small(W.t("Tap to choose a number", "اضغط لاختيار رقم"))
                }
            }
        }
    }
}
class EmergencyReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget: GlanceAppWidget = EmergencyWidget() }
