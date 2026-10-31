package com.example.myapplication.presentation.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.myapplication.presentation.components.StarfieldBackground
import com.example.myapplication.presentation.theme.DeepSpace
import com.example.myapplication.presentation.theme.Navy
import com.example.myapplication.presentation.theme.StellarTeal

@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepSpace, Navy, DeepSpace)))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(StellarTeal.copy(alpha = 0.18f), Color.Transparent),
                        radius = 900f
                    )
                )
        )
        StarfieldBackground(starCount = 50)
        content()
    }
}
