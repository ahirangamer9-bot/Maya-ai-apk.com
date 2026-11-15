package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import com.example.controller.PhoneController
import com.example.service.FloatingAssistantService
import com.example.tts.MayaVoiceManager
import com.example.ui.theme.MayaBlue
import com.example.ui.theme.TextDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneControlScreen(
    voiceManager: MayaVoiceManager,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(PhoneController.isTorchActive()) }
    var batteryInfo by remember { mutableStateOf(PhoneController.getBatteryInfo(context)) }
    var isFloatingActive by remember { mutableStateOf(FloatingAssistantService.isServiceRunning) }

    Scaffold(
        containerColor = Color(0xFFF3F7FD),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Phone Control & Live Assistant 📱",
                        fontSize = 18.sp,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            // 1. Always Live on Homescreen Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEBFC)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🟢 Always Live on Homescreen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "फोन के होमस्क्रीन पर माया हमेशा फ्लोटिंग बबल के रूप में लाइव रहेगी।",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        Switch(
                            checked = isFloatingActive,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "कृपया 'Display over other apps' अनुमति दें", Toast.LENGTH_LONG).show()
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        val serviceIntent = Intent(context, FloatingAssistantService::class.java)
                                        context.startService(serviceIntent)
                                        isFloatingActive = true
                                        voiceManager.speak("माया अब आपके होमस्क्रीन पर लाइव है।")
                                        Toast.makeText(context, "माया होमस्क्रीन पर लाइव हो गई है!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val serviceIntent = Intent(context, FloatingAssistantService::class.java)
                                    context.stopService(serviceIntent)
                                    isFloatingActive = false
                                    Toast.makeText(context, "फ्लोटिंग असिस्टेंट बंद कर दिया गया", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    HorizontalDivider(color = Color(0xFFBFDBFE))

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "💡 Homescreen Widget: होमस्क्रीन के खाली हिस्से पर लॉन्ग-प्रेस करें -> 'Widgets' चुनें -> 'Maya AI Assistant' जोड़ें!",
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Flashlight & Volume Quick Controls
            Text(
                text = "हार्डवेयर एवं सिस्टम कंट्रोल",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Torch Tile
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isFlashOn = PhoneController.toggleTorch(context)
                            PhoneController.triggerHaptic(context)
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlashOn) Color(0xFF059669) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (isFlashOn) "🔦" else "💡", fontSize = 28.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (isFlashOn) "Torch ON" else "Torch OFF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isFlashOn) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = "Tap to toggle",
                            fontSize = 11.sp,
                            color = if (isFlashOn) Color.White.copy(alpha = 0.8f) else Color.Gray
                        )
                    }
                }

                // Battery Tile
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            batteryInfo = PhoneController.getBatteryInfo(context)
                            voiceManager.speak("आपके फोन की बैटरी ${batteryInfo.percentage} प्रतिशत है।")
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (batteryInfo.isCharging) "⚡" else "🔋", fontSize = 28.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${batteryInfo.percentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = if (batteryInfo.isCharging) "Charging..." else "Battery Level",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Volume Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🔊 वॉल्यूम कंट्रोल (Volume)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        Text("आवाज़ कम या ज्यादा करें", fontSize = 11.sp, color = Color.Gray)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { PhoneController.changeVolume(context, false) },
                            modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Volume Down", tint = MayaBlue)
                        }

                        IconButton(
                            onClick = { PhoneController.changeVolume(context, true) },
                            modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume Up", tint = MayaBlue)
                        }

                        IconButton(
                            onClick = { PhoneController.toggleMute(context) },
                            modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Mute", tint = Color.Red)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 3. Quick App Launcher
            Text(
                text = "क्विक ऐप लॉन्चर (Voice & Tap Launch)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(Modifier.height(10.dp))

            val apps = listOf(
                Triple("YouTube", "▶️", "youtube"),
                Triple("WhatsApp", "💬", "whatsapp"),
                Triple("Camera", "📷", "camera"),
                Triple("Phone", "📞", "dialer"),
                Triple("Settings", "⚙️", "settings"),
                Triple("Wi-Fi", "📶", "wifi")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                apps.take(3).forEach { (name, icon, key) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable {
                                if (key == "wifi") PhoneController.openConnectivitySettings(context, "wifi")
                                else PhoneController.launchApp(context, key)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                apps.drop(3).forEach { (name, icon, key) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable {
                                if (key == "wifi") PhoneController.openConnectivitySettings(context, "wifi")
                                else PhoneController.launchApp(context, key)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(icon, fontSize = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 4. Voice Commands Cheat Sheet Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎙️ माया से बोलकर कंट्रोल करें (Voice Commands):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MayaBlue
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("• \"फ्लैशलाइट ऑन करो\" / \"टॉर्च बंद करो\"", fontSize = 12.sp, color = TextDark)
                    Text("• \"वॉल्यूम बढ़ाओ\" / \"म्यूट करो\"", fontSize = 12.sp, color = TextDark)
                    Text("• \"यूट्यूब खोलो\" / \"व्हाट्सएप खोलो\"", fontSize = 12.sp, color = TextDark)
                    Text("• \"बैटरी कितनी है\"", fontSize = 12.sp, color = TextDark)
                    Text("• \"कैमरा खोलो\" / \"फोन सेटिंग्स खोलो\"", fontSize = 12.sp, color = TextDark)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
