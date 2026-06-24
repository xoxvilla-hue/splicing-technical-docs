package com.example.calendarwidget

import android.content.Context
import android.provider.CalendarContract
import java.util.Calendar

class CalendarRepository(private val context: Context) {

    fun getTodayEvents(): List<CalendarEvent> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CALENDAR_COLOR
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? " +
                "AND ${CalendarContract.Events.DTSTART} <= ? " +
                "AND ${CalendarContract.Events.DELETED} = 0"

        val events = mutableListOf<CalendarEvent>()

        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            arrayOf(startOfDay.toString(), endOfDay.toString()),
            "${CalendarContract.Events.DTSTART} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                events.add(
                    CalendarEvent(
                        id = cursor.getString(0) ?: "",
                        title = cursor.getString(1) ?: "ไม่มีชื่อ",
                        startTime = cursor.getLong(2),
                        endTime = cursor.getLong(3),
                        location = cursor.getString(4),
                        calendarColor = cursor.getInt(5).takeIf { it != 0 }
                            ?: 0xFF9C88FF.toInt()
                    )
                )
            }
        }

        return events
    }
}
