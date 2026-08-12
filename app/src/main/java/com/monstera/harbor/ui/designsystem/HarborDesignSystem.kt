package com.monstera.harbor.ui.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Explicit Direction 2 visual tokens. Keep these independent from Material defaults. */
object HarborColors {
    val bgDeep = Color(0xFF060C10)
    val bgPersonal = Color(0xFF081319)
    val surfaceLow = Color(0xFF0E161B)
    val surface = Color(0xFF121F27)
    val surfaceRaised = Color(0xFF151E25)
    val sheet = Color(0xFF19232A)
    val stroke = Color(0xFF2A373E)
    val heroStart = Color(0xFF003A40)
    val heroEnd = Color(0xFF00282F)
    val accent = Color(0xFF12C8C7)
    val accentDark = Color(0xFF052B30)
    val textPrimary = Color(0xFFF0F2F2)
    val textSecondary = Color(0xFFA5B6B8)
    val textMuted = Color(0xFF708589)
    val positiveBg = Color(0xFF17302F)
    val positive = Color(0xFF58D6B7)
    val frozenBg = Color(0xFF1F3448)
    val frozen = Color(0xFF7CC8E8)
    val danger = Color(0xFFD85858)
    val warning = Color(0xFFE6B86A)
}

object HarborSpacing {
    val screen = 20.dp
    val section = 18.dp
    val card = 16.dp
    val compact = 10.dp
}

object HarborShapes {
    val hero = RoundedCornerShape(26.dp)
    val card = RoundedCornerShape(20.dp)
    val row = RoundedCornerShape(18.dp)
    val pill = RoundedCornerShape(50)
    val search = RoundedCornerShape(18.dp)
    val tile = RoundedCornerShape(18.dp)
}

val HarborAppIconSize = 48.dp

enum class StatusTone {
    Positive,
    Neutral,
    Warning,
    Critical,
}

enum class HarborIconKind {
    Lighthouse,
    Work,
    Shield,
    Warning,
    Menu,
    Overflow,
    Close,
    Search,
    Sliders,
    ArrowRight,
    Send,
    Plus,
    Lock,
    Network,
    Analytics,
    Device,
    Check,
    Open,
    Freeze,
    Shortcut,
    Details,
    Uninstall,
    Home,
    Settings,
}

