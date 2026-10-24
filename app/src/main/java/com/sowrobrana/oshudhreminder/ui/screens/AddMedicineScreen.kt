package com.sowrobrana.oshudhreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sowrobrana.oshudhreminder.data.Medicine
import com.sowrobrana.oshudhreminder.ui.MedicineViewModel
import com.sowrobrana.oshudhreminder.util.formatTimeBn
import java.time.LocalDate

/** Add-medicine form: name, dose, times, food timing, duration, stock. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicineScreen(vm: MedicineViewModel, onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var times by remember { mutableStateOf(setOf<Int>()) }
    var beforeFood by remember { mutableStateOf(true) }
    var durationDays by remember { mutableStateOf(0) }
    var trackStock by remember { mutableStateOf(false) }
    var stockText by remember { mutableStateOf("30") }
    var showPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val presets = listOf(480 to "সকাল ৮:০০", 840 to "দুপুর ২:০০", 1260 to "রাত ৯:০০")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "নতুন ওষুধ যোগ করুন",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("ওষুধের নাম") },
            placeholder = { Text("যেমন: নাপা") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = dose,
            onValueChange = { dose = it },
            label = { Text("ডোজ") },
            placeholder = { Text("যেমন: ১টি ট্যাবলেট") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Text("কখন খাবেন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { (minutes, label) ->
                FilterChip(
                    selected = minutes in times,
                    onClick = {
                        times = if (minutes in times) times - minutes else times + minutes
                    },
                    label = { Text(label) },
                )
            }
            FilterChip(
                selected = false,
                onClick = { showPicker = true },
                label = { Text("+ অন্য সময়") },
            )
        }
        if (times.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                times.sorted().forEach { minutes ->
                    AssistChip(
                        onClick = { times = times - minutes },
                        label = { Text(formatTimeBn(minutes)) },
                        trailingIcon = {
                            Icon(Icons.Filled.Close, contentDescription = "সরান")
                        },
                    )
                }
            }
        }

        Text("খাবারের আগে না পরে", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = beforeFood,
                onClick = { beforeFood = true },
                label = { Text("খাবারের আগে") },
            )
            FilterChip(
                selected = !beforeFood,
                onClick = { beforeFood = false },
                label = { Text("খাবারের পরে") },
            )
        }

        Text("কতদিন চলবে", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0 to "চলমান", 7 to "৭ দিন", 14 to "১৪ দিন", 30 to "৩০ দিন").forEach { (days, label) ->
                FilterChip(
                    selected = durationDays == days,
                    onClick = { durationDays = days },
                    label = { Text(label) },
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "স্টকের হিসাব রাখবো",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = trackStock, onCheckedChange = { trackStock = it })
        }
        if (trackStock) {
            OutlinedTextField(
                value = stockText,
                onValueChange = { stockText = it.filter { c -> c.isDigit() } },
                label = { Text("এখন কতটি আছে") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "ওষুধের নাম দিন।"
                    times.isEmpty() -> error = "অন্তত একটি সময় বাছুন।"
                    else -> {
                        val stock = if (trackStock) stockText.toIntOrNull() ?: 0 else -1
                        vm.addMedicine(
                            Medicine(
                                name = name.trim(),
                                dose = dose.trim().ifBlank { "১টি" },
                                times = times.sorted(),
                                beforeFood = beforeFood,
                                durationDays = durationDays,
                                startEpochDay = LocalDate.now().toEpochDay(),
                                stock = stock,
                            ),
                        )
                        onDone()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("সংরক্ষণ করুন")
        }
    }

    if (showPicker) {
        val pickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("সময় বাছুন") },
            text = {
                TimePicker(state = pickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val minutes = pickerState.hour * 60 + pickerState.minute
                        times = times + minutes
                        showPicker = false
                    },
                ) { Text("ঠিক আছে") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("বাতিল") }
            },
        )
    }
}
