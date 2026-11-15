package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayaRepository
import com.example.tts.MayaVoiceManager
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var voiceManager: MayaVoiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        voiceManager = MayaVoiceManager(this)

        setContent {
            val repository = remember { MayaRepository(this) }
            val targetTabFromWidget = intent?.getStringExtra("target_tab")
            var isAppLoading by remember { mutableStateOf(targetTabFromWidget.isNullOrBlank()) }

            MayaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE8F1FC)
                ) {
                    AnimatedContent(
                        targetState = isAppLoading,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "app_root_transition"
                    ) { loading ->
                        if (loading) {
                            // 1. First image shown when app opens (Splash & Loading Screen)
                            SplashScreen(
                                onFinished = {
                                    isAppLoading = false
                                }
                            )
                        } else {
                            // 2. Main Maya AI Assistant App styled per Screenshot 2
                            MayaApp(
                                repository = repository,
                                voiceManager = voiceManager,
                                initialTab = targetTabFromWidget ?: "Home"
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.shutdown()
    }
}

@Composable
fun MayaApp(
    repository: MayaRepository,
    voiceManager: MayaVoiceManager,
    initialTab: String = "Home"
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var currentSubFeature by remember { mutableStateOf<String?>(null) }

    // Intercept Back button
    if (currentSubFeature != null) {
        BackHandler {
            currentSubFeature = null
        }
    } else if (selectedTab != "Home") {
        BackHandler {
            selectedTab = "Home"
        }
    }

    Scaffold(
        containerColor = if (selectedTab == "Home" && currentSubFeature == null) Color(0xFF070B18) else Color(0xFFE8F1FC),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            // Floating Bottom Navigation Dock matching Screenshot 2
            if (currentSubFeature == null) {
                FloatingBottomDock(
                    selectedTab = selectedTab,
                    onSelectTab = { tab ->
                        selectedTab = tab
                    },
                    onOpenVoice = {
                        selectedTab = "Voice"
                    }
                )
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)

        // Subfeature Screen takes precedence if opened
        if (currentSubFeature != null) {
            when (currentSubFeature) {
                "📷 Camera Scan" -> CameraScanScreen(onBack = { currentSubFeature = null })
                "🧠 Yaadein" -> YaadeinScreen(repository = repository, onBack = { currentSubFeature = null })
                "📚 Padhai" -> PadhaiScreen(voiceManager = voiceManager, onBack = { currentSubFeature = null })
                "🎵 Gaane" -> GaaneScreen(voiceManager = voiceManager, onBack = { currentSubFeature = null })
                "📝 Journal" -> JournalScreen(repository = repository, onBack = { currentSubFeature = null })
                "🌤️ Mausam" -> MausamScreen(onBack = { currentSubFeature = null })
                "📈 Markets" -> MarketsScreen(onBack = { currentSubFeature = null })
                "📢 Marketing" -> MarketingScreen(onBack = { currentSubFeature = null })
                "📄 Documents" -> DocumentsScreen(onBack = { currentSubFeature = null })
                "🖼️ Meri Photos" -> MeriPhotosScreen(onBack = { currentSubFeature = null })
                "💻 Coding" -> CodingScreen(onBack = { currentSubFeature = null })
                "🌐 Browser" -> BrowserScreen(onBack = { currentSubFeature = null })
                "🔐 License" -> LicenseScreen(onBack = { currentSubFeature = null })
                "📱 Phone Control" -> PhoneControlScreen(voiceManager = voiceManager, onBack = { currentSubFeature = null })
                else -> {
                    currentSubFeature = null
                }
            }
        } else {
            // Main Tab Navigation
            when (selectedTab) {
                "Chat" -> ChatScreen(
                    repository = repository,
                    voiceManager = voiceManager,
                    modifier = modifier
                )

                "Voice" -> VoiceScreen(
                    repository = repository,
                    voiceManager = voiceManager,
                    modifier = modifier,
                    onNavigateToChat = { selectedTab = "Chat" }
                )

                "Scan" -> CameraScanScreen(
                    onBack = { selectedTab = "Home" }
                )

                "Yaadein" -> YaadeinScreen(
                    repository = repository,
                    onBack = { selectedTab = "Home" }
                )

                "Settings" -> SettingsScreen(
                    repository = repository,
                    voiceManager = voiceManager,
                    onBack = { selectedTab = "Home" },
                    onSelectFeature = { featureName ->
                        currentSubFeature = featureName
                    },
                    modifier = modifier
                )

                else -> HomeScreen(
                    voiceManager = voiceManager,
                    onNavigateToChat = { prompt ->
                        if (!prompt.isNullOrBlank()) {
                            repository.addMessage("You", prompt)
                        }
                        selectedTab = "Chat"
                    },
                    onNavigateToVoice = { selectedTab = "Voice" },
                    onNavigateToScan = { selectedTab = "Scan" },
                    onNavigateToYaadein = { selectedTab = "Yaadein" },
                    onSelectFeature = { featureName ->
                        currentSubFeature = featureName
                    },
                    onOpenSettings = { selectedTab = "Settings" },
                    onOpenLicense = { currentSubFeature = "🔐 License" },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun FloatingBottomDock(
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    onOpenVoice: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating White Dock Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home Item
                DockNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selectedTab == "Home",
                    onClick = { onSelectTab("Home") }
                )

                // 2. Scan Item
                DockNavItem(
                    label = "Scan",
                    icon = Icons.Default.CropFree,
                    isSelected = selectedTab == "Scan",
                    onClick = { onSelectTab("Scan") }
                )

                // 3. Center Elevated Mic Button Spacer
                Spacer(modifier = Modifier.size(54.dp))

                // 4. Yaadein Item
                DockNavItem(
                    label = "Yaadein",
                    icon = Icons.Default.Psychology,
                    isSelected = selectedTab == "Yaadein",
                    onClick = { onSelectTab("Yaadein") }
                )

                // 5. Chat Item
                DockNavItem(
                    label = "Chat",
                    icon = Icons.Default.ChatBubbleOutline,
                    isSelected = selectedTab == "Chat",
                    onClick = { onSelectTab("Chat") }
                )
            }
        }

        // Center Elevated Glowing Mic Button
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF93C5FD),
                            Color(0xFF3B82F6),
                            Color(0xFF1D4ED8)
                        )
                    )
                )
                .clickable { onOpenVoice() }
                .padding(3.dp), // Gradient ring thickness
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Assistant",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun DockNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        // Active Blue Pill style as in screenshot
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF90C2FA))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }
    } else {
        // Inactive item
        Column(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF334155),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF334155),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
