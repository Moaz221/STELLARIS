package com.example.myapplication.presentation.symptoms

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.design.lungCarePalette
import com.example.myapplication.core.ui.*
import com.example.myapplication.data.local.AppStore

@kotlinx.serialization.Serializable
data class EmergencyContact(val id: String, val name: String, val number: String)

private fun dial(context: Context, number: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(number))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, locNow("No phone app available", "مفيش تطبيق اتصال متاح"), Toast.LENGTH_SHORT).show()
    }
}

/** Opens the dialer only (no call permission). Egypt ambulance 123 is always offered; users add their own numbers. */
@Composable
fun EmergencyCallDialog(onClose: () -> Unit) {
    val p = lungCarePalette()
    val context = LocalContext.current
    var contacts by remember { mutableStateOf(emptyList<EmergencyContact>()) }
    var loaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { contacts = AppStore.load("emergency_contacts", EmergencyContact.serializer()); loaded = true }
    LaunchedEffect(contacts, loaded) { if (loaded) AppStore.save("emergency_contacts", EmergencyContact.serializer(), contacts) }

    FullScreenForm(title = loc("Emergency call", "اتصال طوارئ"), onClose = onClose) {
        Text(loc("Tap a number to open the phone dialer. Nothing is dialed until you press call.", "اضغط على رقم لفتح تطبيق الاتصال. مفيش اتصال بيتم إلا لما تضغط اتصال."),
            fontSize = 13.sp, color = p.textSecondary)
        NumberRow(loc("Ambulance (Egypt)", "الإسعاف (مصر)"), "123", null) { dial(context, "123") }
        if (contacts.isEmpty()) {
            Text(loc("No personal numbers saved yet. Add your doctor or a family member below.", "لسه مفيش أرقام شخصية. ضيف رقم دكتورك أو حد من أهلك تحت."),
                fontSize = 14.sp, color = p.textSecondary)
        }
        contacts.forEach { c ->
            NumberRow(c.name, c.number, { contacts = contacts.filter { it.id != c.id } }) { dial(context, c.number) }
        }
        HorizontalDivider(color = p.border)
        Text(loc("Add a number", "إضافة رقم"), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = p.textPrimary)
        AppField(name, { name = it }, singleLine = true, label = loc("Name (e.g. Dr. Ahmed)", "الاسم (مثلا د. أحمد)"))
        AppField(number, { number = it.filter { ch -> ch.isDigit() || ch == '+' }.take(16) }, singleLine = true,
            label = loc("Phone number", "رقم التليفون"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        AppPrimaryButton(loc("Save number", "حفظ الرقم"), enabled = name.isNotBlank() && number.length >= 3, onClick = {
            contacts = contacts + EmergencyContact(System.currentTimeMillis().toString(), name.trim(), number)
            name = ""; number = ""
        })
        AppSecondaryButton(loc("Close", "إغلاق"), onClose)
    }
}

@Composable
private fun NumberRow(title: String, number: String, onDelete: (() -> Unit)?, onCall: () -> Unit) {
    val p = lungCarePalette()
    Surface(shape = RoundedCornerShape(16.dp), color = p.surface, border = BorderStroke(1.dp, p.border), onClick = onCall) {
        Row(Modifier.padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = p.textPrimary)
                Text(number, fontSize = 14.sp, color = p.textSecondary)
            }
            if (onDelete != null) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = loc("Delete", "حذف"), tint = p.urgent) }
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = p.urgent, modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Call, contentDescription = loc("Call", "اتصال"), tint = androidx.compose.ui.graphics.Color.White) }
            }
        }
    }
}