@Composable
fun HarborIcon(
    kind: HarborIconKind,
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = HarborColors.textPrimary,
    contentDescription: String? = null,
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        },
    ) {
        val w = size.width
        val h = size.height
        val stroke = (w.coerceAtMost(h) * 0.09f).coerceAtLeast(1.5f)
        val center = Offset(w / 2f, h / 2f)
        val line = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun line(a: Offset, b: Offset, width: Float = stroke) {
            drawLine(tint, a, b, strokeWidth = width, cap = StrokeCap.Round)
        }
        fun circle(point: Offset, radius: Float, fill: Boolean = true) {
            drawCircle(tint, radius, point, style = if (fill) androidx.compose.ui.graphics.drawscope.Fill else line)
        }
        when (kind) {
            HarborIconKind.Lighthouse -> {
                val tower = Path().apply {
                    moveTo(w * .34f, h * .8f); lineTo(w * .43f, h * .38f)
                    lineTo(w * .57f, h * .38f); lineTo(w * .66f, h * .8f)
                }
                drawPath(tower, tint, style = line)
                drawRoundRect(tint, Offset(w * .34f, h * .28f), Size(w * .32f, h * .12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .03f), style = line)
                line(Offset(w * .5f, h * .14f), Offset(w * .5f, h * .28f))
                line(Offset(w * .13f, h * .84f), Offset(w * .87f, h * .84f))
                line(Offset(w * .2f, h * .92f), Offset(w * .8f, h * .92f))
            }
            HarborIconKind.Work -> {
                drawRoundRect(tint, Offset(w * .18f, h * .3f), Size(w * .64f, h * .5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .08f), style = line)
                line(Offset(w * .38f, h * .3f), Offset(w * .38f, h * .2f))
                line(Offset(w * .62f, h * .3f), Offset(w * .62f, h * .2f))
                line(Offset(w * .18f, h * .52f), Offset(w * .82f, h * .52f))
                circle(Offset(w * .5f, h * .52f), stroke * .8f)
            }
            HarborIconKind.Shield -> {
                val shield = Path().apply {
                    moveTo(w * .5f, h * .08f); lineTo(w * .82f, h * .2f)
                    lineTo(w * .78f, h * .57f); quadraticTo(w * .68f, h * .78f, w * .5f, h * .9f)
                    quadraticTo(w * .32f, h * .78f, w * .22f, h * .57f); lineTo(w * .18f, h * .2f); close()
                }
                drawPath(shield, tint, style = line)
                line(Offset(w * .34f, h * .48f), Offset(w * .46f, h * .61f))
                line(Offset(w * .46f, h * .61f), Offset(w * .7f, h * .34f))
            }
            HarborIconKind.Warning -> {
                val warning = Path().apply {
                    moveTo(w * .5f, h * .12f)
                    lineTo(w * .88f, h * .82f)
                    lineTo(w * .12f, h * .82f)
                    close()
                }
                drawPath(warning, tint, style = line)
                line(Offset(w * .5f, h * .36f), Offset(w * .5f, h * .6f))
                circle(Offset(w * .5f, h * .72f), stroke * .7f)
            }
            HarborIconKind.Menu -> listOf(.3f, .5f, .7f).forEach { y -> line(Offset(w * .18f, h * y), Offset(w * .82f, h * y)) }
            HarborIconKind.Overflow -> listOf(.25f, .5f, .75f).forEach { y -> circle(Offset(w * .5f, h * y), stroke * .75f) }
            HarborIconKind.Close -> { line(Offset(w * .25f, h * .25f), Offset(w * .75f, h * .75f)); line(Offset(w * .75f, h * .25f), Offset(w * .25f, h * .75f)) }
            HarborIconKind.Search -> {
                drawCircle(tint, w * .27f, Offset(w * .42f, h * .42f), style = line)
                line(Offset(w * .61f, h * .61f), Offset(w * .83f, h * .83f))
            }
            HarborIconKind.Sliders -> {
                line(Offset(w * .18f, h * .28f), Offset(w * .82f, h * .28f)); line(Offset(w * .18f, h * .5f), Offset(w * .82f, h * .5f)); line(Offset(w * .18f, h * .72f), Offset(w * .82f, h * .72f))
                circle(Offset(w * .62f, h * .28f), stroke * .7f); circle(Offset(w * .36f, h * .5f), stroke * .7f); circle(Offset(w * .58f, h * .72f), stroke * .7f)
            }
            HarborIconKind.ArrowRight -> { line(Offset(w * .18f, h * .5f), Offset(w * .78f, h * .5f)); line(Offset(w * .58f, h * .3f), Offset(w * .8f, h * .5f)); line(Offset(w * .58f, h * .7f), Offset(w * .8f, h * .5f)) }
            HarborIconKind.Send -> { val p = Path().apply { moveTo(w * .15f, h * .52f); lineTo(w * .85f, h * .16f); lineTo(w * .64f, h * .84f); lineTo(w * .48f, h * .57f); close() }; drawPath(p, tint, style = line); line(Offset(w * .48f, h * .57f), Offset(w * .85f, h * .16f)) }
            HarborIconKind.Plus -> { drawCircle(tint, w * .34f, center, style = line); line(Offset(w * .5f, h * .3f), Offset(w * .5f, h * .7f)); line(Offset(w * .3f, h * .5f), Offset(w * .7f, h * .5f)) }
            HarborIconKind.Lock -> { drawRoundRect(tint, Offset(w * .22f, h * .42f), Size(w * .56f, h * .42f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .06f), style = line); drawArc(tint, 180f, 180f, false, Offset(w * .32f, h * .12f), Size(w * .36f, h * .45f), style = line) }
            HarborIconKind.Network -> { line(Offset(w * .5f, h * .18f), Offset(w * .5f, h * .8f)); line(Offset(w * .22f, h * .36f), Offset(w * .78f, h * .36f)); line(Offset(w * .22f, h * .64f), Offset(w * .78f, h * .64f)); line(Offset(w * .22f, h * .36f), Offset(w * .5f, h * .18f)); line(Offset(w * .78f, h * .36f), Offset(w * .5f, h * .18f)) }
            HarborIconKind.Analytics -> { line(Offset(w * .18f, h * .82f), Offset(w * .18f, h * .52f)); line(Offset(w * .42f, h * .82f), Offset(w * .42f, h * .34f)); line(Offset(w * .66f, h * .82f), Offset(w * .66f, h * .2f)); line(Offset(w * .18f, h * .82f), Offset(w * .82f, h * .82f)) }
            HarborIconKind.Device -> { drawRoundRect(tint, Offset(w * .25f, h * .12f), Size(w * .5f, h * .76f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .07f), style = line); line(Offset(w * .43f, h * .72f), Offset(w * .57f, h * .72f)) }
            HarborIconKind.Check -> { line(Offset(w * .2f, h * .5f), Offset(w * .43f, h * .72f)); line(Offset(w * .43f, h * .72f), Offset(w * .82f, h * .28f)) }
            HarborIconKind.Open -> { drawRoundRect(tint, Offset(w * .18f, h * .18f), Size(w * .58f, h * .64f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .06f), style = line); line(Offset(w * .48f, h * .52f), Offset(w * .82f, h * .18f)); line(Offset(w * .58f, h * .18f), Offset(w * .82f, h * .18f)); line(Offset(w * .82f, h * .18f), Offset(w * .82f, h * .42f)) }
            HarborIconKind.Freeze -> { circle(center, w * .28f, fill = false); line(Offset(w * .5f, h * .12f), Offset(w * .5f, h * .88f)); line(Offset(w * .18f, h * .3f), Offset(w * .82f, h * .7f)); line(Offset(w * .82f, h * .3f), Offset(w * .18f, h * .7f)) }
            HarborIconKind.Shortcut -> {
                drawRoundRect(
                    tint,
                    Offset(w * .16f, h * .3f),
                    Size(w * .4f, h * .42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .06f),
                    style = line,
                )
                line(Offset(w * .48f, h * .52f), Offset(w * .82f, h * .18f))
                line(Offset(w * .64f, h * .18f), Offset(w * .82f, h * .18f))
                line(Offset(w * .82f, h * .18f), Offset(w * .82f, h * .36f))
            }
            HarborIconKind.Details -> { drawCircle(tint, w * .38f, center, style = line); line(Offset(w * .5f, h * .47f), Offset(w * .5f, h * .72f)); circle(Offset(w * .5f, h * .3f), stroke * .75f) }
            HarborIconKind.Uninstall -> { drawRoundRect(tint, Offset(w * .3f, h * .28f), Size(w * .4f, h * .58f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .03f), style = line); line(Offset(w * .24f, h * .2f), Offset(w * .76f, h * .2f)); line(Offset(w * .4f, h * .12f), Offset(w * .6f, h * .12f)); line(Offset(w * .43f, h * .4f), Offset(w * .43f, h * .7f)); line(Offset(w * .57f, h * .4f), Offset(w * .57f, h * .7f)) }
            HarborIconKind.Home -> { val p = Path().apply { moveTo(w * .16f, h * .48f); lineTo(w * .5f, h * .18f); lineTo(w * .84f, h * .48f); lineTo(w * .78f, h * .48f); lineTo(w * .78f, h * .84f); lineTo(w * .22f, h * .84f); lineTo(w * .22f, h * .48f); close() }; drawPath(p, tint, style = line) }
            HarborIconKind.Settings -> { drawCircle(tint, w * .3f, center, style = line); drawCircle(tint, w * .08f, center); listOf(0f, 60f, 120f).forEach { angle -> val rad = Math.toRadians(angle.toDouble()); val a = Offset(center.x + kotlin.math.cos(rad).toFloat() * w * .38f, center.y + kotlin.math.sin(rad).toFloat() * h * .38f); val b = Offset(center.x + kotlin.math.cos(rad).toFloat() * w * .5f, center.y + kotlin.math.sin(rad).toFloat() * h * .5f); line(a, b) } }
        }
    }
}

