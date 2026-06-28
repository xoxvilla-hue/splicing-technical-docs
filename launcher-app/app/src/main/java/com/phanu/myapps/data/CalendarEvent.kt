package com.phanu.myapps.data

data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val calendarColor: Int = 0xFFC9A84C.toInt()
)
