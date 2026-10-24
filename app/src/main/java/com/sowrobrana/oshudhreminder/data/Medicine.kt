package com.sowrobrana.oshudhreminder.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

/** One medicine with its daily schedule. */
@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    /** e.g. "১টি ট্যাবলেট" */
    val dose: String,
    /** Minutes since midnight, e.g. 480 = 08:00. */
    val times: List<Int>,
    /** true = খাবারের আগে, false = খাবারের পরে */
    val beforeFood: Boolean,
    /** 0 = continuous (no end date). */
    val durationDays: Int = 0,
    /** Epoch day the course starts. */
    val startEpochDay: Long,
    /** -1 = stock not tracked. Otherwise remaining units. */
    val stock: Int = -1,
    val stockAlertAt: Int = 5,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

/** One dose occurrence and what happened to it. */
@Entity(
    tableName = "dose_logs",
    indices = [Index(value = ["medicineId", "dateEpochDay", "timeMinutes"], unique = true)],
)
data class DoseLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val dateEpochDay: Long,
    val timeMinutes: Int,
    /** TAKEN, SKIPPED */
    val status: String,
    val loggedAt: Long = System.currentTimeMillis(),
)

class Converters {
    @TypeConverter
    fun timesToString(times: List<Int>): String = times.joinToString(",")

    @TypeConverter
    fun stringToTimes(value: String): List<Int> =
        value.split(",").mapNotNull { it.toIntOrNull() }
}
