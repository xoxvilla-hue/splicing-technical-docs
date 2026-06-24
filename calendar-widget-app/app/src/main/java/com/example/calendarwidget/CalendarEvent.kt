package com.example.calendarwidget

data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val calendarColor: Int = 0xFF9C88FF.toInt()
)