@Composable
fun HarborBrandMark(
    work: Boolean = false,
    modifier: Modifier = Modifier.size(36.dp),
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(11.dp), color = HarborColors.accentDark) {
        Box(contentAlignment = Alignment.Center) {
            HarborIcon(if (work) HarborIconKind.Work else HarborIconKind.Lighthouse, Modifier.size(24.dp), HarborColors.accent, "Harbor")
        }
    }
}

@Composable
fun HarborHeader(
    title: String,
    subtitle: String? = null,
    work: Boolean = false,
    onMenu: (() -> Unit)? = null,
    onOverflow: (() -> Unit)? = null,
    onAdvanced: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = if (subtitle == null) 72.dp else 78.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        onMenu?.let { action -> HarborIconButton(HarborIconKind.Menu, "Open Work navigation", action) }
        HarborBrandMark(work = work)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = HarborColors.textPrimary, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium))
            subtitle?.let { Text(it, color = HarborColors.textMuted, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
        onOverflow?.let { action -> HarborIconButton(HarborIconKind.Overflow, "Open Harbor controls", action) }
        onAdvanced?.let { action -> HarborIconButton(HarborIconKind.Shield, "Open Advanced tools", action, container = HarborColors.accentDark, tint = HarborColors.accent) }
    }
}

