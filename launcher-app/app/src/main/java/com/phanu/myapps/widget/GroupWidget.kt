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
import com.phanu.myapps.data.GroupRepository
import com.phanu.myapps.data.getAppIcon

class GroupWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val groups = GroupRepository(context).getGroups()

        // Pre-load icons (up to 4 per group)
        val iconMap = mutableMapOf<String, Bitmap?>()
        groups.forEach { group ->
            group.packageNames.take(4).forEach { pkg ->
                if (!iconMap.containsKey(pkg)) {
                    iconMap[pkg] = context.getAppIcon(pkg)
                }
            }
        }

        provideContent {
            GlanceTheme {
                WidgetContent(groups = groups, iconMap = iconMap)
            }
        }
    }
}

@Composable
private fun WidgetContent(groups: List<AppGroup>, iconMap: Map<String, Bitmap?>) {
    val bgColor = Color(0xFF07070A)
    val surfaceColor = Color(0xFF0D0C09)
    val gold = Color(0xFFC9A84C)
    val goldLight = Color(0xFFE8C56A)
    val goldFaint = Color(0xFF3A3015)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bgColor))
            .cornerRadius(20.dp)
            .padding(10.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // Header row
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MY APPS",
                    style = TextStyle(
                        color = ColorProvider(gold),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Group grid — 2 columns
            val rows = groups.chunked(2)
            Column(modifier = GlanceModifier.fillMaxSize()) {
                rows.forEachIndexed { rowIdx, rowGroups ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        rowGroups.forEachIndexed { colIdx, group ->
                            GroupTile(
                                group = group,
                                iconMap = iconMap,
                                surfaceColor = surfaceColor,
                                gold = goldLight,
                                goldFaint = goldFaint,
                                modifier = GlanceModifier.defaultWeight().padding(
                                    end = if (colIdx == 0) 4.dp else 0.dp
                                )
                            )
                        }
                        // Fill empty slot if odd number
                        if (rowGroups.size == 1) {
                            Box(modifier = GlanceModifier.defaultWeight()) {}
                        }
                    }
                    if (rowIdx < rows.lastIndex) {
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupTile(
    group: AppGroup,
    iconMap: Map<String, Bitmap?>,
    surfaceColor: Color,
    gold: Color,
    goldFaint: Color,
    modifier: GlanceModifier
) {
    Box(
        modifier = modifier
            .background(ColorProvider(surfaceColor))
            .cornerRadius(14.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (group.pin != null) {
                    Text(
                        text = "🔒 ",
                        style = TextStyle(fontSize = 9.sp)
                    )
                }
                Text(
                    text = group.name,
                    style = TextStyle(
                        color = ColorProvider(gold),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
            }
            Text(
                text = "${group.packageNames.size} แอป",
                style = TextStyle(
                    color = ColorProvider(goldFaint),
                    fontSize = 8.sp
                )
            )

            // App icons row
            val previewPkgs = group.packageNames.take(4)
            if (previewPkgs.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(6.dp))
                Row(horizontalAlignment = Alignment.Start) {
                    previewPkgs.forEach { pkg ->
                        val bmp = iconMap[pkg]
                        if (bmp != null) {
                            Box(
                                modifier = GlanceModifier
                                    .size(22.dp)
                                    .background(ColorProvider(Color(0xFF111008)))
                                    .cornerRadius(6.dp)
                                    .padding(2.dp)
                            ) {
                                Image(
                                    provider = ImageProvider(bmp),
                                    contentDescription = null,
                                    modifier = GlanceModifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = GlanceModifier.width(4.dp))
                        }
                    }
                }
            }
        }
    }
}
