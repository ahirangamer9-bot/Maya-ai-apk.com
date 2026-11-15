package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * MayaVoiceManager: A completely original female AI assistant voice engine named Maya.
 *
 * Voice Identity:
 * - A warm, sweet, natural Indian female voice with clear Hindi pronunciation
 *   and fluent Hinglish speaking ability.
 * - Balanced pitch (1.10f), soft vocal texture, smooth rhythm and natural breathing pauses.
 * - Adaptive emotional modulation: Cheerful on greetings, Calm on explanations,
 *   Confident on queries, and Gentle on learning.
 */
class MayaVoiceManager(context: Context) : TextToSpeech.OnInitListener {

    private var mayaVoice: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    companion object {
        const val UTTERANCE_ID = "MAYA_VOICE"

        // Baseline acoustic parameters for Maya's recognizable vocal identity
        const val BASE_PITCH = 1.10f
        const val BASE_SPEECH_RATE = 0.90f

        const val SIGNATURE_GREETING = "नमस्ते! मैं माया हूँ। आपकी पर्सनल AI असिस्टेंट। बताइए, आज मैं आपकी क्या मदद कर सकती हूँ?"
    }

    enum class VoiceEmotion {
        CHEERFUL,   // Cheerful on greetings & welcoming
        CALM,       // Calm & patient during explanations
        CONFIDENT,  // Confident & sharp on answers
        GENTLE      // Gentle & encouraging for learning
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true

            // Set Primary Locale to Hindi (India)
            @Suppress("DEPRECATION")
            mayaVoice?.language = Locale("hi", "IN")

            // Prefer Indian Female Voice pack if available
            try {
                val availableVoices = mayaVoice?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val femaleIndianVoice = availableVoices.firstOrNull { voice ->
                        val isIndian = voice.locale.country.equals("IN", ignoreCase = true)
                        val isFemale = voice.name.contains("female", ignoreCase = true) ||
                                voice.name.contains("fem", ignoreCase = true) ||
                                voice.name.contains("hie", ignoreCase = true) ||
                                voice.name.contains("hid", ignoreCase = true)
                        isIndian && isFemale && !voice.isNetworkConnectionRequired
                    }
                    if (femaleIndianVoice != null) {
                        mayaVoice?.voice = femaleIndianVoice
                    }
                }
            } catch (e: Exception) {
                Log.d("MayaVoiceManager", "Introspection skipped: ${e.message}")
            }

            // Apply baseline Maya acoustic characteristics
            mayaVoice?.setPitch(BASE_PITCH)
            mayaVoice?.setSpeechRate(BASE_SPEECH_RATE)

            // Progress listener
            mayaVoice?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    // Reset to base acoustics
                    mayaVoice?.setPitch(BASE_PITCH)
                    mayaVoice?.setSpeechRate(BASE_SPEECH_RATE)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e("MayaVoiceManager", "TTS initialization failed: $status")
        }
    }

    /**
     * Speaks text with Maya's signature voice and contextual emotional expression.
     */
    fun speakMaya(
        text: String,
        emotion: VoiceEmotion = detectEmotion(text)
    ) {
        if (!isInitialized || mayaVoice == null) return
        stop()

        // Natural breath & cadence processor
        val formattedSpeech = processNaturalBreathing(text)
        if (formattedSpeech.isBlank()) return

        // Fine acoustic modulation maintaining Maya's recognizable identity
        when (emotion) {
            VoiceEmotion.CHEERFUL -> {
                mayaVoice?.setPitch(1.13f)
                mayaVoice?.setSpeechRate(0.92f)
            }
            VoiceEmotion.CALM -> {
                mayaVoice?.setPitch(1.08f)
                mayaVoice?.setSpeechRate(0.88f)
            }
            VoiceEmotion.GENTLE -> {
                mayaVoice?.setPitch(1.09f)
                mayaVoice?.setSpeechRate(0.87f)
            }
            VoiceEmotion.CONFIDENT -> {
                mayaVoice?.setPitch(BASE_PITCH)
                mayaVoice?.setSpeechRate(BASE_SPEECH_RATE)
            }
        }

        _isSpeaking.value = true
        mayaVoice?.speak(
            formattedSpeech,
            TextToSpeech.QUEUE_FLUSH,
            null,
            UTTERANCE_ID
        )
    }

    fun speak(text: String) = speakMaya(text)

    fun greetUser() = speakMaya(SIGNATURE_GREETING, VoiceEmotion.CHEERFUL)

    fun stop() {
        mayaVoice?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        mayaVoice?.stop()
        mayaVoice?.shutdown()
        mayaVoice = null
        _isSpeaking.value = false
    }

    /**
     * Infers natural emotion from conversational cues.
     */
    private fun detectEmotion(text: String): VoiceEmotion {
        val lower = text.lowercase()
        return when {
            lower.contains("नमस्ते") || lower.contains("hello") || lower.contains("welcome") || lower.contains("शाबाश") || lower.contains("congratulations") ->
                VoiceEmotion.CHEERFUL
            lower.contains("समझिए") || lower.contains("उदाहरण") || lower.contains("step") || lower.contains("क्योंकि") || lower.contains("explaining") ->
                VoiceEmotion.CALM
            lower.contains("चिंता न करें") || lower.contains("ध्यान रखें") || lower.contains("आराम से") || lower.contains("सीखेंगे") ->
                VoiceEmotion.GENTLE
            else ->
                VoiceEmotion.CONFIDENT
        }
    }

    /**
     * Injects micro-pauses for punctuation, creates fluid bilingual Hindi-English
     * pacing, and strips machine artifacts (markdown, code fences).
     */
    private fun processNaturalBreathing(rawText: String): String {
        return rawText
            // Replace code blocks with concise spoken descriptor
            .replace(Regex("```[\\s\\S]*?```"), " कोड का विवरण नीचे दिया गया है। ")
            // Strip markdown formatting symbols
            .replace(Regex("[*#`_~>•\\[\\]\\(\\)]"), " ")
            // Strip emojis
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")
            // Normalize spaces
            .replace(Regex("\\s+"), " ")
            // Micro breathing pauses for realistic human rhythm
            .replace("।", "। , ")
            .replace("? ", "? , ")
            .replace("! ", "! , ")
            .replace(". ", ". , ")
            .trim()
    }
}