@Composable
fun HarborIconButton(
    icon: HarborIconKind,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(48.dp),
    container: Color = Color.Transparent,
    tint: Color = HarborColors.textPrimary,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        shape = RoundedCornerShape(14.dp),
        color = container,
        contentColor = tint,
    ) { Box(contentAlignment = Alignment.Center) { HarborIcon(icon, Modifier.size(24.dp), tint, description) } }
}

@Composable
fun HarborStatusPill(
    text: String,
    tone: StatusTone = StatusTone.Neutral,
    modifier: Modifier = Modifier,
) {
    val (container, content) = when (tone) {
        StatusTone.Positive -> HarborColors.positiveBg to HarborColors.positive
        StatusTone.Neutral -> HarborColors.surfaceLow to HarborColors.textSecondary
        StatusTone.Warning -> Color(0xFF3A3020) to HarborColors.warning
        StatusTone.Critical -> Color(0xFF422125) to HarborColors.danger
    }
    Surface(modifier = modifier, shape = HarborShapes.pill, color = container, contentColor = content) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (tone == StatusTone.Neutral) HarborIcon(HarborIconKind.Freeze, Modifier.size(14.dp), content)
            Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
fun HarborQuickActionTile(
    label: String,
    icon: HarborIconKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, modifier = modifier.heightIn(min = 92.dp), shape = HarborShapes.tile, color = HarborColors.surfaceLow, contentColor = HarborColors.textPrimary) {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HarborIcon(icon, Modifier.size(30.dp), HarborColors.accent, label)
            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium))
        }
    }
}

data class PrivacyFact(val title: String, val body: String, val icon: HarborIconKind = HarborIconKind.Check)

@Composable
fun HarborPrivacyPanel(
    facts: List<PrivacyFact>,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = HarborShapes.card, color = HarborColors.surfaceLow, border = androidx.compose.foundation.BorderStroke(1.dp, HarborColors.stroke)) {
        Column {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HarborIcon(HarborIconKind.Lock, Modifier.size(25.dp), HarborColors.textSecondary, "Privacy")
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Privacy by default", color = HarborColors.textPrimary, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Built for calm. Designed for control.", color = HarborColors.textSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            facts.forEachIndexed { index, fact ->
                if (index > 0) androidx.compose.material3.HorizontalDivider(color = HarborColors.stroke)
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HarborIcon(fact.icon, Modifier.size(28.dp), HarborColors.textPrimary, fact.title)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(fact.title, color = HarborColors.textPrimary, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                        Text(fact.body, color = HarborColors.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Surface(shape = RoundedCornerShape(50), color = HarborColors.accentDark, modifier = Modifier.size(28.dp)) { Box(contentAlignment = Alignment.Center) { HarborIcon(HarborIconKind.Check, Modifier.size(17.dp), HarborColors.accent) } }
                }
            }
        }
    }
}

