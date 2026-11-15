package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.api.MayaApi
import com.example.data.MayaRepository
import com.example.tts.MayaVoiceManager
import com.example.ui.theme.BackgroundBlue
import com.example.ui.theme.MayaBlue
import com.example.ui.theme.MayaDarkBlue
import com.example.ui.theme.TextDark
import kotlinx.coroutines.launch

@Composable
fun VoiceScreen(
    repository: MayaRepository,
    voiceManager: MayaVoiceManager,
    modifier: Modifier = Modifier,
    onNavigateToChat: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var mayaResponse by remember { mutableStateOf("नमस्ते! मैं माया हूँ। माइक बटन दबाकर बोलिए।") }
    var isThinking by remember { mutableStateOf(false) }

    val isSpeaking by voiceManager.isSpeaking.collectAsState()

    // Orb Animation
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isListening || isSpeaking) 0.95f else 1.0f,
        targetValue = if (isListening || isSpeaking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Speech Recognizer setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    DisposableEffect(Unit) {
        val recognizer = if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
        speechRecognizer = recognizer

        onDispose {
            recognizer?.destroy()
            voiceManager.stop()
        }
    }

    fun startListening() {
        voiceManager.stop()
        val recognizer = speechRecognizer
        if (recognizer == null) {
            Toast.makeText(context, "Speech Recognition उपलब्ध नहीं है।", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Maya सुन रही है...")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                recognizedText = "सुन रही हूँ..."
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
            }
            override fun onError(error: Int) {
                isListening = false
                if (recognizedText == "सुन रही हूँ...") {
                    recognizedText = "आवाज़ सुनाई नहीं दी, कृपया पुनः प्रयास करें।"
                }
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spoken = matches?.firstOrNull().orEmpty()
                if (spoken.isNotBlank()) {
                    recognizedText = spoken
                    repository.addMessage("You", spoken)
                    isThinking = true
                    val cmdResult = com.example.controller.PhoneController.parseAndExecuteCommand(context, spoken)
                    if (cmdResult != null) {
                        mayaResponse = cmdResult.responseText
                        repository.addMessage("Maya", cmdResult.responseText)
                        isThinking = false
                        voiceManager.speakMaya(cmdResult.responseText)
                    } else {
                        coroutineScope.launch {
                            val reply = MayaApi.sendMessage(spoken, context)
                            mayaResponse = reply
                            repository.addMessage("Maya", reply)
                            isThinking = false
                            voiceManager.speakMaya(reply)
                        }
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull().orEmpty()
                if (partial.isNotBlank()) {
                    recognizedText = partial
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(context, "माइक शुरू करने में समस्या आई", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            Toast.makeText(context, "वॉयस असिस्टेंट के लिए माइक की अनुमति आवश्यक है", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header with Voice Identity Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Maya Voice 🎤",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MayaBlue
            )
            Text(
                text = "Warm, Sweet & Natural Indian Female Voice",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Spacer(Modifier.height(8.dp))

            // Voice Samples Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SuggestionChip(
                    onClick = {
                        mayaResponse = MayaVoiceManager.SIGNATURE_GREETING
                        voiceManager.greetUser()
                    },
                    label = { Text("🌸 Cheerful Greeting", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        val text = "शांत मन से समझिए, जब हम किसी लक्ष्य को छोटे स्टेप्स में बांटते हैं, तो कठिन से कठिन समस्या भी आसान हो जाती है।"
                        mayaResponse = text
                        voiceManager.speakMaya(text, MayaVoiceManager.VoiceEmotion.CALM)
                    },
                    label = { Text("📚 Calm Explaining", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        val text = "Maya AI पूरी तरह तैयार है। आप मुझसे कोडिंग, साइंस या मार्केट के सवाल बेझिझक पूछ सकते हैं।"
                        mayaResponse = text
                        voiceManager.speakMaya(text, MayaVoiceManager.VoiceEmotion.CONFIDENT)
                    },
                    label = { Text("💡 Confident Answer", fontSize = 11.sp) }
                )
                SuggestionChip(
                    onClick = {
                        val text = "बिल्कुल चिंता मत कीजिए, हम साथ मिलकर यह नया विषय सीखेंगे। गलतियों से ही तो हम सीखते हैं!"
                        mayaResponse = text
                        voiceManager.speakMaya(text, MayaVoiceManager.VoiceEmotion.GENTLE)
                    },
                    label = { Text("💖 Gentle Learning", fontSize = 11.sp) }
                )
            }
        }

        // Glowing Animated Orb
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow ring
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MayaBlue.copy(alpha = if (isListening) 0.5f else 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Core Glowing Orb
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MayaBlue,
                                MayaDarkBlue,
                                Color(0xFF6366F1)
                            )
                        )
                    )
                    .clickable {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPerm) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (isListening) {
                                speechRecognizer?.stopListening()
                                isListening = false
                            } else {
                                startListening()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Mic,
                    contentDescription = "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        // Live status & transcription card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        isListening -> "🔴 Maya सुन रही है..."
                        isThinking -> "⏳ Maya जवाब सोच रही है..."
                        isSpeaking -> "🔊 Maya बोल रही है..."
                        else -> "माइक पर टैप करके बोलें"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isListening) Color.Red else MayaBlue
                )

                if (recognizedText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "आप: \"$recognizedText\"",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = mayaResponse,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextDark,
                    textAlign = TextAlign.Center
                )

                if (isSpeaking) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { voiceManager.stop() },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("आवाज़ रोकें (Stop)", color = Color.Red)
                    }
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPerm) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        startListening()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) Color.Red else MayaBlue
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Listen",
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isListening) "Listening..." else "Tap to Speak")
            }

            OutlinedButton(
                onClick = onNavigateToChat,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Open Text Chat")
            }
        }
    }
}
