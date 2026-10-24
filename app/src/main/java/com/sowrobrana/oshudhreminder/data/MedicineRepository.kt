package com.sowrobrana.oshudhreminder.data

import com.sowrobrana.oshudhreminder.alarms.AlarmScheduler
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/**
 * Single place for medicine + dose-log operations. Every write also
 * (re)schedules alarms so the schedule and the alarms never drift apart.
 */
class MedicineRepository(
    private val dao: MedicineDao,
    private val scheduler: AlarmScheduler,
) {
    fun allMedicines(): Flow<List<Medicine>> = dao.allMedicines()

    fun logsForDay(day: Long): Flow<List<DoseLog>> = dao.logsForDay(day)

    fun recentLogs(): Flow<List<DoseLog>> = dao.recentLogs()

    suspend fun addMedicine(medicine: Medicine) {
        val id = dao.insertMedicine(medicine).toInt()
        val saved = medicine.copy(id = id)
        if (saved.active) scheduler.scheduleMedicine(saved)
    }

    suspend fun updateMedicine(medicine: Medicine) {
        dao.updateMedicine(medicine)
        scheduler.cancelMedicine(medicine)
        if (medicine.active) scheduler.scheduleMedicine(medicine)
    }

    suspend fun deleteMedicine(medicine: Medicine) {
        scheduler.cancelMedicine(medicine)
        dao.deleteMedicine(medicine)
    }

    suspend fun setActive(medicine: Medicine, active: Boolean) {
        val updated = medicine.copy(active = active)
        dao.updateMedicine(updated)
        scheduler.cancelMedicine(medicine)
        if (active) scheduler.scheduleMedicine(updated)
    }

    /** Mark a dose taken: log it and decrement tracked stock by one. */
    suspend fun markTaken(medicine: Medicine, day: Long, timeMinutes: Int) {
        dao.upsertLog(
            DoseLog(
                medicineId = medicine.id,
                dateEpochDay = day,
                timeMinutes = timeMinutes,
                status = "TAKEN",
            ),
        )
        if (medicine.stock > 0) {
            dao.updateMedicine(medicine.copy(stock = medicine.stock - 1))
        }
    }

    suspend fun markSkipped(medicine: Medicine, day: Long, timeMinutes: Int) {
        dao.upsertLog(
            DoseLog(
                medicineId = medicine.id,
                dateEpochDay = day,
                timeMinutes = timeMinutes,
                status = "SKIPPED",
            ),
        )
    }

    /** Is this medicine scheduled on this date (duration window)? */
    fun isDueOn(medicine: Medicine, day: Long): Boolean {
        if (!medicine.active) return false
        if (day < medicine.startEpochDay) return false
        if (medicine.durationDays > 0) {
            return day < medicine.startEpochDay + medicine.durationDays
        }
        return true
    }

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}
