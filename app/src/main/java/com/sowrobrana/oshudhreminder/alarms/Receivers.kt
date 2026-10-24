package com.sowrobrana.oshudhreminder.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sowrobrana.oshudhreminder.data.AppDatabase
import com.sowrobrana.oshudhreminder.data.DoseLog
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Fires at a dose time: notify, then schedule tomorrow's occurrence. */
class DoseAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val medicineId = intent.getIntExtra("medicineId", -1)
        val timeMinutes = intent.getIntExtra("timeMinutes", -1)
        if (medicineId < 0 || timeMinutes < 0) {
            pending.finish()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).medicineDao()
                val medicine = dao.medicineById(medicineId)
                val today = LocalDate.now().toEpochDay()
                if (medicine != null && medicine.active) {
                    val dueToday = today >= medicine.startEpochDay &&
                        (medicine.durationDays <= 0 ||
                            today < medicine.startEpochDay + medicine.durationDays)
                    if (dueToday) {
                        NotificationHelper.showDoseNotification(context, medicine, timeMinutes)
                    }
                    // Next day, unless a finite course has ended.
                    val tomorrow = today + 1
                    val courseContinues = medicine.durationDays <= 0 ||
                        tomorrow < medicine.startEpochDay + medicine.durationDays
                    if (courseContinues) {
                        val scheduler = AlarmScheduler(context)
                        val trigger = java.time.LocalDateTime.now()
                            .plusDays(1)
                            .withHour(timeMinutes / 60)
                            .withMinute(timeMinutes % 60)
                            .withSecond(0)
                            .withNano(0)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toInstant().toEpochMilli()
                        scheduler.scheduleDose(medicineId, timeMinutes, trigger)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}

/** Notification action "খেয়েছি": log the dose, decrement stock. */
class DoseTakenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val medicineId = intent.getIntExtra("medicineId", -1)
        val timeMinutes = intent.getIntExtra("timeMinutes", -1)
        val day = intent.getLongExtra("dateEpochDay", LocalDate.now().toEpochDay())
        if (medicineId < 0 || timeMinutes < 0) {
            pending.finish()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).medicineDao()
                val medicine = dao.medicineById(medicineId)
                if (medicine != null) {
                    dao.upsertLog(
                        DoseLog(
                            medicineId = medicineId,
                            dateEpochDay = day,
                            timeMinutes = timeMinutes,
                            status = "TAKEN",
                        ),
                    )
                    if (medicine.stock > 0) {
                        dao.updateMedicine(medicine.copy(stock = medicine.stock - 1))
                    }
                }
                NotificationHelper.cancelNotification(
                    context,
                    AlarmScheduler(context).requestCode(medicineId, timeMinutes),
                )
            } finally {
                pending.finish()
            }
        }
    }
}

/** After a reboot, rebuild every active medicine's alarm chain. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.get(context).medicineDao()
                val medicines = dao.activeMedicines()
                AlarmScheduler(context).rescheduleAll(medicines)
            } finally {
                pending.finish()
            }
        }
    }
}
