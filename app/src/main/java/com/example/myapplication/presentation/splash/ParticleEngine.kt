package com.example.myapplication.presentation.splash

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.theme.Nebula
import com.example.myapplication.presentation.theme.StellarCyan
import com.example.myapplication.presentation.theme.StellarTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

internal const val VIRTUAL = 1000f          // مساحة افتراضية بنرسم عليها
internal const val PARTICLE_COUNT = 1500    // قللها لـ 900 لو الجهاز ضعيف

// الـ Timeline (نسب من إجمالي مدة الأنيميشن)
internal const val T_LUNG_START = 0.04f
internal const val T_LUNG_END   = 0.32f
internal const val T_HOLD_END   = 0.50f
internal const val T_MORPH_END  = 0.80f

internal class Particle(
    val start: Offset,     // نقطة البداية (دوامة بره الشاشة)
    val lung: Offset,      // هدف الرئة
    val text: Offset,      // هدف الكلمة
    val delay: Float,      // تأخير عشوائي (إحساس عضوي)
    val size: Float,
    val twinkle: Float,
    val colorMix: Float,   // لون وهي رئة (حسب الارتفاع)
    val textMix: Float,    // لون وهي كلمة (حسب الموضع الأفقي)
    val arcAmp: Float,     // قوة القوس أثناء التحول
    val arcCos: Float,     // اتجاه القوس (عمودي على خط الحركة = دوران متناسق)
    val arcSin: Float
)

// تدرّج Teal → Cyan → أزرق ناعم
internal fun particleColor(mix: Float): Color = when {
    mix < 0.65f -> lerpColor(StellarTeal, StellarCyan, mix / 0.65f)
    else -> lerpColor(StellarCyan, Nebula, (mix - 0.65f) / 0.35f)
}

internal fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t
)

internal suspend fun buildParticles(): List<Particle> = withContext(Dispatchers.Default) {
    // نرتّب الاتنين حسب X عشان كل جزء من الرئة يروح للجزء اللي قصاده من الكلمة
    val lungPts = sortByXJitter(pickLungPoints(sampleLung(), PARTICLE_COUNT))
    val textPts = sortByXJitter(pickN(sampleText("STELLARIS"), PARTICLE_COUNT))
    val c = VIRTUAL / 2f

    List(PARTICLE_COUNT) { i ->
        val l = lungPts[i]
        val t = textPts[i]

        // بداية على شكل دوامة: كل جسيمة بتدخل ناحية هدفها وهي بتلف
        val radius = 460f + Random.nextFloat() * 560f
        val ang = atan2(l.y - c, l.x - c) + radius / 650f

        val dx = t.x - l.x
        val dy = t.y - l.y
        val len = hypot(dx, dy).coerceAtLeast(1f)

        val r = Random.nextFloat()
        Particle(
            start = Offset(c + cos(ang) * radius, c + sin(ang) * radius),
            lung = l,
            text = t,
            delay = Random.nextFloat() * 0.3f,
            size = 1.3f + r * r * r * 3.4f,            // أغلبها صغير وقليل منها كبير ولامع
            twinkle = Random.nextFloat(),
            colorMix = ((l.y - 120f) / 700f + (Random.nextFloat() - 0.5f) * 0.12f).coerceIn(0f, 1f),
            textMix = ((t.x - 70f) / 860f).coerceIn(0f, 1f),
            arcAmp = 40f + Random.nextFloat() * 110f,
            arcCos = -dy / len,
            arcSin = dx / len
        )
    }
}

// ================= أدوات اختيار النقاط =================

private fun pickN(src: List<Offset>, n: Int): List<Offset> {
    if (src.isEmpty() || n <= 0) return emptyList()
    val sh = src.shuffled()
    val chosen = if (sh.size >= n) sh.subList(0, n) else List(n) { sh[it % sh.size] }
    // jitter صغير عشان مايبانش إن النقاط على شبكة
    return chosen.map {
        Offset(it.x + (Random.nextFloat() - 0.5f) * 2.5f, it.y + (Random.nextFloat() - 0.5f) * 2.5f)
    }
}

private fun sortByXJitter(pts: List<Offset>): List<Offset> =
    pts.map { it to it.x + Random.nextFloat() * 50f }
        .sortedBy { it.second }
        .map { it.first }

// ================= الرئة =================

private class LungPoints(
    val edge: List<Offset>,   // الحواف (أكتر كثافة = شكل واضح)
    val tree: List<Offset>,   // القصبة والشعب
    val fill: List<Offset>    // الداخل
)

private fun pickLungPoints(l: LungPoints, n: Int): List<Offset> {
    val nEdge = (n * 0.45f).toInt()
    val nTree = (n * 0.25f).toInt()
    val nFill = n - nEdge - nTree
    return pickN(l.edge, nEdge) + pickN(l.tree, nTree) + pickN(l.fill, nFill)
}

private fun rasterize(w: Int, h: Int, draw: (Canvas) -> Unit): BooleanArray {
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    draw(Canvas(bmp))
    val px = IntArray(w * h)
    bmp.getPixels(px, 0, w, 0, 0, w, h)
    bmp.recycle()
    return BooleanArray(w * h) { android.graphics.Color.alpha(px[it]) > 110 }
}

