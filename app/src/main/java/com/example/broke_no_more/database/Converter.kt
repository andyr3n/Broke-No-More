package com.example.broke_no_more.database

import android.icu.util.Calendar
import androidx.room.TypeConverter

class Converter {
    //Convert from Calendar to Long
    @TypeConverter
    fun convertCalendarToLong(calendar: Calendar): Long{
        return calendar.timeInMillis
    }

    //Convert from Long back to Calendar
    @TypeConverter
    fun convertLongToCalendar(calendarLong: Long): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = calendarLong
        return calendar
    }
}