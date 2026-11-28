package com.example.myapplication.presentation.education

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareTag
import com.example.myapplication.presentation.home.homePalette

data class ArticleItem(
    val id: String,
    val title: String,
    val summary: String,
    val readTime: String,
    val category: String,
    val body: String
)

private fun articles(ar: Boolean): List<ArticleItem> = if (ar) listOf(
    ArticleItem("1", "فهم الأشعة المقطعية منخفضة الجرعة (LDCT)", "إزاي الأشعة دي بتكشف عقد الرئة بدري قبل ما الأعراض تظهر.", "4 دقايق", "الفحص",
        "الأشعة المقطعية منخفضة الجرعة بتصوّر الرئة بجرعة إشعاع أقل من الأشعة المقطعية العادية. الغرض منها اكتشاف عقد صغيرة في الرئة بدري عند ناس معرضين لخطر أعلى ولسه معندهمش أعراض.\n\nمين بيتوصى له بيها عادة؟ الإرشادات الشائعة (زي USPSTF) بتقول: عمر 50 إلى 80 سنة، ودخّن 20 باكو/سنة أو أكتر (علبة يوميًا لمدة 20 سنة مثلًا)، ومدخن حاليًا أو بطّل من أقل من 15 سنة.\n\nإيه اللي ممكن يحصل؟ كتير من العقد بتطلع حميدة. الدكتور ممكن يطلب أشعة متابعة بعد شهور بدل ما يعمل حاجة فورًا. ساعات بيطلع نتايج غلط (إيجابية كاذبة) محتاجة فحوصات إضافية.\n\nأهم نقطة: القرار بتاخده مع دكتورك. التطبيق هنا للتوعية فقط ومش بيشخّص."),
    ArticleItem("2", "التعرف على أعراض التنفس المستمرة", "الفرق بين الكحة الموسمية العادية والأعراض اللي محتاجة كشف.", "5 دقايق", "الأعراض",
        "كلّم دكتور لو الأعراض دي استمرت أو اتكررت:\n• كحة أكتر من 3 أسابيع\n• كحة بدم أو بلغم فيه دم\n• ضيق نفس جديد أو بيزيد\n• ألم في الصدر مع التنفس أو الكحة\n• بحة صوت مستمرة\n• نزول وزن بدون سبب أو إرهاق شديد\n• التهابات صدر بتتكرر\n\nروح الطوارئ فورًا لو في كحة بدم كتير، أو ضيق نفس شديد، أو ألم صدر قوي مفاجئ.\n\nمعظم الأعراض دي ليها أسباب تانية أبسط، لكن الأحسن تتفحص. سجّل أعراضك في التطبيق وخدها معاك للدكتور."),
    ArticleItem("3", "التحضير لزيارة دكتور الصدر", "قايمة بالأوراق والأشعة والأسئلة اللي تجيبها معاك.", "3 دقايق", "دليل الرعاية",
        "قبل الزيارة جهّز:\n• كل الأشعة والتحاليل السابقة (الأفلام والتقارير)\n• قايمة الأدوية اللي بتاخدها حاليًا بالجرعات\n• تاريخ التدخين بالتقريب (كام علبة في اليوم وكام سنة)\n• التاريخ العائلي لأمراض الرئة أو السرطان\n• الأعراض ومتى بدأت وإيه اللي بيزودها\n• أسئلتك (استخدم شاشة أسئلة الدكتور في التطبيق)\n\nخد معاك حد من العيلة لو تقدر، وسجّل اللي الدكتور يقوله."),
    ArticleItem("4", "الإقلاع عن التدخين", "أهم خطوة لصحة الرئة، وفي مساعدة متاحة.", "4 دقايق", "الوقاية",
        "الإقلاع عن التدخين بيقلل خطر سرطان الرئة وأمراض الصدر في أي سن، وكل سنة بدون تدخين بتفرق.\n\nحاجات بتساعد:\n• حدد يوم للإقلاع وقول لأهلك وأصحابك\n• اسأل دكتورك عن بدائل النيكوتين أو أدوية مساعدة\n• تجنب المواقف اللي بتحفزك على التدخين\n• لو رجعت تدخن مرة، ده مش فشل، كمّل من تاني\n\nاتكلم مع دكتورك أو صيدلي عن أنسب خطة ليك."),
    ArticleItem("5", "يعني إيه عقدة في الرئة؟", "معظم العقد حميدة، وده اللي بيحصل بعد ما تتلاقى.", "3 دقايق", "الفحص",
        "العقدة الرئوية بقعة صغيرة (أقل من 3 سم) بتظهر في أشعة الصدر. غالبًا بتكون ندبة قديمة أو التهاب سابق أو نسيج حميد.\n\nالدكتور بيبص على الحجم والشكل والتغير مع الوقت. العقد الصغيرة غالبًا بتتابع بأشعة بعد شهور. العقد الأكبر أو المشكوك فيها ممكن تحتاج أشعة PET أو خزعة.\n\nماتقلقش من مجرد وجود عقدة، وماتأجلش المتابعة اللي الدكتور طلبها.")
) else listOf(
    ArticleItem("1", "Understanding Low-Dose CT (LDCT) Screening", "How LDCT scans find small lung nodules before symptoms appear.", "4 min read", "Screening",
        "A low-dose CT scan images your lungs with less radiation than a standard CT. Its goal is to find small lung nodules early in people at higher risk who have no symptoms.\n\nWho is usually advised to have it? Common guidelines (for example USPSTF) say: age 50 to 80, a smoking history of 20 pack-years or more (for example one pack a day for 20 years), and currently smoking or having quit within the last 15 years.\n\nWhat can happen? Many nodules turn out to be harmless. Your doctor may ask for a repeat scan after a few months instead of acting right away. Sometimes a scan gives a false alarm that needs more tests.\n\nThe key point: the decision is yours and your doctor's. This app is educational and does not diagnose."),
    ArticleItem("2", "Recognizing Persistent Respiratory Symptoms", "Telling a common seasonal cough from symptoms that need a check-up.", "5 min read", "Symptoms",
        "See a doctor if these last or keep coming back:\n• A cough for more than 3 weeks\n• Coughing up blood or blood-streaked mucus\n• New or worsening shortness of breath\n• Chest pain with breathing or coughing\n• Hoarseness that does not go away\n• Unexplained weight loss or heavy tiredness\n• Repeated chest infections\n\nGo to emergency care right away for heavy coughing of blood, severe shortness of breath, or sudden strong chest pain.\n\nMost of these symptoms have simpler causes, but it is best to get checked. Log your symptoms in the app and bring them to your visit."),
    ArticleItem("3", "Preparing for Your Pulmonology Consultation", "A checklist of records and questions to bring to your doctor.", "3 min read", "Care Guide",
        "Before the visit, gather:\n• Past scans and lab results (films and reports)\n• A list of your current medicines and doses\n• Your smoking history (packs per day and years)\n• Family history of lung disease or cancer\n• Your symptoms, when they started and what makes them worse\n• Your questions (use the Doctor Questions screen in the app)\n\nBring a family member if you can, and write down what the doctor says."),
    ArticleItem("4", "Quitting Smoking", "The best thing for your lungs, and help is available.", "4 min read", "Prevention",
        "Quitting lowers your risk of lung cancer and lung disease at any age, and every smoke-free year counts.\n\nWhat helps:\n• Pick a quit date and tell family and friends\n• Ask your doctor about nicotine replacement or medicines that help\n• Avoid situations that trigger you\n• If you slip once, it is not failure. Start again\n\nTalk to your doctor or pharmacist about the best plan for you."),
    ArticleItem("5", "What Is a Lung Nodule?", "Most nodules are harmless. Here is what happens after one is found.", "3 min read", "Screening",
        "A lung nodule is a small spot (under 3 cm) seen on a chest scan. Often it is an old scar, a past infection, or harmless tissue.\n\nYour doctor looks at size, shape and change over time. Small nodules are usually watched with a repeat scan after some months. Larger or suspicious ones may need a PET scan or a biopsy.\n\nHaving a nodule is not a reason to panic, and do not skip the follow-up your doctor asked for.")
)

