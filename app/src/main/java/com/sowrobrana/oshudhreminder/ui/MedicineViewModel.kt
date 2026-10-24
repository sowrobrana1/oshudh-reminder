package com.sowrobrana.oshudhreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sowrobrana.oshudhreminder.alarms.AlarmScheduler
import com.sowrobrana.oshudhreminder.data.AppDatabase
import com.sowrobrana.oshudhreminder.data.DoseLog
import com.sowrobrana.oshudhreminder.data.Medicine
import com.sowrobrana.oshudhreminder.data.MedicineRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DoseStatus { PENDING, TAKEN, SKIPPED, MISSED }

data class DoseItem(
    val medicine: Medicine,
    val timeMinutes: Int,
    val status: DoseStatus,
)

class MedicineViewModel(application: Application) : AndroidViewModel(application) {
    private val scheduler = AlarmScheduler(application)
    private val repository: MedicineRepository

    init {
        val dao = AppDatabase.get(application).medicineDao()
        repository = MedicineRepository(dao, scheduler)
    }

    val medicines: StateFlow<List<Medicine>> = repository.allMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<DoseLog>> = repository.recentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val today = repository.todayEpochDay()

    /** Today's timeline: every due dose, with its live status. */
    val todayDoses: StateFlow<List<DoseItem>> =
        combine(repository.allMedicines(), repository.logsForDay(today)) { meds, logs ->
            val logByKey = logs.associateBy { it.medicineId to it.timeMinutes }
            val nowMinutes = LocalTime.now().hour * 60 + LocalTime.now().minute
            meds.filter { repository.isDueOn(it, today) }
                .flatMap { med -> med.times.map { time -> med to time } }
                .map { (med, time) ->
                    val log = logByKey[med.id to time]
                    val status = when (log?.status) {
                        "TAKEN" -> DoseStatus.TAKEN
                        "SKIPPED" -> DoseStatus.SKIPPED
                        null -> if (time < nowMinutes - 30) {
                            DoseStatus.MISSED
                        } else {
                            DoseStatus.PENDING
                        }
                        else -> DoseStatus.PENDING
                    }
                    DoseItem(med, time, status)
                }
                .sortedBy { it.timeMinutes }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun canScheduleExactAlarms(): Boolean = scheduler.canScheduleExactAlarms()

    fun addMedicine(medicine: Medicine) {
        viewModelScope.launch { repository.addMedicine(medicine) }
    }

    fun deleteMedicine(medicine: Medicine) {
        viewModelScope.launch { repository.deleteMedicine(medicine) }
    }

    fun setActive(medicine: Medicine, active: Boolean) {
        viewModelScope.launch { repository.setActive(medicine, active) }
    }

    fun markTaken(item: DoseItem) {
        viewModelScope.launch { repository.markTaken(item.medicine, today, item.timeMinutes) }
    }

    fun markSkipped(item: DoseItem) {
        viewModelScope.launch { repository.markSkipped(item.medicine, today, item.timeMinutes) }
    }
}

class MedicineViewModelFactory(
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MedicineViewModel(application) as T
    }
}
