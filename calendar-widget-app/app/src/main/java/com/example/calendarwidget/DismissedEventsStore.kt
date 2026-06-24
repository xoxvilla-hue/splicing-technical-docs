package com.example.calendarwidget

import android.content.Context

object DismissedEventsStore {

    private const val PREF_NAME = "dismissed_events"
    private const val KEY_IDS = "ids"

    fun markDismissed(context: Context, eventId: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_IDS, mutableSetOf())!!.toMutableSet()
        current.add(eventId)
        prefs.edit().putStringSet(KEY_IDS, current).apply()
    }

    fun isDismissed(context: Context, eventId: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_IDS, emptySet())!!.contains(eventId)
    }

    // ล้างทุกวันเมื่อ alarm ยิงรอบใหม่
    fun clearAll(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
