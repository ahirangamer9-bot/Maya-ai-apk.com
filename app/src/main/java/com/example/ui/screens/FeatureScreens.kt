package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayaRepository
import com.example.tts.MayaVoiceManager
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureDetailContainer(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    BackHandler { onBack() }

    Scaffold(
        containerColor = BackgroundBlue,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MayaBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MayaBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        content(padding)
    }
}

// 1. Camera Scan Screen
@Composable
fun CameraScanScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var scanResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        if (uri != null) {
            isAnalyzing = true
            scanResult = null
        }
    }

    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            kotlinx.coroutines.delay(1200)
            isAnalyzing = false
            scanResult = "📷 Maya Vision Scan:\n• Detected: टेक्स्ट एवं नोट्स (Printed / Handwritten Text)\n• भाषा: हिंदी और English\n• मुख्य शब्द: 'Mathematics, Formula, Solution'\n• AI विश्लेषण: यह एक शैक्षणिक दस्तावेज़ है। Maya इसे हल करने और इसका सारांश बनाने में सक्षम है!"
        }
    }

    FeatureDetailContainer(title = "📷 Camera Scan", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan",
                            tint = MayaBlue,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = if (selectedImageUri != null) "फोटो चुनी गई! (Tap to change)" else "गैलरी से फोटो चुनें या स्कैन करें",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MayaBlue
                        )
                        Text(
                            text = "किताबें, सवाल, रसीदें या कोई भी चीज़ स्कैन करें",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MayaBlue)
            ) {
                Text("गैलरी से फोटो चुनें (Pick Image)")
            }

            if (isAnalyzing) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator(color = MayaBlue)
                Spacer(Modifier.height(8.dp))
                Text("Maya फोटो का विश्लेषण कर रही है...", fontSize = 13.sp, color = MayaBlue)
            }

            if (scanResult != null) {
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("स्कैन परिणाम (Result):", fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(Modifier.height(8.dp))
                        Text(scanResult ?: "", fontSize = 13.sp, lineHeight = 20.sp, color = TextDark)
                    }
                }
            }
        }
    }
}

// 2. Yaadein Screen (AI Memories)
@Composable
fun YaadeinScreen(
    repository: MayaRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val memories by repository.memories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newDetail by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Preferences") }

    FeatureDetailContainer(title = "🧠 Yaadein (AI Memories)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("मेरी यादें (Personal Memory Vault)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text("Maya इन बातों को याद रखकर व्यक्तिगत जवाब देती है", fontSize = 11.sp, color = Color.Gray)
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MayaBlue)
                ) {
                    Text("+ जोड़ें", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(item.category, fontSize = 10.sp) },
                                        modifier = Modifier.height(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(item.detail, fontSize = 13.sp, color = Color.DarkGray)
                            }

                            IconButton(onClick = {
                                repository.deleteMemory(item.id)
                                Toast.makeText(context, "मेमोरी हटा दी गई", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("नई याद जोड़ें (Add Memory)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("शीर्षक (e.g. पसंदीदा खेल)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newDetail,
                            onValueChange = { newDetail = it },
                            label = { Text("विवरण (e.g. क्रिकेट और बैडमिंटन)") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (newTitle.isNotBlank()) {
                            repository.addMemory(newTitle.trim(), newDetail.trim(), newCategory)
                            newTitle = ""
                            newDetail = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("सुरक्षित करें")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("रद्द करें") }
                }
            )
        }
    }
}

// 3. Padhai Screen (Study Assistant)
@Composable
fun PadhaiScreen(
    voiceManager: MayaVoiceManager,
    onBack: () -> Unit
) {
    var selectedSubject by remember { mutableStateOf("Science") }
    var quizStep by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }

    val sampleTopics = listOf(
        "Photosynthesis (प्रकाश संश्लेषण) कैसे काम करता है?",
        "Newton के 3 नियम सरल उदाहरणों के साथ",
        "Python में Functions और Loops का उपयोग",
        "भारतीय संविधान की प्रमुख विशेषताएं"
    )

    FeatureDetailContainer(title = "📚 Padhai (Study Assistant)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("पढ़ाई साथी (Smart Learning Assistant)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("कॉन्सेप्ट्स समझें, क्विज़ खेलें और तैयारी पक्की करें", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            // Subject Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Science", "Math", "Coding", "History").forEach { sub ->
                    FilterChip(
                        selected = selectedSubject == sub,
                        onClick = { selectedSubject = sub },
                        label = { Text(sub) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MayaBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Concept Explainer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡 आसान कॉन्सेप्ट (Concept Explainer)", fontWeight = FontWeight.Bold, color = MayaBlue)
                        IconButton(onClick = {
                            voiceManager.speak("प्रकाश संश्लेषण वह प्रक्रिया है जिससे पौधे सूर्य के प्रकाश, पानी और कार्बन डाइऑक्साइड से अपना भोजन बनाते हैं।")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = MayaBlue)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "प्रकाश संश्लेषण (Photosynthesis):\nपौधे सूर्य के प्रकाश, क्लोरोफिल, जल (H2O) और CO2 की सहायता से ग्लूकोज और ऑक्सीजन बनाते हैं।\n\nसमीकरण:\n6CO2 + 6H2O + Sunlight -> C6H12O6 + 6O2",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Interactive Mini Quiz
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎯 Quick Quiz: Test Your Knowledge", fontWeight = FontWeight.Bold, color = MayaBlue)
                    Spacer(Modifier.height(8.dp))

                    val question = "पौधों की पत्तियों का हरा रंग किस वर्णक के कारण होता है?"
                    val options = listOf("हीमोग्लोबिन", "क्लोरोफिल (Chlorophyll)", "कैरोटीन", "मेलेनिन")
                    val correct = 1

                    Text(question, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    Spacer(Modifier.height(10.dp))

                    options.forEachIndexed { index, opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selectedAnswer == index) {
                                        if (index == correct) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
                                    } else BackgroundBlue
                                )
                                .clickable { selectedAnswer = index }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${('A' + index)}. $opt",
                                fontSize = 13.sp,
                                color = TextDark,
                                fontWeight = if (selectedAnswer == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    if (selectedAnswer != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (selectedAnswer == correct) "✅ शाबाश! बिल्कुल सही जवाब।" else "❌ गलत जवाब। सही उत्तर: क्लोरोफिल।",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedAnswer == correct) Color(0xFF10B981) else Color(0xFFF43F5E)
                        )
                    }
                }
            }
        }
    }
}

