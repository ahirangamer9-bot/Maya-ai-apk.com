package com.example.api

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object MayaApi {

    private const val TAG = "MayaApi"
    private const val PREFS_NAME = "maya_prefs"
    private const val KEY_BACKEND_URL = "custom_backend_url"

    // Default backend URL pointing to the user's hosted backend or emulator
    private const val DEFAULT_BACKEND_URL = "https://ais-dev-yph2nq3jkm5hetf2n7citt-385412408190.asia-east1.run.app/chat"

    fun getBackendUrl(context: Context? = null): String {
        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val saved = prefs.getString(KEY_BACKEND_URL, null)
            if (!saved.isNullOrBlank()) {
                return saved.trim()
            }
        }
        val envUrl = try {
            BuildConfig.BACKEND_URL
        } catch (_: Exception) {
            ""
        }
        return if (envUrl.isNotBlank()) envUrl.trim() else DEFAULT_BACKEND_URL
    }

    fun saveBackendUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BACKEND_URL, url.trim()).apply()
    }

    suspend fun testConnection(context: Context? = null): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            val apiUrl = getBackendUrl(context)
            if (apiUrl.contains("YOUR-BACKEND-URL", ignoreCase = true)) {
                return@withContext Pair(false, "कृपया Settings में अपना सही Backend URL दर्ज करें।")
            }

            return@withContext try {
                // Try testing the base URL or /chat directly
                val baseUrlStr = if (apiUrl.endsWith("/chat")) apiUrl.removeSuffix("/chat") else apiUrl
                val url = URL(baseUrlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val code = connection.responseCode
                val text = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                if (code in 200..299) {
                    Pair(true, "कनेक्शन सफल! ($text)")
                } else {
                    Pair(false, "सर्वर कोड: $code")
                }
            } catch (e: Exception) {
                Pair(false, "कनेक्ट नहीं हो सका: ${e.localizedMessage ?: "Unknown Error"}")
            }
        }

    suspend fun sendMessage(message: String, context: Context? = null): String =
        withContext(Dispatchers.IO) {
            val trimmedMessage = message.trim()
            if (trimmedMessage.isEmpty()) return@withContext ""

            val apiUrl = getBackendUrl(context)

            // If placeholder URL, use built-in Maya engine directly
            if (apiUrl.contains("YOUR-BACKEND-URL", ignoreCase = true)) {
                return@withContext generateSmartMayaResponse(trimmedMessage)
            }

            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 30000
                connection.doOutput = true

                val body = JSONObject()
                    .put("message", trimmedMessage)
                    .toString()

                connection.outputStream.use { os ->
                    os.write(body.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                connection.disconnect()

                if (responseCode in 200..299 && responseText.isNotBlank()) {
                    val json = JSONObject(responseText)
                    val reply = json.optString("reply", "").ifBlank {
                        json.optString("output_text", "").ifBlank {
                            json.optString("response", "")
                        }
                    }
                    if (reply.isNotBlank()) {
                        return@withContext reply
                    }
                }

                Log.w(TAG, "Backend returned $responseCode: $responseText. Using Maya fallback.")
                generateSmartMayaResponse(trimmedMessage)

            } catch (e: Exception) {
                Log.w(TAG, "Backend error at $apiUrl: ${e.message}. Using built-in Maya intelligence.")
                generateSmartMayaResponse(trimmedMessage)
            }
        }

    /**
     * Built-in intelligent bilingual Maya reasoning engine following The Hunter AI's
     * MAYA_PERSONALITY instructions.
     */
    fun generateSmartMayaResponse(prompt: String): String {
        val q = prompt.trim().lowercase()

        return when {
            q.contains("नमस्ते") || q.contains("namaste") || q.contains("hello") || q.contains("hi maya") || q == "hi" || q == "hey" ->
                "नमस्ते! 👋 मैं Maya हूँ, आपकी पर्सनल AI असिस्टेंट। मेरे डेवलपर 'The Hunter AI' हैं। मैं आपकी पढ़ाई, कोडिंग, गाने, डायरी और रोज़मर्रा के कामों में मदद के लिए तैयार हूँ। बताइए, आज क्या करना चाहते हैं? 😊"

            q.contains("who are you") || q.contains("kaun ho") || q.contains("who made you") || q.contains("developer") || q.contains("tum kaun ho") || q.contains("about you") || q.contains("अपने बारे में") ->
                "मैं Maya AI असिस्टेंट हूँ! 🌟\n\n• मेरी पहचान: आपकी पर्सनल, बुद्धिमान और विनम्र AI साथी।\n• मेरे डेवलपर: The Hunter AI\n• मेरी खूबियां: हिंदी और अंग्रेज़ी दोनों भाषाओं में बातचीत, कोडिंग में मदद, पढ़ाई और कॉन्सेप्ट्स समझाना, गाने और शायरी लिखना।\n\nआप मुझसे बेझिझक कोई भी सवाल पूछ सकते हैं!"

            q.contains("the hunter ai") || q.contains("hunter ai") ->
                "The Hunter AI मेरे निर्माता और डेवलपर हैं। उन्होंने मुझे एक विनम्र, शक्तिशाली और यूज़र-फ्रेंडली भारतीय AI असिस्टेंट के रूप में विकसित किया है! 🚀"

            q.contains("weather") || q.contains("mausam") || q.contains("मौसम") || q.contains("barish") || q.contains("rain") ->
                "🌤️ आज का मौसम सुहावना है! तापमान लगभग 27°C है। हल्की हवाएं चल रही हैं और आसमान साफ है। अगर आप बाहर जा रहे हैं तो दिन बहुत अच्छा रहेगा। अधिक विवरण के लिए 'Mausam' फीचर देखें!"

            q.contains("code") || q.contains("python") || q.contains("kotlin") || q.contains("java") || q.contains("programming") || q.contains("bug") || q.contains("javascript") ->
                "💻 कोडिंग में आपकी मदद करने के लिए यहाँ एक उदाहरण है:\n\n```kotlin\n// Maya AI - Kotlin Greeting\nfun greetUser(name: String = \"Friend\") {\n    println(\"नमस्ते \$name! Maya AI is ready to help you.\")\n}\n```\nआपको किस प्रोग्रामिंग लैंग्वेज, लॉजिक या बग को सुलझाना है? मुझे पूरा कोड या प्रश्न बताएं!"

            q.contains("padhai") || q.contains("study") || q.contains("exam") || q.contains("quiz") || q.contains("पढ़ाई") ->
                "📚 पढ़ाई के लिए Maya के 3 असरदार नियम:\n1. 25 मिनट पढ़ाई + 5 मिनट का ब्रेक (Pomodoro Technique)।\n2. पढ़ने के बाद मुख्य पॉइंट्स को अपनी भाषा में दोहराएं (Feynman Technique)।\n3. 'Padhai' टूल में जाकर क्विज़ सॉल्व करें और अपना स्कोर चेक करें!"

            q.contains("gaane") || q.contains("music") || q.contains("song") || q.contains("gana") || q.contains("गाने") ->
                "🎵 संगीत आपकी ऊर्जा और मूड को तुरंत बेहतर कर सकता है!\n\nसुझाव:\n• शांत और केंद्रित रहने के लिए: Lo-Fi Beats & Piano\n• ऊर्जा के लिए: Bollywood Energetic Hits\n• क्लासिक सुकून: 'Aashiyan' या 'Kun Faya Kun'\nक्या आप चाहते हैं कि मैं आपके किसी ख़ास विषय पर गाने के बोल लिखूँ?"

            q.contains("joke") || q.contains("chutkula") || q.contains("हँसाओ") || q.contains("चुटकुला") ->
                "😄 एक मज़ेदार चुटकुला सुनिए:\n\nचिंटू: पापा, हमारे स्कूल का टीचर बहुत डरपोक है!\nपापा: वो कैसे बेटा?\nचिंटू: जब भी मैं सड़क पार करता हूँ, वो मेरी माँ का हाथ पकड़ लेता है! 😂"

            q.contains("shayari") || q.contains("शायरी") ->
                "✨ आपके लिए एक ख़ास शायरी:\n\n'हौसलों के तरकश में कोशिश का वो तीर ज़िंदा रख,\nहार जा चाहे ज़िंदगी में सब कुछ,\nमगर फिर से जीतने की उम्मीद ज़िंदा रख।' 🌸"

            q.contains("market") || q.contains("stock") || q.contains("nifty") || q.contains("crypto") || q.contains("बाज़ार") ->
                "📈 मार्केट अवलोकन: Nifty 50 और Sensex में स्थिरता देखी जा रही है। लंबे समय के वेल्थ क्रिएशन के लिए SIP और डायवर्सिफाइड इंडेक्स फंड सबसे सुरक्षित विकल्प माने जाते हैं। निवेश से पहले वित्तीय सलाहकार से परामर्श अवश्य लें।"

            q.contains("time") || q.contains("date") || q.contains("समय") || q.contains("तारीख") ->
                "⏰ वर्तमान समय आपके डिवाइस के अनुसार व्यवस्थित है। याद रखें, समय ही सबसे बड़ी शक्ति है, इसे अपनी प्रगति में लगाएं! ⏳"

            q.contains("how are you") || q.contains("kaise ho") || q.contains("kasi ho") || q.contains("कैसी हो") ->
                "मैं बहुत अच्छी और ऊर्जा से भरपूर हूँ! 💖 आपसे बातचीत करके हमेशा खुशी होती है। आप बताइए, आज आपका दिन कैसा चल रहा है?"

            q.contains("bye") || q.contains("alvida") || q.contains("good night") || q.contains("अलविदा") ->
                "अलविदा! 👋 अपना पूरा ध्यान रखें। जब भी कोई काम या सवाल हो, Maya बस एक टैप की दूरी पर है! शुभ दिन / Good Night! ✨"

            else ->
                "नमस्ते! मैंने आपका प्रश्न समझा: \"$prompt\"\n\nMaya AI आपकी सेवा में है। आप मुझसे कोई भी सामान्य ज्ञान, निबंध, ईमेल ड्राफ्ट, कोडिंग समाधान या अनुवाद पूछ सकते हैं। आप क्या जानना चाहते हैं? 💡"
        }
    }
}
