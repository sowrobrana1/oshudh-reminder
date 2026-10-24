package com.sowrobrana.oshudhreminder.alarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sowrobrana.oshudhreminder.data.Medicine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Exact daily alarms, one per (medicine, time). When an alarm fires, the
 * receiver shows the notification and schedules the next day's occurrence,
 * so the chain never needs a periodic worker.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun requestCode(medicineId: Int, timeMinutes: Int): Int =
        medicineId * 100000 + timeMinutes

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleMedicine(medicine: Medicine) {
        medicine.times.forEach { time ->
            scheduleDose(medicine.id, time, nextTriggerMillis(time))
        }
    }

    fun scheduleDose(medicineId: Int, timeMinutes: Int, triggerMillis: Long) {
        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            putExtra("medicineId", medicineId)
            putExtra("timeMinutes", timeMinutes)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(medicineId, timeMinutes),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pending,
        )
    }

    fun cancelMedicine(medicine: Medicine) {
        medicine.times.forEach { time ->
            val intent = Intent(context, DoseAlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode(medicine.id, time),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            pending?.let { alarmManager.cancel(it); it.cancel() }
        }
    }

    fun rescheduleAll(medicines: List<Medicine>) {
        medicines.filter { it.active }.forEach { scheduleMedicine(it) }
    }

    /** Next occurrence of a wall-clock time (today if still ahead). */
    fun nextTriggerMillis(timeMinutes: Int): Long {
        val now = LocalDateTime.now()
        var target = LocalDate.now().atTime(timeMinutes / 60, timeMinutes % 60)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
