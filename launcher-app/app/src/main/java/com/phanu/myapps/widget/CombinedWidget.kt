package com.phanu.myapps.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.phanu.myapps.MainActivity
import com.phanu.myapps.data.AppGroup
import com.phanu.myapps.data.CalendarEvent
import com.phanu.myapps.data.CalendarRepository
import com.phanu.myapps.data.GroupRepository
import com.phanu.myapps.data.getAppIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CombinedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = try { CalendarRepository(context).getTodayEvents() } catch (_: Exception) { emptyList() }
        val groups = GroupRepository(context).getGroups()

        val iconMap = mutableMapOf<String, Bitmap?>()
        groups.forEach { group ->
            group.packageNames.take(4).forEach { pkg ->
                if (!iconMap.containsKey(pkg)) iconMap[pkg] = context.getAppIcon(pkg)
            }
        }

        provideContent {
            GlanceTheme {
                CombinedWidgetContent(events = events, groups = groups, iconMap = iconMap)
            }
        }
    }
}

// ── Colours ─────────────────────────────────────────────────────────────────
private val BG        = Color(0xFF07070A)
private val SURFACE   = Color(0xFF0D0C09)
private val BORDER    = Color(0xFF1C1808)
private val GOLD      = Color(0xFFC9A84C)
private val GOLD_LT   = Color(0xFFE8C56A)
private val GOLD_DIM  = Color(0xFF8A7340)
private val GOLD_FAINT= Color(0xFF3A3015)
private val WHITE     = Color(0xFFEEEEEE)
private val RED       = Color(0xFFCC4444)

@Composable
private fun CombinedWidgetContent(
    events: List<CalendarEvent>,
    groups: List<AppGroup>,
    iconMap: Map<String, Bitmap?>
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(BG))
            .cornerRadius(22.dp)
            .padding(14.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // ── CALENDAR SECTION ────────────────────────────────────────────
            CalendarSection(events = events)

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Gold divider
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorProvider(Color(0xFF2A2010)))
            ) {}

            Spacer(modifier = GlanceModifier.height(10.dp))

            // ── GROUPS SECTION ──────────────────────────────────────────────
            GroupsSection(groups = groups, iconMap = iconMap)

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Signature
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "By Phanu",
                    style = TextStyle(color = ColorProvider(GOLD_FAINT), fontSize = 8.sp)
                )
            }
        }
    }
}

// ── Calendar section ─────────────────────────────────────────────────────────

@Composable
private fun CalendarSection(events: List<CalendarEvent>) {
    val dayName = SimpleDateFormat("EEEE", Locale("th")).format(Date()).uppercase()
    val fullDate = SimpleDateFormat("d MMMM yyyy", Locale("th")).format(Date())

    Column(modifier = GlanceModifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = dayName,
                    style = TextStyle(color = ColorProvider(GOLD), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = fullDate,
                    style = TextStyle(color = ColorProvider(WHITE), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                )
            }
            // Event count badge
            if (events.isNotEmpty()) {
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(Color(0xFF1A1508)))
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${events.size} นัด",
                        style = TextStyle(color = ColorProvider(GOLD_DIM), fontSize = 9.sp)
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (events.isEmpty()) {
            Text(
                text = "ไม่มีนัดหมายวันนี้  ✨",
                style = TextStyle(color = ColorProvider(GOLD_FAINT), fontSize = 11.sp)
            )
        } else {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                events.take(3).forEachIndexed { i, event ->
                    CalendarEventRow(event = event)
                    if (i < minOf(events.size, 3) - 1) {
                        Spacer(modifier = GlanceModifier.height(5.dp))
                    }
                }
                if (events.size > 3) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "+ ${events.size - 3} รายการ",
                        style = TextStyle(color = ColorProvider(GOLD_FAINT), fontSize = 9.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarEventRow(event: CalendarEvent) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(SURFACE))
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold left bar
            Box(
                modifier = GlanceModifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(ColorProvider(GOLD))
                    .cornerRadius(1.dp)
            ) {}

            Spacer(modifier = GlanceModifier.width(8.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = event.title,
                    style = TextStyle(color = ColorProvider(WHITE), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
                Text(
                    text = "${fmt.format(Date(event.startTime))} – ${fmt.format(Date(event.endTime))}" +
                            (event.location?.let { "  •  $it" } ?: ""),
                    style = TextStyle(color = ColorProvider(GOLD_DIM), fontSize = 10.sp),
                    maxLines = 1
                )
            }
        }
    }
}

// ── Groups section ───────────────────────────────────────────────────────────

@Composable
private fun GroupsSection(groups: List<AppGroup>, iconMap: Map<String, Bitmap?>) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Text(
            text = "MY APPS",
            style = TextStyle(color = ColorProvider(GOLD_DIM), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = GlanceModifier.height(6.dp))

        if (groups.isEmpty()) {
            Text(
                text = "เปิดแอปเพื่อสร้างกลุ่ม",
                style = TextStyle(color = ColorProvider(GOLD_FAINT), fontSize = 10.sp)
            )
        } else {
            val rows = groups.take(6).chunked(3)
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                rows.forEachIndexed { rowIdx, rowGroups ->
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        rowGroups.forEachIndexed { colIdx, group ->
                            GroupChip(
                                group = group,
                                iconMap = iconMap,
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .padding(end = if (colIdx < rowGroups.lastIndex) 5.dp else 0.dp)
                            )
                        }
                        repeat(3 - rowGroups.size) {
                            Spacer(modifier = GlanceModifier.defaultWeight())
                        }
                    }
                    if (rowIdx < rows.lastIndex) {
                        Spacer(modifier = GlanceModifier.height(5.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupChip(
    group: AppGroup,
    iconMap: Map<String, Bitmap?>,
    modifier: GlanceModifier
) {
    Box(
        modifier = modifier
            .background(ColorProvider(SURFACE))
            .cornerRadius(12.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (group.pin != null) {
                    Text(text = "🔒 ", style = TextStyle(fontSize = 8.sp))
                }
                Text(
                    text = group.name,
                    style = TextStyle(
                        color = ColorProvider(GOLD_LT),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }

            // App icons row
            val previewPkgs = group.packageNames.take(3)
            if (previewPkgs.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row {
                    previewPkgs.forEach { pkg ->
                        val bmp = iconMap[pkg]
                        if (bmp != null) {
                            Box(
                                modifier = GlanceModifier
                                    .size(18.dp)
                                    .background(ColorProvider(Color(0xFF111008)))
                                    .cornerRadius(5.dp)
                                    .padding(1.dp)
                            ) {
                                Image(
                                    provider = ImageProvider(bmp),
                                    contentDescription = null,
                                    modifier = GlanceModifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = GlanceModifier.width(3.dp))
                        }
                    }
                }
            }
        }
    }
}
