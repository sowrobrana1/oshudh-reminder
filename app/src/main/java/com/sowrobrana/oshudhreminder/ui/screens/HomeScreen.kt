package com.sowrobrana.oshudhreminder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sowrobrana.oshudhreminder.ui.DoseItem
import com.sowrobrana.oshudhreminder.ui.DoseStatus
import com.sowrobrana.oshudhreminder.ui.MedicineViewModel
import com.sowrobrana.oshudhreminder.util.bnDigits
import com.sowrobrana.oshudhreminder.util.formatTimeBn

/**
 * Home: today's dose timeline under an animated progress header.
 * The header gradient slowly shifts, the ring springs to the live
 * taken/total ratio, and cards stagger in — the "full animation" feel.
 */
@Composable
fun HomeScreen(vm: MedicineViewModel, onRequestExactAlarm: () -> Unit) {
    val doses by vm.todayDoses.collectAsState()
    val medicines by vm.medicines.collectAsState()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val taken = doses.count { it.status == DoseStatus.TAKEN }
    val total = doses.size
    val progress = if (total == 0) 0f else taken.toFloat() / total
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress",
    )

    // Slowly shifting header gradient.
    val infinite = rememberInfiniteTransition(label = "header")
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(6000)),
        label = "shift",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F766E),
                                Color(0xFF14B8A6),
                                Color(0xFF0F766E),
                            ),
                            start = Offset(shift * 600f, 0f),
                            end = Offset(600f + shift * 600f, 400f),
                        ),
                    )
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "আজকের ডোজ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (total == 0) {
                                "আজকের জন্য কোনো ডোজ নেই"
                            } else {
                                "${bnDigits(taken)} / ${bnDigits(total)} ডোজ খাওয়া হয়েছে"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                    ProgressRing(progress = animatedProgress, taken = taken, total = total)
                }
            }
        }

        if (!vm.canScheduleExactAlarms()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "সঠিক সময়ে অ্যালার্মের অনুমতি দিন",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "না দিলে রিমাইন্ডার দেরিতে আসতে পারে।",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Button(onClick = onRequestExactAlarm) { Text("অনুমতি দিন") }
                    }
                }
            }
        }

        val lowStock = medicines.filter { it.stock >= 0 && it.stock <= it.stockAlertAt }
        if (lowStock.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "স্টক শেষের দিকে: " +
                                lowStock.joinToString(", ") {
                                    "${it.name} (${bnDigits(it.stock)}টি বাকি)"
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        if (doses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "এখনো কোনো ওষুধ যোগ করা হয়নি",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "নিচের + বাটন চেপে প্রথম ওষুধ যোগ করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            itemsIndexed(doses, key = { _, item -> "${item.medicine.id}-${item.timeMinutes}" }) { index, item ->
                AnimatedVisibility(
                    visible = entered,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = index * 70)) +
                        slideInVertically(
                            animationSpec = tween(400, delayMillis = index * 70),
                        ) { it / 2 },
                ) {
                    DoseCard(item = item, onTaken = { vm.markTaken(item) }, onSkipped = { vm.markSkipped(item) })
                }
            }
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, taken: Int, total: Int) {
    Box(
        modifier = Modifier.size(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val stroke = 12.dp.toPx()
            drawArc(
                color = Color.White.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (total == 0) "—" else "${bnDigits(taken)}/${bnDigits(total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                "ডোজ",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun DoseCard(
    item: DoseItem,
    onTaken: () -> Unit,
    onSkipped: () -> Unit,
) {
    val med = item.medicine
    val (chipText, chipColor) = when (item.status) {
        DoseStatus.TAKEN -> "খাওয়া হয়েছে" to Color(0xFF15803D)
        DoseStatus.SKIPPED -> "বাদ দেওয়া" to Color(0xFF64748B)
        DoseStatus.MISSED -> "মিস হয়েছে" to Color(0xFFDC2626)
        DoseStatus.PENDING -> "বাকি আছে" to Color(0xFFD97706)
    }
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.status == DoseStatus.TAKEN) {
                Color(0xFFF0FDF4)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        "${med.dose} • ${formatTimeBn(item.timeMinutes)} • " +
                            if (med.beforeFood) "খাবারের আগে" else "খাবারের পরে",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    chipText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = chipColor,
                )
            }
            if (item.status == DoseStatus.PENDING || item.status == DoseStatus.MISSED) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTaken, modifier = Modifier.weight(1f)) {
                        Text("খেয়েছি ✓")
                    }
                    OutlinedButton(onClick = onSkipped) {
                        Text("বাদ দিন")
                    }
                }
            } else if (item.status == DoseStatus.TAKEN) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        "দারুণ! এই ডোজ সম্পন্ন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF15803D),
                    )
                }
            }
        }
    }
}
