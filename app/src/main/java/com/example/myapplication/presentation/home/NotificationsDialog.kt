package com.example.myapplication.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.design.lungCarePalette
import com.example.myapplication.core.model.AppointmentStatus
import com.example.myapplication.core.ui.*
import com.example.myapplication.data.care.LocalCareRepository

private data class NotifItem(val title: String, val detail: String)

/** Lists medication reminders, upcoming appointments and open follow-ups saved in the app. */
@Composable
internal fun NotificationsDialog(onClose: () -> Unit) {
    val p = lungCarePalette()
    var state by remember { mutableStateOf<com.example.myapplication.core.model.CareJourneyState?>(null) }
    LaunchedEffect(Unit) { state = LocalCareRepository().getCareJourney() }
    val items: List<NotifItem>? = state?.let { st ->
        val list = mutableListOf<NotifItem>()
        val medL = loc("Medication: ", "دواء: "); val atL = loc("Reminder at ", "تنبيه الساعة ")
        val apL = loc("Appointment: ", "موعد: "); val fuL = loc("Follow-up: ", "متابعة: ")
        st.medications.filter { it.isActive && it.reminderTimes.isNotEmpty() }.forEach { list += NotifItem(medL + it.name, atL + it.reminderTimes.joinToString(", ")) }
        st.appointments.filter { it.status == AppointmentStatus.UPCOMING }.forEach { list += NotifItem(apL + it.title, it.dateTime) }
        st.milestones.filter { !it.isCompleted }.forEach { list += NotifItem(fuL + it.title, it.dueDate ?: "") }
        list
    }
    FullScreenForm(title = loc("Notifications", "الإشعارات"), onClose = onClose) {
        val l = items
        if (l == null) {
            CircularProgressIndicator(color = p.primary)
        } else if (l.isEmpty()) {
            Text(loc("No notifications yet", "مفيش إشعارات لسه"), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = p.textPrimary)
            Text(loc("Medication reminders, upcoming appointments and follow-ups you add in My Care Journey will show up here.",
                "تنبيهات الأدوية والمواعيد القادمة والمتابعات اللي بتضيفها في رحلتي العلاجية هتظهر هنا."), fontSize = 14.sp, color = p.textSecondary)
        } else {
            l.forEach {
                Surface(shape = RoundedCornerShape(16.dp), color = p.surface, border = BorderStroke(1.dp, p.border)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(it.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = p.textPrimary)
                        if (it.detail.isNotBlank()) Text(it.detail, fontSize = 13.sp, color = p.textSecondary)
                    }
                }
            }
        }
        AppSecondaryButton(loc("Close", "إغلاق"), onClose)
    }
}
