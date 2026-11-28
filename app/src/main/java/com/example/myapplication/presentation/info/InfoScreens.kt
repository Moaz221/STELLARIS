package com.example.myapplication.presentation.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.design.lungCarePalette
import com.example.myapplication.core.ui.loc
import com.example.myapplication.presentation.main.OverlayScreen

const val DOCS_URL = "https://moaz221.github.io/stellaris-docs/"

private data class Block(val title: String, val lines: List<String>)

@Composable
private fun InfoPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val p = lungCarePalette()
    Column(Modifier.fillMaxSize().background(p.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = loc("Back", "رجوع"), tint = p.textPrimary) }
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
        }
        HorizontalDivider(color = p.border)
        Column(Modifier.weight(1f).fillMaxWidth(), content = content)
    }
}

@Composable
private fun Card(block: Block) {
    val p = lungCarePalette()
    Surface(shape = RoundedCornerShape(18.dp), color = p.surface, border = BorderStroke(1.dp, p.border), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(block.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.primary)
            block.lines.forEach { Text(it, fontSize = 14.sp, lineHeight = 21.sp, color = p.textPrimary) }
        }
    }
}

private fun openDocs(ctx: android.content.Context) {
    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DOCS_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val p = lungCarePalette()
    val blocks = listOf(
        Block(loc("Who this is for", "الصفحة دي لمين"), listOf(loc(
            "STELLARIS is a lung-health companion. It gives education and helps you keep your own health notes. It does not diagnose, treat or replace your doctor.",
            "ستيلاريس تطبيق مساعد لصحة الرئة. بيقدم معلومات تعليمية وبيساعدك تحتفظ بملاحظاتك الصحية. مش بيشخّص ولا بيعالج ومش بديل عن دكتورك."))),
        Block(loc("What we collect", "إيه اللي بنجمعه"), listOf(
            loc("Account: your name, email and, if you sign in with Google, your profile photo.", "الحساب: اسمك وإيميلك، ولو دخلت بجوجل بتتجاب صورة حسابك."),
            loc("Health notes you type yourself: symptoms, medical records, doctor questions, the risk check result, appointments, medications, side effects, diagnosis notes and emergency phone numbers.", "الملاحظات الصحية اللي بتكتبها بنفسك: الأعراض، السجلات الطبية، أسئلة الدكتور، نتيجة تقييم المخاطر، المواعيد، الأدوية، الأعراض الجانبية، ملاحظات التشخيص وأرقام الطوارئ."),
            loc("Your assistant conversations and your settings (language, theme, chosen journey).", "محادثاتك مع المساعد وإعداداتك (اللغة، المظهر، الرحلة اللي اخترتها)."),
            loc("We do not collect your location, contacts, microphone or camera, and the app has no advertising or analytics tools.", "مش بنجمع موقعك ولا جهات اتصالك ولا الميكروفون ولا الكاميرا، والتطبيق مفيهوش إعلانات ولا أدوات تحليل."))),
        Block(loc("Where it is stored", "بيتخزن فين"), listOf(
            loc("Your health notes, assistant history and settings are saved on your phone only. They are excluded from automatic cloud backup.", "ملاحظاتك الصحية وسجل المساعد وإعداداتك بتتحفظ على موبايلك بس، ومستثناة من النسخ الاحتياطي التلقائي."),
            loc("Your account details are kept by our sign-in provider (Google Firebase) so you can log in.", "بيانات حسابك بيحتفظ بيها مزوّد تسجيل الدخول (Google Firebase) عشان تقدر تدخل."),
            loc("If you uninstall the app or clear its data, the notes on the phone are deleted.", "لو مسحت التطبيق أو بياناته، الملاحظات على الموبايل بتتمسح."))),
        Block(loc("What leaves your phone", "إيه اللي بيخرج من موبايلك"), listOf(
            loc("Assistant: the text of the question you send is sent to our AI service to get an answer. Nothing is sent until you press send.", "المساعد: نص السؤال اللي بتبعته بيتبعت لخدمة الذكاء الاصطناعي عشان تجيب الرد. مفيش حاجة بتتبعت إلا لما تضغط إرسال."),
            loc("Share with assistant: the button only puts a summary of your notes into the message box so you can read and edit it. It is sent only if you press send. You can delete it before.", "زرار المشاركة مع المساعد: بيحط ملخص ملاحظاتك في خانة الرسالة بس عشان تقراه وتعدله. بيتبعت لو ضغطت إرسال بس، وتقدر تمسحه قبلها."),
            loc("Vision Lab: when you press Analyze, the image you chose is sent over the internet to the image-analysis server to get a result. You are asked to agree once before the first upload. Only the image is sent, not your name or account. We cannot describe how that server handles images, so send only images you are comfortable sharing. The result is a visual aid, not a diagnosis. The app does not save the image or the result after you leave the screen.", "معمل الرؤية: لما تضغط تحليل، الصورة اللي اخترتها بتتبعت عبر الإنترنت لسيرفر تحليل الصور عشان تطلع نتيجة. بنسألك توافق مرة واحدة قبل أول رفع. بتتبعت الصورة بس، من غير اسمك أو حسابك. مش قادرين نوضح السيرفر بيتعامل مع الصور إزاي، فابعت بس الصور اللي مرتاح تشاركها. النتيجة مساعدة بصرية مش تشخيص. التطبيق مبيحفظش الصورة ولا النتيجة بعد ما تخرج من الشاشة."),
            loc("Sign-in: handled by Google Firebase.", "تسجيل الدخول: بيتم عن طريق Google Firebase."),
            loc("We do not sell your data and do not share it with advertisers. Please do not type your full name, ID number or address into assistant questions.", "مبنبيعش بياناتك ومبنشاركهاش مع معلنين. من فضلك متكتبش اسمك الكامل أو رقم البطاقة أو العنوان في أسئلة المساعد."))),
        Block(loc("Notifications", "الإشعارات"), listOf(loc(
            "Medication reminders are set on your phone and need the notification permission. They never leave the device.",
            "تنبيهات الأدوية بتتظبط على موبايلك وبتحتاج إذن الإشعارات. وبتفضل على الجهاز."))),
        Block(loc("Your choices", "اختياراتك"), listOf(
            loc("Edit or delete any saved item inside the app.", "تقدر تعدل أو تمسح أي عنصر محفوظ جوه التطبيق."),
            loc("Delete everything saved on the phone by clearing the app data in Android settings, or by uninstalling.", "تمسح كل المحفوظ على الموبايل من إعدادات أندرويد (مسح بيانات التطبيق) أو بإلغاء التثبيت."),
            loc("Turn reminders off in Android notification settings.", "تقفل التنبيهات من إعدادات إشعارات أندرويد."),
            loc("To ask about deleting your account, contact the app owner.", "لو عايز تسأل عن حذف حسابك، تواصل مع صاحب التطبيق."))),
        Block(loc("Medical notice", "تنبيه طبي"), listOf(loc(
            "Information in the app is educational. In an emergency call your local emergency number (Egypt ambulance: 123) or go to the nearest hospital.",
            "المعلومات في التطبيق تعليمية. في الطوارئ اتصل برقم الطوارئ (إسعاف مصر: 123) أو روح لأقرب مستشفى."))),
        Block(loc("Changes", "التغييرات"), listOf(loc(
            "If we change what the app does with data, this page is updated in the next version.",
            "لو غيّرنا طريقة تعامل التطبيق مع البيانات، الصفحة دي بتتحدث في الإصدار الجاي.")))
    )
    InfoPage(loc("Privacy Policy", "سياسة الخصوصية"), onBack) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(blocks) { Card(it) }
            item {
                Button(onClick = { openDocs(ctx) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.primary, contentColor = p.onPrimary)) {
                    Text(loc("More details about the app", "تفاصيل أكتر عن التطبيق"), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private data class Step(val title: String, val body: String, val open: OverlayScreen?, val openLabel: String = "")

@Composable
fun HowToUseScreen(onBack: () -> Unit, onOpen: (OverlayScreen) -> Unit) {
    val ctx = LocalContext.current
    val p = lungCarePalette()
    val open = loc("Open", "افتح")
    val steps = listOf(
        Step(loc("1. Choose your journey", "1. اختار رحلتك"), loc("On Home tap the journey box and pick: early detection, under evaluation or diagnosed. The hero card, My Health and the app icon follow your choice.", "من الرئيسية اضغط على بوكس الرحلة واختار: فحص مبكر أو تحت التقييم أو مشخّص. الكارت الرئيسي وصفحة صحتي وأيقونة التطبيق بيمشوا مع اختيارك."), null),
        Step(loc("2. Check your risk", "2. قيّم خطر الإصابة"), loc("Answer four short steps. You get an educational result and it is saved in My Health.", "جاوب على 4 خطوات قصيرة. بتاخد نتيجة تعليمية وبتتحفظ في صحتي."), OverlayScreen.RISK_ASSESSMENT, open),
        Step(loc("3. Log symptoms", "3. سجّل الأعراض"), loc("Tap + to add a symptom with severity and frequency. Red-flag symptoms show an alert and the emergency button opens the phone dialer.", "اضغط + لإضافة عرض بالشدة والتكرار. الأعراض الخطيرة بتظهر تنبيه وزرار الطوارئ بيفتح تطبيق الاتصال."), OverlayScreen.SYMPTOMS_TRACKER, open),
        Step(loc("4. Keep medical records", "4. احفظ سجلاتك الطبية"), loc("Add lab results, imaging and visit notes. Filter by type and see the status of each.", "ضيف تحاليل وأشعة وزيارات. فلتر بالنوع وشوف حالة كل واحد."), OverlayScreen.RECORDS, open),
        Step(loc("5. Prepare doctor questions", "5. جهّز أسئلة الدكتور"), loc("Write questions, tick them when asked (they move to History with the date). Delete has Undo.", "اكتب أسئلتك، علّم عليها لما تسألها (بتروح للسجل بالتاريخ). الحذف فيه تراجع."), OverlayScreen.DOCTOR_QUESTIONS, open),
        Step(loc("6. Learn", "6. اتعلم"), loc("Read short articles about lung health, screening and treatment.", "اقرا مقالات قصيرة عن صحة الرئة والفحص والعلاج."), OverlayScreen.EDUCATION_HUB, open),
        Step(loc("7. Vision Lab", "7. معمل الرؤية"), loc("Pick a chest X-ray image to get an AI quality and analysis result. It is only a help, not a diagnosis.", "اختار صورة أشعة على الصدر عشان تاخد نتيجة تحليل بالذكاء الاصطناعي. ده مساعد بس مش تشخيص."), OverlayScreen.VISION_LAB, open),
        Step(loc("8. Ask the assistant", "8. اسأل المساعد"), loc("Type a question or tap the robot button on any screen to preview a summary of that screen's notes, then press send yourself.", "اكتب سؤال أو اضغط زرار الروبوت في أي شاشة عشان تشوف ملخص ملاحظات الشاشة دي، وبعدين اضغط إرسال بنفسك."), OverlayScreen.ASSISTANT, open),
        Step(loc("9. Care journey and reminders", "9. رحلتي العلاجية والتنبيهات"), loc("In the Care tab add appointments, medicines with reminder times and side effects. Tap an item to edit it. The bell on Home lists what is coming.", "في تاب رحلتي العلاجية ضيف مواعيد وأدوية بمواعيد تنبيه وأعراض جانبية. اضغط على أي عنصر لتعديله. الجرس في الرئيسية بيعرض اللي جاي."), null),
        Step(loc("10. Language, theme, profile", "10. اللغة والمظهر والحساب"), loc("Use the globe and moon buttons on Home, or the Profile tab to edit your name and open settings.", "استخدم زراير الكورة الأرضية والقمر في الرئيسية، أو تاب حسابي لتعديل اسمك وفتح الإعدادات."), null)
    )
    InfoPage(loc("How to use the app", "إزاي تستخدم التطبيق"), onBack) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text(loc("Tap any step to learn it, or press Open to go straight to that screen.", "اقرا أي خطوة، أو اضغط افتح عشان تروح للشاشة على طول."), fontSize = 14.sp, color = p.textSecondary)
            }
            items(steps) { s ->
                Surface(shape = RoundedCornerShape(18.dp), color = p.surface, border = BorderStroke(1.dp, p.border), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.primary)
                        Text(s.body, fontSize = 14.sp, lineHeight = 21.sp, color = p.textPrimary)
                        if (s.open != null) {
                            OutlinedButton(onClick = { onOpen(s.open) }, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.5.dp, p.primary)) {
                                Text(s.openLabel, color = p.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item {
                Button(onClick = { openDocs(ctx) }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = p.primary, contentColor = p.onPrimary)) {
                    Text(loc("More details about the app", "تفاصيل أكتر عن التطبيق"), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
