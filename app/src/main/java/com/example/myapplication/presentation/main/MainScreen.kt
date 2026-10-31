package com.example.myapplication.presentation.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.presentation.assistant.AssistantScreen
import com.example.myapplication.presentation.care.CareJourneyScreen
import com.example.myapplication.presentation.design.DesignSystemPreviewScreen
import com.example.myapplication.presentation.symptoms.SymptomTrackerScreen
import com.example.myapplication.presentation.education.EducationHubScreen
import com.example.myapplication.presentation.home.HomeScreen
import com.example.myapplication.presentation.myhealth.MyHealthScreen
import com.example.myapplication.presentation.profile.ProfileScreen
import com.example.myapplication.presentation.questions.DoctorQuestionsScreen
import com.example.myapplication.presentation.risk.RiskAssessmentScreen
import com.example.myapplication.presentation.vision.VisionLabScreen

sealed class MainTab(
    val index: Int,
    val titleRes: Int,
    val icon: ImageVector
) {
    data object Home : MainTab(
        index = 0,
        titleRes = R.string.nav_home,
        icon = Icons.Default.Home
    )

    data object Health : MainTab(
        index = 1,
        titleRes = R.string.nav_health,
        icon = Icons.Default.Favorite
    )

    data object Assistant : MainTab(
        index = 2,
        titleRes = R.string.nav_assistant,
        icon = Icons.Default.AutoAwesome
    )

    data object Care : MainTab(
        index = 3,
        titleRes = R.string.nav_care,
        icon = Icons.Default.MedicalServices
    )

    data object Profile : MainTab(
        index = 4,
        titleRes = R.string.nav_profile,
        icon = Icons.Default.Person
    )
}

enum class OverlayScreen {
    NONE,
    VISION_LAB,
    RISK_ASSESSMENT,
    DOCTOR_QUESTIONS,
    EDUCATION_HUB,
    DESIGN_SYSTEM_PREVIEW,
    SYMPTOMS_TRACKER
}

private val mainTabs = listOf(
    MainTab.Home,
    MainTab.Health,
    MainTab.Assistant,
    MainTab.Care,
    MainTab.Profile
)

@Composable
fun MainScreen(
    onToggleTheme: () -> Unit = {},
    onToggleLanguage: () -> Unit = {}
) {
    // Save simple values instead of the MainTab objects.
    var selectedTabIndex by rememberSaveable {
        mutableStateOf(MainTab.Home.index)
    }

    var activeOverlayName by rememberSaveable {
        mutableStateOf(OverlayScreen.NONE.name)
    }

    val selectedTab = mainTabs.firstOrNull {
        it.index == selectedTabIndex
    } ?: MainTab.Home

    val activeOverlay = OverlayScreen.entries.firstOrNull {
        it.name == activeOverlayName
    } ?: OverlayScreen.NONE

    val stateHolder = rememberSaveableStateHolder()

    val closeOverlay: () -> Unit = {
        activeOverlayName = OverlayScreen.NONE.name
    }

    val openOverlay: (OverlayScreen) -> Unit = { screen ->
        activeOverlayName = screen.name
    }

    BackHandler(
        enabled = activeOverlay != OverlayScreen.NONE ||
                selectedTab != MainTab.Home
    ) {
        if (activeOverlay != OverlayScreen.NONE) {
            closeOverlay()
        } else {
            selectedTabIndex = MainTab.Home.index
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (activeOverlay == OverlayScreen.NONE) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    com.example.myapplication.presentation.home.GlowBottomBar(
                        selectedIndex = selectedTabIndex,
                        onSelect = { index ->
                            selectedTabIndex = index
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    stateHolder.SaveableStateProvider(
                        key = "main_tab_${selectedTab.index}"
                    ) {
                        MainTabContent(
                            selectedTab = selectedTab,
                            onOpenOverlay = openOverlay,
                            onOpenHealth = {
                                selectedTabIndex = MainTab.Health.index
                            },
                            onToggleTheme = onToggleTheme,
                            onToggleLanguage = onToggleLanguage
                        )
                    }
                }
            }
        } else {
            stateHolder.SaveableStateProvider(
                key = "overlay_${activeOverlay.name}"
            ) {
                OverlayContent(
                    screen = activeOverlay,
                    onBack = closeOverlay
                )
            }
        }
    }
}

