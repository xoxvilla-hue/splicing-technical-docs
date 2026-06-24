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

    private const val CHANNEL_DAILY = "daily_reminder"
    private const val CHANNEL_SNOOZE = "snooze_confirm"
    const val NOTIF_ID_DAILY = 2001

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        if (manager.getNotificationChannel(CHANNEL_DAILY) == null) {
            NotificationChannel(
                CHANNEL_DAILY,
                "แจ้งเตือนนัดหมายประจำวัน",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)
            }.also { manager.createNotificationChannel(it) }
        }

        if (manager.getNotificationChannel(CHANNEL_SNOOZE) == null) {
            NotificationChannel(
                CHANNEL_SNOOZE,
                "ยืนยันการเลื่อนนัด",
                NotificationManager.IMPORTANCE_LOW
            ).also { manager.createNotificationChannel(it) }
        }
    }

    fun sendDailyReminder(context: Context, events: List<CalendarEvent>) {
        createChannels(context)

        // กรองรายการที่อ่านแล้ว
        val pending = events.filter { !DismissedEventsStore.isDismissed(context, it.id) }
        if (pending.isEmpty()) return

        val eventIds = pending.map { it.id }.toTypedArray()
        val openIntent = buildOpenIntent(context, showTomorrow = true)

        val dismissPi = NotificationActionReceiver.dismissIntent(context, NOTIF_ID_DAILY, eventIds)
        val snoozePi = NotificationActionReceiver.snoozeIntent(context, NOTIF_ID_DAILY, eventIds)

        val bigText = buildBigText(pending)
        val summaryLine = "มีนัดหมาย ${pending.size} รายการในพรุ่งนี้"

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📅 นัดหมายพรุ่งนี้")
            .setContentText(summaryLine)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setSummaryText(summaryLine)
            )
            .setColor(0xFF9C88FF.toInt())
            .setColorized(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openIntent)
            .setAutoCancel(false) // ไม่ปิดอัตโนมัติ รอให้กดปุ่ม
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "✅ อ่านแล้ว",
                dismissPi
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "⏰ เตือนอีกครั้ง 08:00 น.",
                snoozePi
            )
            .build()

        context.getSystemService(NotificationManager::class.java).notify(NOTIF_ID_DAILY, notification)
    }

    fun sendSnoozeConfirmation(context: Context, notifId: Int) {
        createChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_SNOOZE)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentTitle("⏰ ตั้งเตือนใหม่แล้ว")
            .setContentText("จะแจ้งเตือนอีกครั้งตอน 08:00 น. พรุ่งนี้")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }

    // ใช้ใน createChannel เดิม (backward compat)
    fun createChannel(context: Context) = createChannels(context)

    private fun buildOpenIntent(context: Context, showTomorrow: Boolean): PendingIntent =
        PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("show_tomorrow", showTomorrow)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun buildBigText(events: List<CalendarEvent>): String {
        val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        return events.joinToString(separator = "\n") { event ->
            val time = fmt.format(Date(event.startTime))
            val loc = event.location?.let { "  •  $it" } ?: ""
            "🔹 $time  ${event.title}$loc"
        }
    }
}
