package com.sowrobrana.oshudhreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sowrobrana.oshudhreminder.ui.MedicineViewModel
import com.sowrobrana.oshudhreminder.util.formatTimeBn
import java.time.LocalDate

private val BN_MONTHS = arrayOf(
    "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
    "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর",
)

private fun dateLabel(epochDay: Long): String {
    val today = LocalDate.now().toEpochDay()
    val date = LocalDate.ofEpochDay(epochDay)
    return when (epochDay) {
        today -> "আজ"
        today - 1 -> "গতকাল"
        else -> "${date.dayOfMonth} ${BN_MONTHS[date.monthValue - 1]}, ${date.year}"
    }
}

/** Recent dose history, grouped by day, newest first. */
@Composable
fun HistoryScreen(vm: MedicineViewModel) {
    val logs by vm.recentLogs.collectAsState()
    val medicines by vm.medicines.collectAsState()
    val nameById = medicines.associate { it.id to it.name }

    if (logs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("এখনো কোনো ইতিহাস নেই", style = MaterialTheme.typography.titleMedium)
            Text(
                "ডোজ খেলে বা বাদ দিলে এখানে দেখা যাবে।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val grouped = logs.groupBy { it.dateEpochDay }.toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        grouped.forEach { (day, dayLogs) ->
            item {
                Text(
                    dateLabel(day),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            items(dayLogs, key = { it.id }) { log ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                nameById[log.medicineId] ?: "ওষুধ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                formatTimeBn(log.timeMinutes),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            if (log.status == "TAKEN") "খেয়েছেন ✓" else "বাদ দিয়েছেন",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (log.status == "TAKEN") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }
}