@Composable
private fun StellarisBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 10.dp
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = colors.surface,
            tonalElevation = 3.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.30f),
                        colors.onSurface.copy(alpha = 0.08f),
                        colors.secondary.copy(alpha = 0.25f)
                    )
                )
            )
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = colors.onSurface,
                tonalElevation = 0.dp,

                // The outer container handles system navigation insets.
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                mainTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val title = stringResource(tab.titleRes)

                    val accentColor =
                        if (tab == MainTab.Assistant) {
                            colors.secondary
                        } else {
                            colors.primary
                        }

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1f,
                        animationSpec = tween(durationMillis = 180)
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                onTabSelected(tab)
                            }
                        },
                        alwaysShowLabel = true,
                        icon = {
                            Box(
                                modifier = Modifier.size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,

                                    // The label already provides the tab name.
                                    contentDescription = null,

                                    modifier = Modifier
                                        .size(23.dp)
                                        .graphicsLayer {
                                            scaleX = iconScale
                                            scaleY = iconScale
                                        }
                                )
                            }
                        },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = accentColor,
                            selectedTextColor = accentColor,
                            unselectedIconColor = colors.onSurfaceVariant,
                            unselectedTextColor = colors.onSurfaceVariant,
                            indicatorColor = accentColor.copy(alpha = 0.14f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MainTabContent(
    selectedTab: MainTab,
    onOpenOverlay: (OverlayScreen) -> Unit,
    onOpenHealth: () -> Unit,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    when (selectedTab) {
        MainTab.Home -> {
            HomeScreen(
                onNavigateToVisionLab = {
                    onOpenOverlay(OverlayScreen.VISION_LAB)
                },
                onNavigateToSymptoms = {
                    onOpenOverlay(OverlayScreen.SYMPTOMS_TRACKER)
                },
                onNavigateToRecords = onOpenHealth,
                onNavigateToQuestions = {
                    onOpenOverlay(OverlayScreen.DOCTOR_QUESTIONS)
                },
                onNavigateToScreeningGuide = {
                    onOpenOverlay(OverlayScreen.RISK_ASSESSMENT)
                },
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage
            )
        }

        MainTab.Health -> {
            MyHealthScreen(
                onNavigateToSymptoms = {
                    onOpenOverlay(OverlayScreen.SYMPTOMS_TRACKER)
                },
                onNavigateToRiskAssessment = {
                    onOpenOverlay(OverlayScreen.RISK_ASSESSMENT)
                },
                onNavigateToScreening = {
                    onOpenOverlay(OverlayScreen.RISK_ASSESSMENT)
                },
                onNavigateToQuestions = {
                    onOpenOverlay(OverlayScreen.DOCTOR_QUESTIONS)
                },
                onNavigateToEducation = {
                    onOpenOverlay(OverlayScreen.EDUCATION_HUB)
                }
            )
        }

        MainTab.Assistant -> {
            AssistantScreen()
        }

        MainTab.Care -> {
            CareJourneyScreen()
        }

        MainTab.Profile -> {
            ProfileScreen(
                onToggleTheme = onToggleTheme,
                onToggleLanguage = onToggleLanguage,
                onNavigateToDesignPreview = {
                    onOpenOverlay(OverlayScreen.DESIGN_SYSTEM_PREVIEW)
                }
            )
        }
    }
}

@Composable
private fun OverlayContent(
    screen: OverlayScreen,
    onBack: () -> Unit
) {
    when (screen) {
        OverlayScreen.VISION_LAB -> {
            VisionLabScreen(onBack = onBack)
        }

        OverlayScreen.RISK_ASSESSMENT -> {
            RiskAssessmentScreen(onBack = onBack)
        }

        OverlayScreen.DOCTOR_QUESTIONS -> {
            DoctorQuestionsScreen(onBack = onBack)
        }

        OverlayScreen.EDUCATION_HUB -> {
            EducationHubScreen(onBack = onBack)
        }

        OverlayScreen.DESIGN_SYSTEM_PREVIEW -> {
            DesignSystemPreviewScreen(onBack = onBack)
        }

        OverlayScreen.SYMPTOMS_TRACKER -> {
            SymptomTrackerScreen(onBack = onBack)
        }

        OverlayScreen.NONE -> Unit
    }
}