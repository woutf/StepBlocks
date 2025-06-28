package com.stepblocks.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalTime
import java.util.Date

class Converters {
    @TypeConverter
    fun fromBlockProgressList(value: List<BlockProgress>): String {
        val gson = Gson()
        val type = object : TypeToken<List<BlockProgress>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toBlockProgressList(value: String): List<BlockProgress> {
        val gson = Gson()
        val type = object : TypeToken<List<BlockProgress>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun localTimeToString(localTime: LocalTime?): String? {
        return localTime?.toString()
    }
}