@Composable
fun HarborBottomBar(
    active: HarborIconKind,
    onPersonal: (() -> Unit)? = null,
    onWork: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), color = HarborColors.surface, shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp), tonalElevation = 0.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)) {
            HarborBottomItem("Personal", HarborIconKind.Home, active == HarborIconKind.Home, onPersonal, Modifier.weight(1f))
            HarborBottomItem("Work", HarborIconKind.Work, active == HarborIconKind.Work, onWork, Modifier.weight(1f))
            HarborBottomItem("Settings", HarborIconKind.Settings, active == HarborIconKind.Settings, onSettings, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HarborBottomItem(label: String, icon: HarborIconKind, selected: Boolean, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(enabled = onClick != null) { onClick?.invoke() }.padding(horizontal = 4.dp, vertical = 7.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        HarborIcon(icon, Modifier.size(25.dp), if (selected) HarborColors.accent else HarborColors.textSecondary, label)
        Text(label, modifier = Modifier.fillMaxWidth(), color = if (selected) HarborColors.accent else HarborColors.textSecondary, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Visible, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
fun HarborSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp).clip(HarborShapes.search).background(HarborColors.surfaceLow).padding(horizontal = 16.dp),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = HarborColors.textPrimary),
        decorationBox = { inner ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HarborIcon(HarborIconKind.Search, Modifier.size(23.dp), HarborColors.textSecondary, "Search")
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text("Search apps", color = HarborColors.textSecondary, style = MaterialTheme.typography.bodyLarge)
                    inner()
                }
                HarborIcon(HarborIconKind.Sliders, Modifier.size(23.dp), HarborColors.textSecondary, "Filter")
            }
        },
    )
}

@Composable
fun HarborHeroBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(Brush.linearGradient(listOf(HarborColors.heroStart, HarborColors.heroEnd), Offset.Zero, Offset(size.width, size.height)))
        val designHeight = minOf(size.height, 410.dp.toPx())
        val beam = Path().apply {
            moveTo(size.width * .82f, designHeight * .28f)
            lineTo(size.width * .34f, designHeight * .17f)
            lineTo(size.width * .34f, designHeight * .4f)
            close()
        }
        drawPath(beam, Color(0xFF1BA6A8).copy(alpha = .2f))
        val rocks = Path().apply { moveTo(size.width * .53f, designHeight); cubicTo(size.width * .64f, designHeight * .78f, size.width * .77f, designHeight * .9f, size.width, designHeight * .67f); lineTo(size.width, designHeight); close() }
        drawPath(rocks, Color(0xFF031D25))
        val waves = Path().apply { moveTo(size.width * .48f, designHeight * .82f); cubicTo(size.width * .62f, designHeight * .72f, size.width * .73f, designHeight * .88f, size.width, designHeight * .76f); moveTo(size.width * .57f, designHeight * .9f); cubicTo(size.width * .7f, designHeight * .82f, size.width * .83f, designHeight * .96f, size.width, designHeight * .86f) }
        drawPath(waves, Color(0xFF0D5A64), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        val tower = Path().apply { moveTo(size.width * .72f, designHeight * .72f); lineTo(size.width * .77f, designHeight * .3f); lineTo(size.width * .88f, designHeight * .3f); lineTo(size.width * .91f, designHeight * .72f); close() }
        drawPath(tower, Color(0xFF02141B))
        drawRoundRect(Color(0xFFB1E7E4), Offset(size.width * .755f, designHeight * .23f), Size(size.width * .14f, designHeight * .12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
        drawRoundRect(Color(0xFF03151B), Offset(size.width * .77f, designHeight * .27f), Size(size.width * .11f, designHeight * .08f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))
        drawCircle(HarborColors.accent, 3.dp.toPx(), Offset(size.width * .25f, designHeight * .25f))
        drawCircle(HarborColors.accent, 2.dp.toPx(), Offset(size.width * .48f, designHeight * .16f))
        drawCircle(HarborColors.accent, 2.dp.toPx(), Offset(size.width * .9f, designHeight * .52f))
    }
}

@Composable
fun HarborSpacer(height: Dp = HarborSpacing.section) { Spacer(Modifier.height(height)) }
