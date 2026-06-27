package com.example.calendarwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = CalendarRepository(context).getTodayEvents()

        provideContent {
            GlanceTheme {
                WidgetContent(events)
            }
        }
    }
}

@Composable
private fun WidgetContent(events: List<CalendarEvent>) {
    val bgColor = Color(0xFF0F0F1A)
    val surfaceColor = Color(0xFF1C1C2E)
    val accentColor = Color(0xFF9C88FF)
    val textColor = Color(0xFFEEEEEE)
    val subTextColor = Color(0xFF888899)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bgColor))
            .cornerRadius(20)
            .padding(14.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {

            // Header — วันที่วันนี้
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅",
                    style = TextStyle(fontSize = 14.sp)
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Column {
                    Text(
                        text = getTodayLabel(),
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = getFullDate(),
                        style = TextStyle(
                            color = ColorProvider(textColor),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Divider
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorProvider(Color(0xFF2A2A3E)))
            ) {}

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Event list
            if (events.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ไม่มีนัดหมายวันนี้ ✨",
                        style = TextStyle(
                            color = ColorProvider(subTextColor),
                            fontSize = 13.sp
                        )
                    )
                }
            } else {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    val visibleEvents = events.take(4)
                    visibleEvents.forEachIndexed { index, event ->
                        EventRow(
                            event = event,
                            surfaceColor = surfaceColor,
                            textColor = textColor,
                            subTextColor = subTextColor
                        )
                        if (index < visibleEvents.lastIndex) {
                            Spacer(modifier = GlanceModifier.height(6.dp))
                        }
                    }

                    if (events.size > 4) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = "+ ${events.size - 4} รายการ",
                            style = TextStyle(
                                color = ColorProvider(subTextColor),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    surfaceColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(ColorProvider(surfaceColor))
            .cornerRadius(10)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // สีเส้นข้างซ้ายแทน dot
        Box(
            modifier = GlanceModifier
                .width(3.dp)
                .height(32.dp)
                .background(ColorProvider(Color(event.calendarColor)))
                .cornerRadius(2)
        ) {}

        Spacer(modifier = GlanceModifier.width(9.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = event.title,
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = buildTimeLabel(event),
                style = TextStyle(
                    color = ColorProvider(subTextColor),
                    fontSize = 11.sp
                )
            )
        }
    }
}

private fun getTodayLabel(): String {
    return SimpleDateFormat("EEEE", Locale("th")).format(Date()).uppercase()
}

private fun getFullDate(): String {
    return SimpleDateFormat("d MMMM yyyy", Locale("th")).format(Date())
}

private fun buildTimeLabel(event: CalendarEvent): String {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val start = fmt.format(Date(event.startTime))
    val end = fmt.format(Date(event.endTime))
    return if (event.location != null) "$start – $end  •  ${event.location}" else "$start – $end"
}
