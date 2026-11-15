package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
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
import com.example.data.ChatMessage
import com.example.data.MayaRepository
import com.example.tts.MayaVoiceManager
import com.example.ui.theme.BackgroundBlue
import com.example.ui.theme.MayaBlue
import com.example.ui.theme.TextDark
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    repository: MayaRepository,
    voiceManager: MayaVoiceManager,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatMessages by repository.chatMessages.collectAsState()
    val listState = rememberLazyListState()

    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size, isSending) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "नमस्ते Maya! अपने बारे में बताओ 👋",
        "Explain recursion with a fun code example 💻",
        "Write a 4-line Hindi poetry on Chai ☕",
        "Give me a 3-step study plan for exams 📚",
        "What are the top 3 rules of stock market? 📈"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Maya AI Chat 💬",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MayaBlue
                )
                Text(
                    text = "Powered by The Hunter AI",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            Row {
                if (chatMessages.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            voiceManager.stop()
                            repository.clearChatHistory()
                            Toast.makeText(context, "चैट साफ़ कर दी गई", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("New Chat", color = Color.Gray)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Message List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (chatMessages.isEmpty() && !isSending) {
                // Empty state greeting
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 36.sp)
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "नमस्ते! मैं Maya हूँ। 😊",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "The Hunter AI द्वारा विकसित आपकी पर्सनल AI असिस्टेंट।\nआज मैं आपकी क्या मदद कर सकती हूँ?",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    // Suggestion chips
                    Text(
                        text = "सुझाए गए विषय (Quick suggestions):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MayaBlue
                    )

                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickPrompts.take(3).forEach { prompt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        message = prompt
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatMessages) { item ->
                        ChatMessageItem(
                            message = item,
                            onSpeak = { text ->
                                voiceManager.speak(text)
                            },
                            onCopy = { text ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Maya AI", text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "कॉपी किया गया!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    if (isSending) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }

        // Horizontal Quick chips when in conversation
        if (chatMessages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickPrompts.forEach { prompt ->
                    SuggestionChip(
                        onClick = { message = prompt },
                        label = { Text(prompt, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Ask Maya anything... (हिंदी / English)", fontSize = 14.sp, color = Color.Gray)
                },
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = MayaBlue,
                    unfocusedBorderColor = Color(0xFFD1E4FA)
                ),
                maxLines = 3
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    val promptText = message.trim()
                    if (promptText.isNotBlank() && !isSending) {
                        message = ""
                        repository.addMessage("You", promptText)
                        isSending = true

                        // Check if prompt is a device command (torch, volume, apps, battery)
                        val cmdResult = com.example.controller.PhoneController.parseAndExecuteCommand(context, promptText)
                        if (cmdResult != null) {
                            repository.addMessage("Maya", cmdResult.responseText)
                            voiceManager.speakMaya(cmdResult.responseText)
                            isSending = false
                        } else {
                            coroutineScope.launch {
                                val reply = MayaApi.sendMessage(promptText, context)
                                repository.addMessage("Maya", reply)
                                isSending = false
                            }
                        }
                    }
                },
                enabled = message.isNotBlank() && !isSending,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MayaBlue),
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("➤", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeak: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val isUser = message.sender == "You"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .widthIn(max = 310.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MayaBlue else Color.White
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "Maya AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.9f) else MayaBlue
                    )

                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onSpeak(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = MayaBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onCopy(message.text) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = if (isUser) Color.White else TextDark
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dotAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dotAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier.padding(start = 8.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Maya सोच रही है ", fontSize = 12.sp, color = MayaBlue)
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MayaBlue.copy(alpha = dotAlpha1))
                )
                Spacer(Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MayaBlue.copy(alpha = dotAlpha2))
                )
                Spacer(Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MayaBlue.copy(alpha = dotAlpha3))
                )
            }
        }
    }
}
