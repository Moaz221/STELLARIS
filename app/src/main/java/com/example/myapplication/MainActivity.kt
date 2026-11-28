package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.launch
import com.example.myapplication.presentation.navigation.StellarisNavGraph

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.myapplication.data.local.AppStore.init(applicationContext)
        enableEdgeToEdge()
        kotlinx.coroutines.MainScope().launch {
            val meds = com.example.myapplication.data.local.AppStore.load("care_medications", com.example.myapplication.core.model.Medication.serializer())
            com.example.myapplication.core.reminders.ReminderScheduler.rescheduleAll(applicationContext, meds)
        }
        setContent {
            StellarisNavGraph()
        }
    }
}

