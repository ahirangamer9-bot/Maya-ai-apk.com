package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val sender: String, // "You" or "Maya"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class YaadeinItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val detail: String,
    val category: String = "General",
    val dateString: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
)

data class JournalEntry(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val content: String,
    val mood: String = "😊 Happy",
    val aiInsight: String = "",
    val dateString: String = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
)

class MayaRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("maya_data", Context.MODE_PRIVATE)

    // Chat history state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(loadChatHistory())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Yaadein (Memories) state
    private val _memories = MutableStateFlow<List<YaadeinItem>>(loadMemories())
    val memories: StateFlow<List<YaadeinItem>> = _memories.asStateFlow()

    // Journal entries state
    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(loadJournalEntries())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    fun addMessage(sender: String, text: String) {
        val updated = _chatMessages.value + ChatMessage(sender, text)
        _chatMessages.value = updated
        saveChatHistory(updated)
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        prefs.edit().remove("chat_history").apply()
    }

    fun addMemory(title: String, detail: String, category: String = "General") {
        val newItem = YaadeinItem(title = title, detail = detail, category = category)
        val updated = listOf(newItem) + _memories.value
        _memories.value = updated
        saveMemories(updated)
    }

    fun deleteMemory(id: String) {
        val updated = _memories.value.filter { it.id != id }
        _memories.value = updated
        saveMemories(updated)
    }

    fun addJournalEntry(title: String, content: String, mood: String, aiInsight: String) {
        val newEntry = JournalEntry(
            title = title,
            content = content,
            mood = mood,
            aiInsight = aiInsight
        )
        val updated = listOf(newEntry) + _journalEntries.value
        _journalEntries.value = updated
        saveJournalEntries(updated)
    }

    fun deleteJournalEntry(id: String) {
        val updated = _journalEntries.value.filter { it.id != id }
        _journalEntries.value = updated
        saveJournalEntries(updated)
    }

    // Persistence Helpers
    private fun loadChatHistory(): List<ChatMessage> {
        val jsonStr = prefs.getString("chat_history", null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        sender = obj.getString("sender"),
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveChatHistory(list: List<ChatMessage>) {
        try {
            val array = JSONArray()
            list.takeLast(100).forEach { msg ->
                val obj = JSONObject()
                    .put("sender", msg.sender)
                    .put("text", msg.text)
                    .put("timestamp", msg.timestamp)
                array.put(obj)
            }
            prefs.edit().putString("chat_history", array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadMemories(): List<YaadeinItem> {
        val jsonStr = prefs.getString("memories", null)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<YaadeinItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        YaadeinItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            detail = obj.getString("detail"),
                            category = obj.optString("category", "General"),
                            dateString = obj.optString("dateString", "")
                        )
                    )
                }
                return list
            } catch (_: Exception) {}
        }
        // Defaults
        return listOf(
            YaadeinItem(
                title = "पसंदीदा भाषा",
                detail = "हिंदी और हिंग्लिश (English mixed)",
                category = "Preferences"
            ),
            YaadeinItem(
                title = "लक्ष्य (Goal)",
                detail = "Android विकास और AI टूल्स में निपुण होना",
                category = "Goals"
            ),
            YaadeinItem(
                title = "पर्सनल स्टाइल",
                detail = "सकारात्मक, विनम्र और व्यावहारिक उत्तर पसंद हैं",
                category = "Personality"
            )
        )
    }

    private fun saveMemories(list: List<YaadeinItem>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("detail", item.detail)
                    .put("category", item.category)
                    .put("dateString", item.dateString)
                array.put(obj)
            }
            prefs.edit().putString("memories", array.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadJournalEntries(): List<JournalEntry> {
        val jsonStr = prefs.getString("journal", null)
        if (jsonStr != null) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<JournalEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        JournalEntry(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            mood = obj.optString("mood", "😊"),
                            aiInsight = obj.optString("aiInsight", ""),
                            dateString = obj.optString("dateString", "")
                        )
                    )
                }
                return list
            } catch (_: Exception) {}
        }
        return listOf(
            JournalEntry(
                title = "Maya AI के साथ नई शुरुआत",
                content = "आज मैंने Maya AI असिस्टेंट इंस्टॉल किया। इसकी आवाज़ और बातचीत बहुत प्रेरणादायक है।",
                mood = "🚀 Productive",
                aiInsight = "Maya: हर नई शुरुआत आत्मविश्वास बढ़ाती है। आपके लक्ष्य ज़रूर पूरे होंगे!",
                dateString = "आज, 10:00 am"
            )
        )
    }

    private fun saveJournalEntries(list: List<JournalEntry>) {
        try {
            val array = JSONArray()
            list.forEach { entry ->
                val obj = JSONObject()
                    .put("id", entry.id)
                    .put("title", entry.title)
                    .put("content", entry.content)
                    .put("mood", entry.mood)
                    .put("aiInsight", entry.aiInsight)
                    .put("dateString", entry.dateString)
                array.put(obj)
            }
            prefs.edit().putString("journal", array.toString()).apply()
        } catch (_: Exception) {}
    }
}
