package com.example.calendarwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NotificationHelper {

    private const val CHANNEL_ID = "daily_reminder"
    private const val CHANNEL_NAME = "แจ้งเตือนนัดหมายประจำวัน"
    private const val NOTIF_ID = 2001

    fun createChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "แจ้งเตือนตีเที่ยงคืนเมื่อมีนัดหมายในวันถัดไป"
            enableLights(true)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun sendDailyReminder(context: Context, events: List<CalendarEvent>) {
        createChannel(context)

        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("show_tomorrow", true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigText = buildBigText(events)
        val summaryLine = "มีนัดหมาย ${events.size} รายการในพรุ่งนี้"

        val style = NotificationCompat.BigTextStyle()
            .bigText(bigText)
            .setSummaryText(summaryLine)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📅 นัดหมายพรุ่งนี้")
            .setContentText(summaryLine)
            .setStyle(style)
            .setColor(0xFF9C88FF.toInt())
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIF_ID, notification)
    }

    private fun buildBigText(events: List<CalendarEvent>): String {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        return events.joinToString(separator = "\n") { event ->
            val time = fmt.format(Date(event.startTime))
            val loc = event.location?.let { "  •  $it" } ?: ""
            "🔹 $time  ${event.title}$loc"
        }
    }
}
