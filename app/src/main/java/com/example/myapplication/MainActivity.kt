package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.myapplication.presentation.navigation.StellarisNavGraph

/** Set by widgets: which overlay to open when the app comes to the front. */
object LaunchTarget {
    var overlay by androidx.compose.runtime.mutableStateOf<String?>(null)
    var extra by androidx.compose.runtime.mutableStateOf<String?>(null)
}

class MainActivity : AppCompatActivity() {
    private fun readTarget(i: android.content.Intent?) {
        val o = i?.getStringExtra("overlay") ?: return
        LaunchTarget.extra = i.getStringExtra("extra")
        LaunchTarget.overlay = o
        i.removeExtra("overlay")
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readTarget(intent)
    }

    override fun onStop() {
        super.onStop()
        kotlinx.coroutines.MainScope().launch { com.example.myapplication.widgets.updateAllWidgets(applicationContext) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.myapplication.data.local.AppStore.init(applicationContext)
        enableEdgeToEdge()
        readTarget(intent)
        kotlinx.coroutines.MainScope().launch {
            val meds = com.example.myapplication.data.local.AppStore.load("care_medications", com.example.myapplication.core.model.Medication.serializer())
            com.example.myapplication.core.reminders.ReminderScheduler.rescheduleAll(applicationContext, meds)
        }
        setContent {
            StellarisNavGraph()
        }
    }
}

