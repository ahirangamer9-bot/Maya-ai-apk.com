package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.MayaApi
import com.example.data.MayaRepository
import com.example.tts.MayaVoiceManager
import com.example.ui.theme.BackgroundBlue
import com.example.ui.theme.MayaBlue
import com.example.ui.theme.TextDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: MayaRepository,
    voiceManager: MayaVoiceManager,
    onBack: () -> Unit,
    onSelectFeature: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var backendUrlInput by remember {
        mutableStateOf(MayaApi.getBackendUrl(context))
    }
    var connectionStatus by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    var showLicenseDialog by remember { mutableStateOf(false) }
    var licenseKeyInput by remember { mutableStateOf("") }
    var isLicenseActive by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF3F7FD),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            // Official Maya AI Assistant Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maya_ai_banner),
                    contentDescription = "Maya AI Assistant Official Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            // Phone Control & Live Homescreen Assistant Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectFeature("📱 Phone Control") },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📱", fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Phone Control & Live Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Homescreen Bubble, Torch, Apps & Volume",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                    Text("›", fontSize = 24.sp, color = Color(0xFF38BDF8))
                }
            }

            Spacer(Modifier.height(16.dp))

            // 1. Account & Subscription Card (Matching HomeScreen Banner Style)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEBFC)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "License",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isLicenseActive) "Maya Pro • Unlimited" else "Free mode • aaj 10:00 min baaki",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                        }

                        Button(
                            onClick = { showLicenseDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLicenseActive) Color(0xFF10B981) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isLicenseActive) "Active ✓" else "Activate karo",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (isLicenseActive)
                            "सभी टूल्स और अनलिमिटेड बातचीत अनलॉक है।"
                        else
                            "Saare tools aur unlimited baat ke liye license activate karo",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isLicenseActive) "Unlimited Energy" else "1 Energy remaining today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 2. Maya Voice Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = Color(0xFF2563EB)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Maya AI Voice (आवाज़)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "• Identity: Female Indian Voice (Hindi + Hinglish)\n• Speech Rate: 0.90f | Pitch: 1.10f\n• Characteristic: Soft, Sweet, Natural Breathing",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            voiceManager.greetUser()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Maya Voice सुनें (Test Greeting)", color = Color(0xFF2563EB), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3. Secure Backend URL Configuration
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Backend",
                            tint = Color(0xFF2563EB)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Secure Backend API URL",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "यहाँ अपना Node.js Express या Cloud Run बैकएंड URL सेट करें:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = backendUrlInput,
                        onValueChange = { backendUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFD1E4FA)
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                MayaApi.saveBackendUrl(context, backendUrlInput)
                                Toast.makeText(context, "URL सुरक्षित कर लिया गया!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save URL", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                isTestingConnection = true
                                connectionStatus = "सर्वर टेस्ट हो रहा है..."
                                coroutineScope.launch {
                                    val (ok, msg) = MayaApi.testConnection(context)
                                    connectionStatus = if (ok) "✅ $msg" else "⚠️ $msg"
                                    isTestingConnection = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isTestingConnection
                        ) {
                            Text(if (isTestingConnection) "Testing..." else "Test Server", fontSize = 13.sp)
                        }
                    }

                    if (connectionStatus != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = connectionStatus ?: "",
                            fontSize = 12.sp,
                            color = if (connectionStatus?.startsWith("✅") == true) Color(0xFF10B981) else Color(0xFFF43F5E)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 4. Quick Tools Directory
            Text(
                text = "AI Suite Tools",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(8.dp))

            val toolLinks = listOf(
                "🎵 Gaane (Music Assistant)" to "🎵 Gaane",
                "📚 Padhai (Study Assistant)" to "📚 Padhai",
                "📝 Journal (Daily Diary)" to "📝 Journal",
                "🌤️ Mausam (Weather Forecast)" to "🌤️ Mausam",
                "📈 Markets (Financial Insights)" to "📈 Markets",
                "💻 Coding (Programming Helper)" to "💻 Coding",
                "📢 Marketing (Ad Copy & Captions)" to "📢 Marketing",
                "📄 Documents (Summarizer)" to "📄 Documents",
                "🖼️ Meri Photos (Gallery)" to "🖼️ Meri Photos",
                "🌐 Browser (Web Explorer)" to "🌐 Browser",
                "🔐 License (Premium Status)" to "🔐 License"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    toolLinks.forEach { (label, id) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectFeature(id) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = label, fontSize = 13.sp, color = Color(0xFF1E293B))
                            Text(text = "›", fontSize = 20.sp, color = Color(0xFF2563EB))
                        }
                        if (id != "🔐 License") {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 5. Data & Privacy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Data & Privacy",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "चैट और यादें आपके डिवाइस पर सुरक्षित रहती हैं।",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            repository.clearChatHistory()
                            Toast.makeText(context, "चैट इतिहास साफ़ कर दिया गया", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Chat History", color = Color.Red, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // License Activation Dialog
        if (showLicenseDialog) {
            AlertDialog(
                onDismissRequest = { showLicenseDialog = false },
                title = { Text("Activate Maya License", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "अपना लाइसेंस की दर्ज करें या Instant Trial सक्रिय करें:",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        OutlinedTextField(
                            value = licenseKeyInput,
                            onValueChange = { licenseKeyInput = it },
                            placeholder = { Text("e.g. MAYA-PRO-2026") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isLicenseActive = true
                            showLicenseDialog = false
                            Toast.makeText(context, "Maya Pro सक्रिय हो गया! 🎉", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Activate")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLicenseDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
