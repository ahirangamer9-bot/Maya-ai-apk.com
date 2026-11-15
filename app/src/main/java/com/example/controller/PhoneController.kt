package com.example.controller

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean
)

data class CommandResult(
    val executed: Boolean,
    val responseText: String
)

object PhoneController {

    private const val TAG = "PhoneController"
    private var isTorchOn: Boolean = false

    // 1. Flashlight / Torch Control
    fun toggleTorch(context: Context): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            isTorchOn
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle flashlight: ${e.message}")
            false
        }
    }

    fun setTorch(context: Context, on: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            cameraManager.setTorchMode(cameraId, on)
            isTorchOn = on
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set flashlight: ${e.message}")
            false
        }
    }

    fun isTorchActive(): Boolean = isTorchOn

    // 2. Battery & Telemetry
    fun getBatteryInfo(context: Context): BatteryInfo {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, filter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 85
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            BatteryInfo(percentage, isCharging)
        } catch (e: Exception) {
            BatteryInfo(85, false)
        }
    }

    // 3. Volume Control
    fun changeVolume(context: Context, increase: Boolean): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return -1
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun toggleMute(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        if (current > 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
            return true
        } else {
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max / 2, AudioManager.FLAG_SHOW_UI)
            return false
        }
    }

    // 4. Quick App Launcher
    fun launchApp(context: Context, appKey: String): Boolean {
        return try {
            val intent = when (appKey.lowercase()) {
                "youtube" -> {
                    context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
                }
                "whatsapp" -> {
                    context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://whatsapp.com"))
                }
                "camera" -> {
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                }
                "chrome", "browser" -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                }
                "maps" -> {
                    Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=nearby"))
                }
                "settings" -> {
                    Intent(Settings.ACTION_SETTINGS)
                }
                "dialer", "phone" -> {
                    Intent(Intent.ACTION_DIAL)
                }
                else -> null
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else false
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: $appKey", e)
            false
        }
    }

    // 5. Connectivity Settings
    fun openConnectivitySettings(context: Context, settingType: String) {
        val intent = when (settingType.lowercase()) {
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "display", "brightness" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // 6. Haptic Feedback
    fun triggerHaptic(context: Context, durationMs: Long = 60) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    // 7. Natural Language Phone Control Command Parser
    fun parseAndExecuteCommand(context: Context, command: String): CommandResult? {
        val q = command.trim().lowercase()

        // Flashlight commands
        if (q.contains("torch on") || q.contains("flashlight on") || q.contains("फ्लैशलाइट ऑन") || q.contains("टॉर्च ऑन") || q.contains("बत्ती जलाओ")) {
            setTorch(context, true)
            triggerHaptic(context)
            return CommandResult(true, "🔦 मैंने आपके फोन की फ्लैशलाइट ऑन कर दी है।")
        }
        if (q.contains("torch off") || q.contains("flashlight off") || q.contains("फ्लैशलाइट बंद") || q.contains("टॉर्च बंद") || q.contains("बत्ती बुझाओ")) {
            setTorch(context, false)
            triggerHaptic(context)
            return CommandResult(true, "🔦 फ्लैशलाइट बंद कर दी गई है।")
        }

        // Volume commands
        if (q.contains("volume up") || q.contains("volume badhao") || q.contains("आवाज़ बढ़ाओ") || q.contains("वॉल्यूम बढ़ाओ") || q.contains("वॉल्यूम बढ़ा") || q.contains("sound up")) {
            val vol = changeVolume(context, true)
            return CommandResult(true, "🔊 वॉल्यूम बढ़ा दिया गया है ($vol)।")
        }
        if (q.contains("volume down") || q.contains("volume kam karo") || q.contains("आवाज़ कम करो") || q.contains("वॉल्यूम कम करो") || q.contains("वॉल्यूम घटाओ") || q.contains("sound down")) {
            val vol = changeVolume(context, false)
            return CommandResult(true, "🔉 वॉल्यूम कम कर दिया गया है ($vol)।")
        }
        if (q.contains("mute") || q.contains("म्यूट करो") || q.contains("silent karo")) {
            val muted = toggleMute(context)
            return CommandResult(true, if (muted) "🔇 फोन म्यूट कर दिया गया है।" else "🔊 म्यूट हटा दिया गया है।")
        }

        // Battery inquiry
        if (q.contains("battery") || q.contains("बैटरी") || q.contains("charge") || q.contains("चार्ज")) {
            val info = getBatteryInfo(context)
            val state = if (info.isCharging) "चार्जिंग पर लगा है ⚡" else "डिस्चार्ज हो रहा है"
            return CommandResult(true, "🔋 आपके फोन की बैटरी ${info.percentage}% है और फोन $state।")
        }

        // App Launch commands
        if (q.contains("youtube") || q.contains("यूट्यूब")) {
            launchApp(context, "youtube")
            return CommandResult(true, "▶️ YouTube खोला जा रहा है...")
        }
        if (q.contains("whatsapp") || q.contains("व्हाट्सएप")) {
            launchApp(context, "whatsapp")
            return CommandResult(true, "💬 WhatsApp खोला जा रहा है...")
        }
        if (q.contains("camera kholo") || q.contains("open camera") || q.contains("कैमरा खोलो") || q.contains("फोटो खींचो")) {
            launchApp(context, "camera")
            return CommandResult(true, "📷 कैमरा खोला जा रहा है...")
        }
        if (q.contains("wifi") || q.contains("वाईफाई")) {
            openConnectivitySettings(context, "wifi")
            return CommandResult(true, "📶 वाई-फाई सेटिंग्स खोली जा रही हैं...")
        }
        if (q.contains("bluetooth") || q.contains("ब्लूटूथ")) {
            openConnectivitySettings(context, "bluetooth")
            return CommandResult(true, "🔷 ब्लूटूथ सेटिंग्स खोली जा रही हैं...")
        }
        if (q.contains("settings kholo") || q.contains("phone setting") || q.contains("सेटिंग खोलो")) {
            launchApp(context, "settings")
            return CommandResult(true, "⚙️ फोन की सेटिंग्स खोली जा रही हैं...")
        }
        if (q.contains("dialer") || q.contains("call") || q.contains("कॉल करो") || q.contains("फोन लगाओ")) {
            launchApp(context, "dialer")
            return CommandResult(true, "📞 फोन डायलर खोला जा रहा है...")
        }

        return null
    }
}
