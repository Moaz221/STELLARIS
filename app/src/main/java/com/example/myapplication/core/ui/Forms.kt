package com.example.myapplication.core.ui

import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.R
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.example.myapplication.core.design.lungCarePalette

/** Strong-contrast text field used by every add/edit form. */
@Composable
fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val p = lungCarePalette()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = p.textPrimary,
            unfocusedTextColor = p.textPrimary,
            focusedContainerColor = p.surface,
            unfocusedContainerColor = p.surface,
            focusedBorderColor = p.primary,
            unfocusedBorderColor = p.textSecondary,
            focusedLabelColor = p.primary,
            unfocusedLabelColor = p.textSecondary,
            cursorColor = p.primary
        )
    )
}

@Composable
fun AppPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val p = lungCarePalette()
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = p.primary,
            contentColor = p.onPrimary,
            disabledContainerColor = p.primary.copy(alpha = 0.35f),
            disabledContentColor = p.onPrimary.copy(alpha = 0.7f)
        )
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
}

@Composable
fun AppSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = lungCarePalette()
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, p.primary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = p.primary)
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
}

/** Full-screen page with a title bar and close button, in the app palette. */
@Composable
fun FullScreenForm(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val p = lungCarePalette()
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = p.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = loc("Close", "إغلاق"), tint = p.textPrimary)
                    }
                    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.textPrimary)
                }
                HorizontalDivider(color = p.border)
                Column(
                    Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = content
                )
            }
        }
    }
}

@Composable
fun appFieldColors(): TextFieldColors {
    val p = lungCarePalette()
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = p.textPrimary, unfocusedTextColor = p.textPrimary,
        focusedContainerColor = p.surface, unfocusedContainerColor = p.surface,
        focusedBorderColor = p.primary, unfocusedBorderColor = p.textSecondary,
        focusedLabelColor = p.primary, unfocusedLabelColor = p.textSecondary, cursorColor = p.primary
    )
}

/** Animated "Share with assistant" pill with the looping robot animation. Sends nothing by itself. */
@Composable
fun ShareWithAssistantButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = lungCarePalette()
    val comp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.robot_wave))
    val shape = RoundedCornerShape(30.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(p.primary, p.info)))
            .border(1.5.dp, p.onPrimary.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 18.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LottieAnimation(composition = comp, iterations = LottieConstants.IterateForever, modifier = Modifier.size(44.dp))
        Spacer(Modifier.width(4.dp))
        Text(loc("Share with assistant", "شارك مع المساعد"), color = p.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