@Composable
fun EducationHubScreen(onBack: () -> Unit = {}) {
    val p = homePalette()
    val ar = LocalConfiguration.current.locales[0].language == "ar"
    val list = remember(ar) { articles(ar) }
    var openId by remember { mutableStateOf<String?>(null) }
    val open = list.firstOrNull { it.id == openId }

    BackHandler(enabled = open != null) { openId = null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .background(Brush.verticalGradient(0f to p.bgGlow.copy(alpha = 0.45f), 0.35f to p.bgGlowSecondary.copy(alpha = 0.20f), 0.75f to Color.Transparent))
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(onClick = { if (open != null) openId = null else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (ar) "رجوع" else "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Column {
                    Text(if (ar) "التوعية الصحية" else "Education", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        if (ar) "مقالات توعوية عامة. مش بديل عن الدكتور." else "General educational articles. Not a substitute for a doctor.",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            if (open != null) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    LungCareTag(text = open.category)
                    Spacer(Modifier.height(8.dp))
                    Text(open.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(10.dp))
                    Text(open.body, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (ar) "للتوعية فقط، مش تشخيص ولا نصيحة علاجية." else "For education only, not a diagnosis or treatment advice.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(list) { article ->
                        LungCareCard(
                            modifier = Modifier.clickable { openId = article.id },
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            cornerRadius = 14.dp
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = MaterialTheme.colorScheme.primary) }
                                Column(Modifier.weight(1f)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        LungCareTag(text = article.category)
                                        Text(article.readTime, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(article.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(4.dp))
                                    Text(article.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
