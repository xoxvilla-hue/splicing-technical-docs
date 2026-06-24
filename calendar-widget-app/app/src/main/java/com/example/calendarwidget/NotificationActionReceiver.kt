package com.example.calendarwidget

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DISMISS = "com.example.calendarwidget.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.example.calendarwidget.ACTION_SNOOZE"
        const val EXTRA_NOTIF_ID = "notif_id"
        const val EXTRA_EVENT_IDS = "event_ids"

        fun dismissIntent(context: Context, notifId: Int, eventIds: Array<String>): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_EVENT_IDS, eventIds)
            }
            return PendingIntent.getBroadcast(
                context, notifId * 10 + 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun snoozeIntent(context: Context, notifId: Int, eventIds: Array<String>): PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_NOTIF_ID, notifId)
                putExtra(EXTRA_EVENT_IDS, eventIds)
            }
            return PendingIntent.getBroadcast(
                context, notifId * 10 + 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getIntExtra(EXTRA_NOTIF_ID, 0)
        val eventIds = intent.getStringArrayExtra(EXTRA_EVENT_IDS) ?: emptyArray()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(notifId)

        when (intent.action) {
            ACTION_DISMISS -> {
                eventIds.forEach { DismissedEventsStore.markDismissed(context, it) }
            }

            ACTION_SNOOZE -> {
                // แจ้งเตือนอีกครั้งตอน 08:00 น. ของวันพรุ่งนี้ (เช้าก่อนนัด)
                val snoozeTime = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val snoozeIntent = PendingIntent.getBroadcast(
                    context,
                    notifId * 10 + 3,
                    Intent(context, MidnightReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val alarmManager = context.getSystemService(AlarmManager::class.java)
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    snoozeIntent
                )

                // แจ้งให้รู้ว่า snooze สำเร็จ
                NotificationHelper.sendSnoozeConfirmation(context, notifId + 100)
            }
        }
    }
}
