package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.tts.MayaVoiceManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    voiceManager: MayaVoiceManager,
    onNavigateToChat: (initialPrompt: String?) -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToYaadein: () -> Unit,
    onSelectFeature: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLicense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greetingTime = when (hour) {
        in 4..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else -> "Good evening,"
    }

    val dayNumber = SimpleDateFormat("dd", Locale.getDefault()).format(Date())
    val dayNameMonth = SimpleDateFormat("EEE, MMM", Locale.getDefault()).format(Date())

    var promptInput by remember { mutableStateOf("") }

    val isSpeaking by voiceManager.isSpeaking.collectAsState()

    // 1. Rhythmic breathing and reactive voice animation for the neural core
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    val bannerGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner_glow"
    )

    val coreScale by infiniteTransition.animateFloat(
        initialValue = if (isSpeaking) 0.94f else 0.97f,
        targetValue = if (isSpeaking) 1.08f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 500 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_scale"
    )

    val coreRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 8000 else 22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "core_rotate"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 700 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_pulse"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isSpeaking) 0.7f else 0.45f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 700 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. User Uploaded Background Wallpaper
        Image(
            painter = painterResource(id = R.drawable.maya_homescreen_bg),
            contentDescription = "Maya HomeScreen Wallpaper",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Translucent Ambient Scrim Gradient for text contrast & glowing aura
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x55000000), // top bar scrim
                            Color(0x10000000), // center clear to reveal Maya's portrait & glowing hearts
                            Color(0x600A1128), // card area scrim
                            Color(0xB3050B1A)  // bottom prompt area
                        )
                    )
                )
        )

        // 3. Foreground Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // 1. Top Bar: Hamburger Menu, Title "Maya", Notification Bell
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Menu Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.90f))
                        .clickable { onOpenSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu & Settings",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Maya Title
                Text(
                    text = "Maya",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.7f),
                            offset = Offset(2f, 2f),
                            blurRadius = 8f
                        )
                    )
                )

            // Circular Notification Button with active status indicator
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.90f))
                    .clickable {
                        voiceManager.greetUser()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = Color(0xFF1E293B),
                    modifier = Modifier.size(22.dp)
                )
                // Status dot indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 2. Official Maya AI Assistant Hero Banner (User Uploaded Banner)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0xFF38BDF8),
                    ambientColor = Color(0xFFEC4899)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF38BDF8).copy(alpha = bannerGlow),
                            Color(0xFFEC4899).copy(alpha = bannerGlow),
                            Color(0xFF6366F1).copy(alpha = bannerGlow)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .clickable {
                    voiceManager.greetUser()
                },
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                // Banner Image
                Image(
                    painter = painterResource(id = R.drawable.maya_ai_banner),
                    contentDescription = "Maya AI Assistant Official Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom Floating Action Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Talk with Maya pill
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        modifier = Modifier
                            .clickable { onNavigateToVoice() }
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎤", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Talk to Maya",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Start Chat pill
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF2563EB).copy(alpha = 0.9f),
                        modifier = Modifier
                            .clickable { onNavigateToChat(null) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💬", fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Start Chat",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. Subscription / License Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lock Icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "License",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free mode • aaj 10:00 min baaki",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Saare tools aur unlimited baat ke liye license activate karo",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        lineHeight = 15.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "Activate karo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenLicense() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // 4. Greeting & Energy Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = greetingTime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.7f),
                            offset = Offset(2f, 2f),
                            blurRadius = 8f
                        )
                    )
                )
                Text(
                    text = "dost",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF67E8F9),
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(2f, 2f),
                            blurRadius = 10f
                        )
                    )
                )
            }

            // Energy Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚡", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "1",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Energy",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 5. Central Interactive Neural Core Orb with User Uploaded Image Animation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // A. Expanding Holographic Glow Pulse
            Box(
                modifier = Modifier
                    .size(245.dp)
                    .scale(glowPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFF43F5E).copy(alpha = glowAlpha),
                                Color(0xFF38BDF8).copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // B. Rotating Technological Orbit Ring
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .rotate(coreRotate)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFFDA4AF).copy(alpha = if (isSpeaking) 0.6f else 0.35f),
                        shape = CircleShape
                    )
            )

            // C. Counter-Rotating Subtle Sci-Fi Ring
            Box(
                modifier = Modifier
                    .size(215.dp)
                    .rotate(-coreRotate * 0.7f)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFFFB7185).copy(alpha = 0.6f),
                                Color.Transparent,
                                Color(0xFF38BDF8).copy(alpha = 0.5f),
                                Color(0xFFFB7185).copy(alpha = 0.6f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // D. Exact User Uploaded Image with Breathing & Audio Reactivity Animation
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(coreScale)
                    .shadow(
                        elevation = if (isSpeaking) 18.dp else 10.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFFF43F5E),
                        ambientColor = Color(0xFF38BDF8)
                    )
                    .clip(CircleShape)
                    .clickable {
                        // Speaks Maya's warm signature greeting
                        voiceManager.greetUser()
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.maya_neural_core_square),
                    contentDescription = "Maya Neural Brain Animation",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // E. Active Speech Status Badge
            if (isSpeaking) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-4).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔊", fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Maya बोल रही है...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 6. Tool Pills Row: 🎵 Gaane | 📖 Padhai | ✏️ Journal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolPillButton(
                icon = "🎵",
                label = "Gaane",
                onClick = { onSelectFeature("🎵 Gaane") },
                modifier = Modifier.weight(1f)
            )
            ToolPillButton(
                icon = "📖",
                label = "Padhai",
                onClick = { onSelectFeature("📚 Padhai") },
                modifier = Modifier.weight(1f)
            )
            ToolPillButton(
                icon = "✏️",
                label = "Journal",
                onClick = { onSelectFeature("📝 Journal") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(14.dp))

        // 7. Status Info Cards Grid: ☁ Mausam | 📅 Aaj | 💗 Mood
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mausam Card
            StatusCard(
                title = "☁ Mausam",
                mainValue = "—",
                subValue = "Data nahi",
                onClick = { onSelectFeature("🌤️ Mausam") },
                modifier = Modifier.weight(1f)
            )

            // Aaj (Date) Card
            StatusCard(
                title = "📅 Aaj",
                mainValue = dayNumber,
                subValue = dayNameMonth,
                onClick = { onSelectFeature("📝 Journal") },
                modifier = Modifier.weight(1f)
            )

            // Mood Card
            StatusCard(
                title = "💗 Mood",
                mainValue = "Pyaar ...",
                subValue = "Sab badhiya",
                onClick = { onSelectFeature("📝 Journal") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(18.dp))

        // 8. Bottom Input Bar: Paperclip + "Maya se kuch bhi poochho..." + Send Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paperclip icon
                IconButton(
                    onClick = { onNavigateToScan() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach / Scan",
                        tint = Color(0xFF475569)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Text Field
                TextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            text = "Maya se kuch bhi poochho...",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                // White Send Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            val text = promptInput.trim()
                            onNavigateToChat(text.ifBlank { null })
                            promptInput = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color(0xFF1E293B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(90.dp))
    }
}
}

@Composable
fun ToolPillButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 15.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    mainValue: String,
    subValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = mainValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = subValue,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