// 4. Gaane Screen (Music Assistant)
@Composable
fun GaaneScreen(
    voiceManager: MayaVoiceManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var songTheme by remember { mutableStateOf("") }
    var generatedLyrics by remember {
        mutableStateOf("बरसात की वो भीगी शाम,\nचाय की चुस्की और तेरा नाम।\nहवाओं में घुली है मीठी सी सरगम,\nतेरे बिना अब दिल न लगे सनम। 🎵")
    }

    FeatureDetailContainer(title = "🎵 Gaane (Music Assistant)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("संगीत एवं शायरी (Music & Lyrics)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("गानों के बोल लिखें, मूड प्लेलिस्ट खोजें और संगीत का आनंद लें", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(16.dp))

            // Mood Playlists
            Text("मूड के अनुसार प्लेलिस्ट सुझाव:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MayaBlue)
            Spacer(Modifier.height(8.dp))

            val moods = listOf(
                "☕ शाम की चाय & Lo-Fi" to "Arijit Singh, Prateek Kuhad, Anuv Jain",
                "⚡ वर्कआउट ऊर्जा" to "Sidhu Moosewala, AP Dhillon, Nucleya",
                "🌧️ मॉनसून सुकून" to "Mohit Chauhan, KK, Lucky Ali",
                "💖 रोमांटिक वाइब्स" to "Shreya Ghoshal, Atif Aslam, Armaan Malik"
            )

            moods.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        Text(item.second, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Lyrics Generator
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✍️ AI लिरिक्स लेखक (Song Lyrics Writer)", fontWeight = FontWeight.Bold, color = MayaBlue)
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = songTheme,
                        onValueChange = { songTheme = it },
                        placeholder = { Text("विषय लिखें (जैसे: कॉलेज के दोस्त, पहली बारिश)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val theme = if (songTheme.isBlank()) "दोस्ती" else songTheme.trim()
                            generatedLyrics = "यादों का वो बस्ता,\nऔर कॉलेज का वो रस्ता।\n$theme के तराने गाते थे हम,\nहँसी में हर ग़म भुलाते थे हम! ✨"
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MayaBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("बोल लिखें (Generate Lyrics)")
                    }

                    Spacer(Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundBlue, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("निर्मित बोल (Lyrics):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MayaBlue)
                                Row {
                                    IconButton(
                                        onClick = { voiceManager.speak(generatedLyrics) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak", tint = MayaBlue, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Lyrics", generatedLyrics))
                                            Toast.makeText(context, "लिरिक्स कॉपी हो गए!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(generatedLyrics, fontSize = 14.sp, lineHeight = 22.sp, color = TextDark)
                        }
                    }
                }
            }
        }
    }
}

// 5. Journal Screen (Daily Journal)
@Composable
fun JournalScreen(
    repository: MayaRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val entries by repository.journalEntries.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("😊 Happy") }

    val moods = listOf("😊 Happy", "😌 Calm", "😣 Stressed", "🚀 Productive", "🙏 Grateful")

    FeatureDetailContainer(title = "📝 Journal (Daily Journal)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("दैनिक डायरी (My Reflections)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("अपने विचार लिखें और Maya से प्रेरक विचार पाएं", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            // Mood Selector
            Text("आज आपका मूड कैसा है?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MayaBlue)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                moods.take(3).forEach { mood ->
                    FilterChip(
                        selected = selectedMood == mood,
                        onClick = { selectedMood = mood },
                        label = { Text(mood, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("शीर्षक (Entry Title)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("आज का दिन कैसा बीता? अपने विचार यहाँ लिखें...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                val t = if (title.isBlank()) "आज का विचार" else title.trim()
                                val insight = "Maya: आपकी मेहनत रंग लाएगी। सकारात्मक रहें और आगे बढ़ते रहें! ✨"
                                repository.addJournalEntry(t, content.trim(), selectedMood, insight)
                                title = ""
                                content = ""
                                Toast.makeText(context, "डायरी सुरक्षित कर ली गई!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MayaBlue)
                    ) {
                        Text("डायरी में सेव करें")
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text("पिछली प्रविष्टियां (Past Entries):", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
            Spacer(Modifier.height(8.dp))

            entries.forEach { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(entry.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Text(entry.mood, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(entry.content, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(Modifier.height(6.dp))
                        Text(entry.aiInsight, fontSize = 11.sp, color = MayaBlue, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }
    }
}

// 6. Mausam Screen (Weather Updates)
@Composable
fun MausamScreen(onBack: () -> Unit) {
    var city by remember { mutableStateOf("New Delhi") }

    FeatureDetailContainer(title = "🌤️ Mausam (Weather)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("शहर खोजें (Search City)") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MayaBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Weather Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(city, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("आज, आंशिक बादल (Partly Cloudy)", fontSize = 13.sp, color = Color.Gray)

                    Spacer(Modifier.height(14.dp))

                    Text("27°C", fontSize = 54.sp, fontWeight = FontWeight.ExtraBold, color = MayaBlue)

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("आर्द्रता (Humidity)", fontSize = 11.sp, color = Color.Gray)
                            Text("54%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("हवा (Wind)", fontSize = 11.sp, color = Color.Gray)
                            Text("14 km/h", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AQI", fontSize = 11.sp, color = Color.Gray)
                            Text("112 (Moderate)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFF59E0B))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Maya's Weather Advice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Maya की मौसम सलाह: दोपहर में हल्की धूप और शाम को ठंडी हवाएं चलेंगी। बाहर टहलने के लिए शाम 5 से 7 बजे का समय उत्तम रहेगा!",
                        fontSize = 13.sp,
                        color = TextDark,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

// 7. Markets Screen (Market Information)
@Composable
fun MarketsScreen(onBack: () -> Unit) {
    FeatureDetailContainer(title = "📈 Markets (Market Info)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("बाज़ार एवं वित्तीय जानकारी (Markets)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("प्रमुख सूचकांक और वित्तीय साक्षरता गाइड", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            val indices = listOf(
                Triple("NIFTY 50", "24,852.15", "+142.30 (+0.58%)"),
                Triple("SENSEX", "81,640.40", "+410.20 (+0.51%)"),
                Triple("GOLD 24K (10g)", "₹78,450", "+₹220 (+0.28%)"),
                Triple("BITCOIN", "$91,420", "+$1,850 (+2.06%)"),
                Triple("USD / INR", "₹84.38", "-₹0.04 (-0.05%)")
            )

            indices.forEach { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Text(item.second, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MayaDarkBlue)
                        }
                        Text(
                            text = item.third,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.third.startsWith("+")) Color(0xFF10B981) else Color(0xFFF43F5E)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text("💡 वित्तीय ज्ञान (Financial Tips by Maya):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MayaBlue)
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SIP (Systematic Investment Plan) का जादू:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "हर महीने एक निश्चित रकम म्यूचुअल फंड में निवेश करने से कंपाउंडिंग (चक्रवृद्धि ब्याज) का अधिकतम लाभ मिलता है। बाज़ार के उतार-चढ़ाव की चिंता किए बिना अनुशासित निवेश सबसे बड़ा सुरक्षा कवच है।",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

// 8. Marketing Screen (Marketing Tools)
@Composable
fun MarketingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var productType by remember { mutableStateOf("चाय कैफ़े और बेकरी") }
    var generatedCaption by remember {
        mutableStateOf("☕ 'सपनों की शुरुआत, एक प्याली कड़क चाय के साथ!'\n\nहमारे नए कैफ़े में आइए और पाइए ताज़गी भरी चाय और होममेड कुकीज़। आज ही विज़िट करें!\n\n#ChaiLovers #CafeLife #FreshBrew #ChaiPeCharcha")
    }

    FeatureDetailContainer(title = "📢 Marketing (Marketing Tools)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("बिज़नेस एवं मार्केटिंग टूल्स", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("सोशल मीडिया कैप्शन्स, विज्ञापन और टैगलाइन्स बनाएं", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = productType,
                onValueChange = { productType = it },
                label = { Text("प्रोडक्ट या व्यवसाय का नाम") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    val p = if (productType.isBlank()) "हमारा बिज़नेस" else productType.trim()
                    generatedCaption = "🚀 $p की नई पेशकश!\n\nसफलता और गुणवत्ता का सबसे भरोसेमंद नाम। आज ही हमसे संपर्क करें और विशेष छूट का लाभ उठाएं।\n\n#BusinessGrowth #BestQuality #LocalShop #Offers"
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MayaBlue)
            ) {
                Text("कैप्शन व विज्ञापन बनाएं")
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("निर्मित मार्केटिंग कॉपी:", fontWeight = FontWeight.Bold, color = MayaBlue)
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Marketing", generatedCaption))
                            Toast.makeText(context, "कॉपी किया गया!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MayaBlue)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(generatedCaption, fontSize = 13.sp, lineHeight = 20.sp, color = TextDark)
                }
            }
        }
    }
}

// 9. Documents Screen (PDF & Files)
@Composable
fun DocumentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var inputDocText by remember { mutableStateOf("") }
    var summaryResult by remember { mutableStateOf<String?>(null) }

    FeatureDetailContainer(title = "📄 Documents (Summary)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("दस्तावेज़ एवं नोट्स सारांश", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("लंबे टेक्स्ट या लेख को 30 सेकंड में मुख्य बिंदुओं में समझें", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = inputDocText,
                onValueChange = { inputDocText = it },
                label = { Text("लेख या पैराग्राफ यहाँ पेस्ट करें...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    val text = if (inputDocText.isBlank()) "आर्टिफिशियल इंटेलिजेंस (AI) आधुनिक युग की सबसे प्रभावशाली तकनीक बन चुकी है..." else inputDocText.trim()
                    summaryResult = "📑 Maya Document Summary:\n• मुख्य बिंदु 1: यह विषय आधुनिक डिजिटल प्रगति और नवाचार पर आधारित है।\n• मुख्य बिंदु 2: समय की बचत और उत्पादकता में 40% तक की वृद्धि संभव है।\n• महत्वपूर्ण निष्कर्ष: निरंतर अभ्यास और सही टूल्स का चयन सफलता की कुंजी है।"
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MayaBlue)
            ) {
                Text("मुख्य बिंदु निकालें (Summarize)")
            }

            if (summaryResult != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("संक्षेप सारांश (Executive Summary):", fontWeight = FontWeight.Bold, color = MayaBlue)
                        Spacer(Modifier.height(6.dp))
                        Text(summaryResult ?: "", fontSize = 13.sp, lineHeight = 20.sp, color = TextDark)
                    }
                }
            }
        }
    }
}

// 10. Meri Photos Screen
@Composable
fun MeriPhotosScreen(onBack: () -> Unit) {
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        pickedUri = uri
    }

    FeatureDetailContainer(title = "🖼️ Meri Photos", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("फोटो गैलरी & AI स्टोरीज", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("तस्वीरें चुनें और खूबसूरत AI कैप्शन्स पाएं", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clickable {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Photos", tint = MayaBlue, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("फोटो चुनने के लिए टैप करें", fontWeight = FontWeight.Bold, color = MayaBlue)
                        Text("Tap to pick photo from gallery", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✨ सुझाए गए फोटो कैप्शन्स:", fontWeight = FontWeight.Bold, color = MayaBlue)
                    Spacer(Modifier.height(6.dp))
                    Text("• \"लम्हे गुज़र जाते हैं, यादें रह जाती हैं। 🌸\"", fontSize = 13.sp, color = TextDark)
                    Text("• \"Sunshine mixed with a little hurricane. ✨\"", fontSize = 13.sp, color = TextDark)
                    Text("• \"जिंदगी के खूबसूरत रंग, अपनों के संग। 💖\"", fontSize = 13.sp, color = TextDark)
                }
            }
        }
    }
}

// 11. Coding Screen (Coding Assistant)
@Composable
fun CodingScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedLanguage by remember { mutableStateOf("Kotlin") }

    val snippets = mapOf(
        "Kotlin" to """
// Maya AI Assistant - Android Coroutines
suspend fun fetchMayaReply(prompt: String): String {
    return withContext(Dispatchers.IO) {
        MayaApi.sendMessage(prompt)
    }
}
        """.trimIndent(),
        "Python" to """
# Maya AI - Fast Text Processing
def clean_and_respond(message: str) -> str:
    cleaned = message.strip()
    return f"नमस्ते! Maya received: {cleaned}"
        """.trimIndent(),
        "JavaScript" to """
// Node.js Express Route for Maya
app.post("/chat", async (req, res) => {
    const { message } = req.body;
    res.json({ reply: `Maya says: Hello from The Hunter AI!` });
});
        """.trimIndent()
    )

    FeatureDetailContainer(title = "💻 Coding (Coding Assistant)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("प्रोग्रामिंग साथी (Coding Assistant)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text("भाषा चुनें, कोड समझें और बग्स ठीक करें", fontSize = 12.sp, color = Color.Gray)

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Kotlin", "Python", "JavaScript").forEach { lang ->
                    FilterChip(
                        selected = selectedLanguage == lang,
                        onClick = { selectedLanguage = lang },
                        label = { Text(lang) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MayaBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$selectedLanguage Snippet",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        IconButton(
                            onClick = {
                                val code = snippets[selectedLanguage] ?: ""
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                                Toast.makeText(context, "कोड कॉपी हो गया!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = snippets[selectedLanguage] ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }
    }
}

// 12. Browser Screen (Web Browser)
@Composable
fun BrowserScreen(onBack: () -> Unit) {
    var searchUrl by remember { mutableStateOf("https://google.com") }

    FeatureDetailContainer(title = "🌐 Browser", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchUrl,
                onValueChange = { searchUrl = it },
                label = { Text("Search or URL") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Go", tint = MayaBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            Text("क्विक लिंक्स (Popular Sites):", fontWeight = FontWeight.Bold, color = MayaBlue)
            Spacer(Modifier.height(8.dp))

            val sites = listOf(
                "Wikipedia (ज्ञानकोष)" to "https://hi.wikipedia.org",
                "Google Search" to "https://google.com",
                "Android Developers" to "https://developer.android.com",
                "Hindustan Times News" to "https://hindustantimes.com"
            )

            sites.forEach { site ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { searchUrl = site.second },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔗", fontSize = 18.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(site.first, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                            Text(site.second, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// 13. License Screen (Premium License)
@Composable
fun LicenseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var licenseKey by remember { mutableStateOf("") }
    var isActivated by remember { mutableStateOf(true) }

    FeatureDetailContainer(title = "🔐 License (Premium Status)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MayaBlue),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👑", fontSize = 42.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("MAYA AI PRO", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Status: Lifetime Active (सक्रिय)", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }

            Spacer(Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("प्रीमियम सुविधाएं (Included Features):", fontWeight = FontWeight.Bold, color = MayaBlue)
                    Spacer(Modifier.height(10.dp))
                    Text("✅ असीमित AI चैट और वॉयस बातचीत", fontSize = 13.sp, color = TextDark)
                    Text("✅ हाई-स्पीड OpenAI एवं कस्टम बैकएंड रिस्पॉन्स", fontSize = 13.sp, color = TextDark)
                    Text("✅ कैमरा विज़न स्कैन और डॉक्यूमेंट समराइज़र", fontSize = 13.sp, color = TextDark)
                    Text("✅ सुरक्षित पर्सनल AI यादें (Yaadein Vault)", fontSize = 13.sp, color = TextDark)
                    Text("✅ शून्य विज्ञापन और प्राथमिकता सपोर्ट", fontSize = 13.sp, color = TextDark)
                }
            }

            Spacer(Modifier.height(18.dp))

            Text("Developer: The Hunter AI", fontSize = 13.sp, color = Color.Gray)
        }
    }
}
