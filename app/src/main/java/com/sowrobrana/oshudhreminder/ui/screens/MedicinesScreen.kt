package com.sowrobrana.oshudhreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sowrobrana.oshudhreminder.data.Medicine
import com.sowrobrana.oshudhreminder.ui.MedicineViewModel
import com.sowrobrana.oshudhreminder.util.bnDigits
import com.sowrobrana.oshudhreminder.util.formatTimeBn

/** All medicines: toggle on/off, delete with confirmation. */
@Composable
fun MedicinesScreen(vm: MedicineViewModel) {
    val medicines by vm.medicines.collectAsState()
    var toDelete by remember { mutableStateOf<Medicine?>(null) }

    if (medicines.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("কোনো ওষুধ নেই", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "+ বাটন চেপে ওষুধ যোগ করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(medicines, key = { it.id }) { med ->
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.animateItem(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                med.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                med.dose,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = med.active,
                            onCheckedChange = { vm.setActive(med, it) },
                        )
                        IconButton(onClick = { toDelete = med }) {
                            Icon(Icons.Filled.Delete, contentDescription = "মুছুন")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "সময়: " + med.times.sorted().joinToString(", ") { formatTimeBn(it) },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        (if (med.beforeFood) "খাবারের আগে" else "খাবারের পরে") +
                            if (med.durationDays > 0) {
                                " • ${bnDigits(med.durationDays)} দিনের কোর্স"
                            } else {
                                " • চলমান"
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (med.stock >= 0) {
                        Text(
                            "স্টক: ${bnDigits(med.stock)}টি বাকি",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (med.stock <= med.stockAlertAt) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("মুছবেন?") },
            text = { Text("${med.name} ও এর সব রিমাইন্ডার মুছে যাবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteMedicine(med)
                        toDelete = null
                    },
                ) { Text("মুছুন") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("না") }
            },
        )
    }
}
