package com.example.myapplication.presentation.vision

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.myapplication.core.ui.loc
import com.example.myapplication.R
import com.example.myapplication.core.design.LungCarePalette
import com.example.myapplication.core.design.lungCarePalette

@Composable
fun VisionLabScreen(
    onBack: () -> Unit = {},
    viewModel: VisionViewModel = viewModel(),
) {
    val p = lungCarePalette()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val sp = remember { ctx.getSharedPreferences("stellaris_prefs", android.content.Context.MODE_PRIVATE) }
    var consented by remember { mutableStateOf(sp.getBoolean("vision_consent", false)) }
    var showConsent by remember { mutableStateOf(false) }
    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text(loc("Send this image for analysis?", "إرسال الصورة للتحليل؟")) },
            text = {
                Text(loc(
                    "When you press Analyze, the image you chose is sent over the internet to the image-analysis server used by this app, to produce the result. Only the image is sent, not your name or account. Do not use images that show your name or ID. We cannot describe how the server handles images, so send only what you are comfortable sharing. The result is a visual aid, not a medical diagnosis. You will be asked this only once.",
                    "لما تضغط تحليل، الصورة اللي اخترتها بتتبعت عبر الإنترنت لسيرفر تحليل الصور اللي التطبيق بيستخدمه عشان تطلع النتيجة. بتتبعت الصورة بس، من غير اسمك أو حسابك. متستخدمش صور ظاهر فيها اسمك أو رقم بطاقتك. مش قادرين نوضح بيتعامل مع الصور إزاي على السيرفر، فابعت بس اللي مرتاح إنك تشاركه. النتيجة مساعدة بصرية مش تشخيص طبي. هنسألك السؤال ده مرة واحدة بس."
                ), fontSize = 13.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    sp.edit().putBoolean("vision_consent", true).apply(); consented = true; showConsent = false; viewModel.analyze()
                }) { Text(loc("I agree, analyze", "أوافق، حلّل")) }
            },
            dismissButton = { TextButton(onClick = { showConsent = false }) { Text(loc("Cancel", "إلغاء")) } }
        )
    }
    var cameraUri by remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { viewModel.onImageSelected(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(p.background)
            .background(
                Brush.verticalGradient(
                    listOf(p.primary.copy(alpha = 0.08f), Color.Transparent)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = p.textPrimary
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.vision_lab_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = p.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.vision_lab_subtitle),
                        fontSize = 12.sp,
                        color = p.textSecondary
                    )
                }
            }

            // Safety banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = p.info.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, p.info.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = p.info,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.vision_disclaimer),
                        fontSize = 11.sp,
                        color = p.textPrimary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Image preview / capture area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(p.surface)
                    .border(1.5.dp, p.border, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (state.state) {
                    VisionUiState.IDLE -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraEnhance,
                                contentDescription = null,
                                tint = p.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.capture_or_select),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = p.textPrimary
                            )
                            Text(
                                text = stringResource(R.string.tap_to_analyze),
                                fontSize = 12.sp,
                                color = p.textSecondary
                            )
                        }
                    }

                    VisionUiState.PREVIEW -> {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    VisionUiState.ANALYZING -> {
                        ScanningView(state.selectedImageUri, state.step, p) { viewModel.cancel() }
                    }

                    VisionUiState.RESULT -> {
                        AsyncImage(
                            model = state.selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    VisionUiState.ERROR -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = p.urgent, modifier = Modifier.size(48.dp))
                            Text(loc("Could not analyze the image", "تعذر تحليل الصورة"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                            Text(
                                text = when (state.errorKind) {
                                    "TIMEOUT" -> loc("The server took too long to answer. It may be starting up. Try again in a minute.", "السيرفر اتأخر في الرد. ممكن يكون بيشتغل دلوقتي. جرّب تاني بعد دقيقة.")
                                    "NETWORK" -> loc("No connection to the server. Check your internet and try again.", "مفيش اتصال بالسيرفر. اتأكد من الإنترنت وجرّب تاني.")
                                    "BAD_IMAGE" -> loc("The server could not read this image. Choose a clear X-ray image (JPG or PNG).", "السيرفر مقدرش يقرا الصورة دي. اختار صورة أشعة واضحة (JPG أو PNG).")
                                    "SERVER" -> loc("The server had a problem. Please try again later.", "السيرفر فيه مشكلة. جرّب تاني بعد شوية.")
                                    else -> stringResource(R.string.error_generic)
                                },
                                fontSize = 13.sp, color = p.urgent, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Text(loc("Your image is still selected.", "صورتك لسه مختارة."), fontSize = 12.sp, color = p.textSecondary)
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.state != VisionUiState.ANALYZING
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.pick_photo), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val dir = java.io.File(ctx.cacheDir, "camera").apply { mkdirs() }
                        val f = java.io.File(dir, "xray_${System.currentTimeMillis()}.jpg")
                        val uri = androidx.core.content.FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = state.state != VisionUiState.ANALYZING
                ) {
                    Icon(Icons.Default.CameraEnhance, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.take_photo), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { if (consented) viewModel.analyze() else showConsent = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = (state.state == VisionUiState.PREVIEW) || (state.state == VisionUiState.ERROR),
                    colors = ButtonDefaults.buttonColors(containerColor = p.primary)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.analyze), fontWeight = FontWeight.Bold)
                }
            }

            // Results section
            AnimatedVisibility(
                visible = state.state == VisionUiState.RESULT,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val abnormal = (state.prediction ?: "").uppercase().contains("ABNORMAL")
                    val conf = state.confidence ?: 0.0
                    // raw_score is the probability of "abnormal" (verified: NORMAL results show confidence = 1 - raw_score)
                    val abProb = (state.rawScore ?: if (abnormal) conf else 1.0 - conf).coerceIn(0.0, 1.0)
                    val uncertain = !abnormal && abProb >= 0.35
                    val tint = when { abnormal -> p.urgent; uncertain -> p.warning; else -> p.success }
                    val headline = when {
                        abnormal -> loc("Possible abnormality detected. Please see a doctor.", "تم رصد علامات محتملة غير طبيعية. من فضلك راجع دكتور.")
                        uncertain -> loc("No clear signs found, but the result is not certain.", "مفيش علامات واضحة، لكن النتيجة مش مؤكدة.")
                        else -> loc("No signs that need a doctor's review were detected.", "لم يتم رصد علامات تحتاج مراجعة دكتور.")
                    }
                    val explain = when {
                        abnormal -> loc("The AI model found patterns in this X-ray that look different from a typical normal chest X-ray. This is a screening result, not a diagnosis. It cannot tell which condition it is, and it can be wrong.",
                            "النموذج لقى أنماط في الأشعة دي شكلها مختلف عن أشعة الصدر الطبيعية. ده نتيجة فحص أولي مش تشخيص. النموذج مش بيقدر يحدد إيه الحالة، وممكن يغلط.")
                        uncertain -> loc("The model leaned towards normal, but only slightly. Treat this as inconclusive. A doctor should look at the original X-ray, especially if you have symptoms.",
                            "النموذج مال للطبيعي لكن بفارق بسيط. اعتبر النتيجة غير حاسمة. الدكتور لازم يشوف الأشعة الأصلية، خصوصاً لو عندك أعراض.")
                        else -> loc("The AI model did not find patterns that need review. This does not prove that your lungs are healthy. It is a screening aid, and it can miss things.",
                            "النموذج مالقاش أنماط محتاجة مراجعة. ده مش معناه إن رئتك سليمة تماماً. ده مساعد فحص أولي وممكن يفوّت حاجات.")
                    }
                    val nextStep = when {
                        abnormal -> loc("Next step: show this X-ray to a doctor (a chest specialist if possible) in the next few days. Go to urgent care now if you have severe shortness of breath, chest pain or you cough up blood.",
                            "الخطوة الجاية: اعرض الأشعة دي على دكتور (يفضل صدرية) في الأيام الجاية. روح الطوارئ فوراً لو عندك ضيق نفس شديد أو ألم في الصدر أو كحة بدم.")
                        uncertain -> loc("Next step: share the X-ray with a doctor, and see one soon if you have a cough that lasts, fever, weight loss or shortness of breath.",
                            "الخطوة الجاية: اعرض الأشعة على دكتور، وروح قريب لو عندك كحة مستمرة أو حرارة أو نزول وزن أو ضيق نفس.")
                        else -> loc("Next step: no action needed from this result alone. If you have a cough that lasts, fever, chest pain or shortness of breath, see a doctor anyway.",
                            "الخطوة الجاية: مفيش إجراء مطلوب من النتيجة دي لوحدها. ولو عندك كحة مستمرة أو حرارة أو ألم صدر أو ضيق نفس، روح لدكتور برضه.")
                    }
                    Surface(shape = RoundedCornerShape(16.dp), color = tint.copy(alpha = 0.10f), border = BorderStroke(1.5.dp, tint.copy(alpha = 0.8f))) {
                        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(if (abnormal || uncertain) Icons.Default.Warning else Icons.Default.CheckCircle, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
                                Text(headline, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = p.textPrimary, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                            }
                            Text(explain, fontSize = 13.sp, color = p.textPrimary, lineHeight = 19.sp)
                            Text(loc("Chance of abnormal signs (model estimate)", "احتمال وجود علامات غير طبيعية (تقدير النموذج)") + ": ${(abProb * 100).toInt()}%", fontSize = 12.sp, color = p.textSecondary)
                            LinearProgressIndicator(progress = { abProb.toFloat() }, color = tint, trackColor = p.border, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
                            Text(loc("The model decides between normal and abnormal only. It does not name a disease.", "النموذج بيفرّق بين طبيعي وغير طبيعي بس. مش بيحدد اسم مرض."), fontSize = 11.sp, color = p.textSecondary)
                        }
                    }
                    Surface(shape = RoundedCornerShape(14.dp), color = p.surface, border = BorderStroke(1.dp, p.border)) {
                        Text(nextStep, fontSize = 13.sp, color = p.textPrimary, lineHeight = 19.sp, modifier = Modifier.padding(14.dp).fillMaxWidth())
                    }

                    var showDetails by remember { mutableStateOf(false) }
                    OutlinedButton(onClick = { showDetails = !showDetails }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (showDetails) loc("Hide details", "إخفاء التفاصيل") else loc("Show details (highlighted areas)", "عرض التفاصيل (المناطق المميزة)"))
                    }
                    if (showDetails) {
                        state.gradcamBase64?.let { b64 ->
                            ResultImageCard(
                                title = loc("Attention map", "خريطة الانتباه"),
                                subtitle = loc("Warm colors (red, yellow) show where the model looked most when deciding. It shows attention, not a confirmed problem.", "الألوان الدافية (الأحمر والأصفر) بتوضح المكان اللي النموذج ركز عليه أكتر. دي خريطة انتباه مش مشكلة مؤكدة."),
                                base64 = b64, p = p
                            )
                        }
                        state.segmentationOverlayBase64?.let { b64 ->
                            ResultImageCard(
                                title = loc("Lung outline", "حدود الرئتين"),
                                subtitle = loc("The green line shows the lung area the model detected on your image. It is only an outline.", "الخط الأخضر بيوضح منطقة الرئتين اللي النموذج حددها في صورتك. ده مجرد حدود."),
                                base64 = b64, p = p
                            )
                        }
                    }

                    // Medical safety warning
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = p.warning.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, p.warning.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = p.warning, modifier = Modifier.size(18.dp))
                            Text(
                                text = stringResource(R.string.ai_not_diagnosis),
                                fontSize = 11.sp,
                                color = p.textPrimary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Error state action
            if (state.state == VisionUiState.ERROR) {
                Button(
                    onClick = { viewModel.analyze() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = p.urgent)
                ) {
                    Text(stringResource(R.string.try_again), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ResultImageCard(
    title: String,
    subtitle: String,
    base64: String,
    p: LungCarePalette
) {
    val bitmap = remember(base64) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = p.surface,
        border = BorderStroke(1.dp, p.border)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
            Text(subtitle, fontSize = 11.sp, color = p.textSecondary, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(10.dp))
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } ?: Text(stringResource(R.string.image_decode_error), color = p.urgent, fontSize = 12.sp)
        }
    }
}


@Composable
private fun ScanningView(uri: android.net.Uri?, step: Int, p: LungCarePalette, onCancel: () -> Unit) {
    val t = rememberInfiniteTransition(label = "scan")
    val y by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "y")
    val pulse by t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse")
    val neon = p.primary
    val steps = listOf(
        loc("Uploading the image...", "جاري رفع الصورة..."),
        loc("Analyzing...", "جاري التحليل..."),
        loc("Preparing the result...", "جاري تجهيز النتيجة..."),
        loc("Still working. The server may be starting up, please wait...", "لسه شغالين. ممكن السيرفر بيشتغل دلوقتي، استنى شوية...")
    )
    Box(Modifier.fillMaxSize()) {
        AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color(0xB0030A12)))
        Canvas(Modifier.fillMaxSize()) {
            val ly = size.height * y
            val band = 90.dp.toPx()
            drawRect(
                brush = Brush.verticalGradient(listOf(Color.Transparent, neon.copy(alpha = 0.35f)), startY = ly - band, endY = ly),
                topLeft = Offset(0f, ly - band), size = Size(size.width, band)
            )
            drawLine(neon, Offset(0f, ly), Offset(size.width, ly), strokeWidth = 3.dp.toPx())
            val m = 6.dp.toPx()
            drawRoundRect(
                color = neon.copy(alpha = pulse), topLeft = Offset(m, m),
                size = Size(size.width - 2 * m, size.height - 2 * m),
                cornerRadius = CornerRadius(20.dp.toPx()), style = Stroke(2.dp.toPx())
            )
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(steps[step.coerceIn(0, 3)], fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                (0..3).forEach { i ->
                    Box(Modifier.size(if (i == step) 10.dp else 7.dp).clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (i <= step) neon else Color.White.copy(alpha = 0.3f)))
                }
            }
            OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, neon),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 2.dp)) {
                Text(loc("Cancel", "إلغاء"), color = neon, fontSize = 13.sp)
            }
        }
    }
}
