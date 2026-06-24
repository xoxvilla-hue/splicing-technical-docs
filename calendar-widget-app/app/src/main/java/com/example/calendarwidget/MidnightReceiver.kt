package com.example.calendarwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        DismissedEventsStore.clearAll(context)
        val tomorrow = CalendarRepository(context).getTomorrowEvents()

        if (tomorrow.isNotEmpty()) {
            NotificationHelper.sendDailyReminder(context, tomorrow)
        }

        // จองนาฬิกาสำหรับคืนถัดไปต่อเนื่อง
        MidnightScheduler.schedule(context)
    }
}
