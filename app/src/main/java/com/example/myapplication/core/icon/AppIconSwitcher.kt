package com.example.myapplication.core.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/** Switches the launcher icon to match the health-journey status by enabling exactly one activity-alias. */
object AppIconSwitcher {
    private val aliases = mapOf(
        "EARLY_DETECTION" to ".IconEarly",
        "UNDER_EVALUATION" to ".IconEvaluation",
        "DIAGNOSED" to ".IconDiagnosed"
    )
    private const val DEFAULT = ".IconDefault"

    fun apply(context: Context, journey: String?) {
        try {
            val target = aliases[journey] ?: DEFAULT
            val pm = context.packageManager
            val all = aliases.values + DEFAULT
            // Enable the new alias first, then disable the others, so a launcher entry always exists.
            val order = listOf(target) + all.filter { it != target }
            order.forEach { name ->
                val cn = ComponentName(context.packageName, context.packageName.let { "com.example.myapplication" } + name)
                val state = if (name == target) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                if (pm.getComponentEnabledSetting(cn) != state) {
                    pm.setComponentEnabledSetting(cn, state, PackageManager.DONT_KILL_APP)
                }
            }
        } catch (_: Exception) {
        }
    }
}
