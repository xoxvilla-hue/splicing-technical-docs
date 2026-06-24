package com.example.calendarwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// รีเซ็ต alarm หลังรีบูตเครื่อง เพราะ AlarmManager ถูกล้างเมื่อปิด
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            MidnightScheduler.schedule(context)
        }
    }
}