private fun sampleLung(): LungPoints {
    val n = VIRTUAL.toInt()

    val fillPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    val fillMask = rasterize(n, n) { it.drawPath(lungPath(), fillPaint) }
    val treeMask = rasterize(n, n) { drawBronchi(it) }

    val edge = ArrayList<Offset>(4096)
    val fill = ArrayList<Offset>(16384)
    val tree = ArrayList<Offset>(4096)

    val band = 9          // سُمك شريط الحافة
    val step = 3

    var y = band
    while (y < n - band) {
        var x = band
        while (x < n - band) {
            val i = y * n + x
            if (fillMask[i]) {
                val isEdge = !fillMask[i - band] || !fillMask[i + band] ||
                        !fillMask[i - band * n] || !fillMask[i + band * n]
                if (isEdge) edge.add(Offset(x.toFloat(), y.toFloat()))
                else fill.add(Offset(x.toFloat(), y.toFloat()))
            }
            x += step
        }
        y += step
    }

    y = 0
    while (y < n) {
        var x = 0
        while (x < n) {
            if (treeMask[y * n + x]) tree.add(Offset(x.toFloat(), y.toFloat()))
            x += 2
        }
        y += 2
    }
    return LungPoints(edge, tree, fill)
}

// رئتين بشكل تشريحي: اليمين (على يسار الشاشة) أكبر، والشمال فيها تجويف للقلب
private fun lungPath(): Path {
    val p = Path()

    // رئة المريض اليمنى (يسار الشاشة)
    p.moveTo(455f, 250f)
    p.cubicTo(400f, 205f, 330f, 235f, 290f, 330f)
    p.cubicTo(245f, 440f, 215f, 600f, 235f, 720f)
    p.cubicTo(250f, 790f, 320f, 822f, 410f, 815f)
    p.cubicTo(470f, 810f, 490f, 770f, 485f, 700f)
    p.cubicTo(480f, 600f, 470f, 420f, 455f, 250f)
    p.close()

    // رئة المريض اليسرى (يمين الشاشة) - أصغر وفيها تجويف القلب
    p.moveTo(545f, 250f)
    p.cubicTo(600f, 205f, 670f, 235f, 710f, 330f)
    p.cubicTo(755f, 440f, 785f, 600f, 765f, 720f)
    p.cubicTo(750f, 790f, 680f, 822f, 600f, 815f)
    p.cubicTo(545f, 810f, 525f, 775f, 528f, 715f)
    p.cubicTo(528f, 650f, 600f, 610f, 598f, 530f)
    p.cubicTo(596f, 440f, 552f, 380f, 545f, 250f)
    p.close()

    return p
}

// القصبة الهوائية + شجرة الشعب
private fun drawBronchi(c: Canvas) {
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }
    fun seg(width: Float, build: Path.() -> Unit) {
        paint.strokeWidth = width
        c.drawPath(Path().apply(build), paint)
    }

    // القصبة
    seg(26f) { moveTo(500f, 110f); lineTo(500f, 300f) }

    // الشعبة اليمنى (يسار الشاشة) وفروعها
    seg(14f) { moveTo(500f, 292f); quadTo(470f, 330f, 405f, 390f) }
    seg(9f)  { moveTo(405f, 390f); quadTo(360f, 420f, 315f, 470f) }
    seg(9f)  { moveTo(405f, 390f); quadTo(400f, 480f, 375f, 570f) }
    seg(6f)  { moveTo(375f, 570f); quadTo(350f, 640f, 330f, 715f) }
    seg(6f)  { moveTo(375f, 570f); quadTo(415f, 640f, 425f, 730f) }
    seg(5f)  { moveTo(315f, 470f); quadTo(290f, 520f, 280f, 590f) }

    // الشعبة اليسرى (يمين الشاشة) وفروعها
    seg(14f) { moveTo(500f, 292f); quadTo(540f, 330f, 600f, 392f) }
    seg(9f)  { moveTo(600f, 392f); quadTo(650f, 420f, 700f, 470f) }
    seg(9f)  { moveTo(600f, 392f); quadTo(610f, 480f, 640f, 570f) }
    seg(6f)  { moveTo(640f, 570f); quadTo(675f, 640f, 690f, 715f) }
    seg(5f)  { moveTo(700f, 470f); quadTo(725f, 520f, 735f, 590f) }
}

// ================= الكلمة =================

private fun sampleText(text: String): List<Offset> {
    val w = 2000
    val h = 420
    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        textSize = 240f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.16f
    }
    val baseY = h / 2f - (paint.descent() + paint.ascent()) / 2f
    val mask = rasterize(w, h) { it.drawText(text, w / 2f, baseY, paint) }

    val raw = ArrayList<Offset>(32768)
    var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
    var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
    var y = 0
    while (y < h) {
        var x = 0
        while (x < w) {
            if (mask[y * w + x]) {
                raw.add(Offset(x.toFloat(), y.toFloat()))
                if (x < minX) minX = x.toFloat()
                if (x > maxX) maxX = x.toFloat()
                if (y < minY) minY = y.toFloat()
                if (y > maxY) maxY = y.toFloat()
            }
            x += 2
        }
        y += 2
    }
    if (raw.isEmpty()) return emptyList()

    // نحجّم الكلمة على حسب حدودها الفعلية: عرض 860 وتتوسّط
    val scale = 860f / (maxX - minX)
    val cx = (minX + maxX) / 2f
    val cy = (minY + maxY) / 2f
    val c = VIRTUAL / 2f
    return raw.map { Offset(c + (it.x - cx) * scale, c + (it.y - cy) * scale) }
}