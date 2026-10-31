package com.example.myapplication.presentation.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.presentation.components.LungCareButton
import com.example.myapplication.presentation.components.LungCareCard
import com.example.myapplication.presentation.components.LungCareNotice
import com.example.myapplication.presentation.components.LungCareTag

@Composable
fun DesignSystemPreviewScreen(
    onBack: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column {
                    Text(
                        text = "Design System Showcase",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "LungCare AI Visual Tokens & Components",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Color Tokens Showcase
        item {
            Text(
                text = "Color Palette",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorBox("Primary", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                ColorBox("Secondary", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                ColorBox("Surface", MaterialTheme.colorScheme.surface, Modifier.weight(1f))
                ColorBox("Error", MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }

        // Buttons Showcase
        item {
            Text(
                text = "Buttons & Interactive Elements",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            LungCareButton(
                text = "Primary Button Action",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))
            LungCareButton(
                text = "Secondary Button Action",
                onClick = {},
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Tags Showcase
        item {
            Text(
                text = "Tags & Badges",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LungCareTag(text = "CLINICIAN CONFIRMED")
                LungCareTag(text = "DEMO", containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.error)
                LungCareTag(text = "PATIENT ENTERED", containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Card Showcase
        item {
            Text(
                text = "Cards & Elevation Surfaces",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            LungCareCard {
                Text(
                    text = "Standard Surface Card",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "16dp rounded corners, restrained subtle border, tonal background.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Notice Showcase
        item {
            Text(
                text = "Notices & Safety Alerts",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            LungCareNotice(
                title = "Informational Notice",
                message = "Used for general guidelines, screening eligibility criteria, or system updates.",
                isUrgent = false
            )
            Spacer(modifier = Modifier.height(8.dp))
            LungCareNotice(
                title = "Urgent Medical Alert",
                message = "Used for symptoms requiring immediate clinical or emergency intervention.",
                isUrgent = true
            )
        }
    }
}

@Composable
private fun ColorBox(
    name: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(color, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (color == MaterialTheme.colorScheme.surface) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        )
    }
}
