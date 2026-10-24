package com.sowrobrana.oshudhreminder.alarms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sowrobrana.oshudhreminder.MainActivity
import com.sowrobrana.oshudhreminder.data.Medicine
import com.sowrobrana.oshudhreminder.util.formatTimeBn

object NotificationHelper {
    const val CHANNEL_ID = "dose_channel"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ওষুধের রিমাইন্ডার",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "ওষুধ খাওয়ার সময় হলে জানিয়ে দেবে" }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showDoseNotification(context: Context, medicine: Medicine, timeMinutes: Int) {
        createChannel(context)
        val scheduler = AlarmScheduler(context)
        val takenIntent = Intent(context, DoseTakenReceiver::class.java).apply {
            putExtra("medicineId", medicine.id)
            putExtra("timeMinutes", timeMinutes)
            putExtra("dateEpochDay", java.time.LocalDate.now().toEpochDay())
        }
        val takenPending = PendingIntent.getBroadcast(
            context,
            scheduler.requestCode(medicine.id, timeMinutes),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val openIntent = Intent(context, MainActivity::class.java)
        val openPending = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val food = if (medicine.beforeFood) "খাবারের আগে" else "খাবারের পরে"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("ওষুধ খাওয়ার সময়")
            .setContentText("${medicine.name} — ${medicine.dose} • ${formatTimeBn(timeMinutes)} • $food")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPending)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "খেয়েছি",
                takenPending,
            )
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        manager.notify(
            scheduler.requestCode(medicine.id, timeMinutes),
            notification,
        )
    }

    fun cancelNotification(context: Context, id: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        manager.cancel(id)
    }
}
