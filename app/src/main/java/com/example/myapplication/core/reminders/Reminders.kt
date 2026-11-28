package com.example.myapplication.core.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.core.model.Medication
import com.example.myapplication.data.local.AppStore
import java.util.Calendar
import com.example.myapplication.core.ui.locNow

/** Daily medication reminders. Uses inexact alarms, so no special alarm permission is needed. */
object ReminderScheduler {
    private const val PREFS = "lungcare_reminders"
    private const val KEY = "codes"
    const val CHANNEL = "medication_reminders"

    private fun pending(context: Context, code: Int, name: String, text: String): PendingIntent {
        val i = Intent(context, ReminderReceiver::class.java)
            .putExtra("code", code).putExtra("name", name).putExtra("text", text)
        return PendingIntent.getBroadcast(context, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun nextTrigger(time: String): Long? {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
        val m = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (c.timeInMillis <= System.currentTimeMillis() + 5_000) c.add(Calendar.DAY_OF_YEAR, 1)
        return c.timeInMillis
    }

    fun scheduleOne(context: Context, code: Int, name: String, text: String, time: String) {
        val at = nextTrigger(time) ?: return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(context, code, name, text).also { })
        // keep the original time so the receiver can re-arm for the next day
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("time_$code", time).apply()
    }

    fun rescheduleAll(context: Context, meds: List<Medication>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        prefs.getStringSet(KEY, emptySet())?.forEach { c ->
            val code = c.toIntOrNull() ?: return@forEach
            am.cancel(pending(context, code, "", ""))
        }
        val codes = mutableSetOf<String>()
        meds.filter { it.isActive }.forEach { med ->
            med.reminderTimes.forEach { t ->
                val code = (med.id + t).hashCode()
                scheduleOne(context, code, med.name, med.instructions, t)
                codes.add(code.toString())
            }
        }
        prefs.edit().putStringSet(KEY, codes).apply()
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val code = intent.getIntExtra("code", 0)
        val name = intent.getStringExtra("name").orEmpty()
        val text = intent.getStringExtra("text").orEmpty()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel(ReminderScheduler.CHANNEL, "Medication reminders", NotificationManager.IMPORTANCE_HIGH))
        }
        if (ReminderScheduler.canNotify(context)) {
            val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val n = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(name.ifBlank { locNow("Medication reminder", "تنبيه الدوا") })
                .setContentText(text.ifBlank { locNow("Time to take your medication", "جه ميعاد الدوا") })
                .setContentIntent(open).setAutoCancel(true).build()
            NotificationManagerCompat.from(context).notify(code, n)
        }
        // re-arm for tomorrow
        val time = context.getSharedPreferences("lungcare_reminders", Context.MODE_PRIVATE).getString("time_$code", null)
        if (time != null) ReminderScheduler.scheduleOne(context, code, name, text, time)
    }
}
