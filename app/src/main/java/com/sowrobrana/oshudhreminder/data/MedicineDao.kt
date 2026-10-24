package com.sowrobrana.oshudhreminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY createdAt ASC")
    fun allMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE active = 1 ORDER BY createdAt ASC")
    suspend fun activeMedicines(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun medicineById(id: Int): Medicine?

    @Insert
    suspend fun insertMedicine(medicine: Medicine): Long

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("SELECT * FROM dose_logs WHERE dateEpochDay = :day ORDER BY timeMinutes ASC")
    fun logsForDay(day: Long): Flow<List<DoseLog>>

    @Query(
        "SELECT * FROM dose_logs WHERE medicineId = :medicineId " +
            "AND dateEpochDay = :day AND timeMinutes = :time LIMIT 1",
    )
    suspend fun logFor(medicineId: Int, day: Long, time: Int): DoseLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DoseLog)

    @Query("SELECT * FROM dose_logs ORDER BY dateEpochDay DESC, timeMinutes DESC LIMIT 200")
    fun recentLogs(): Flow<List<DoseLog>>
}
