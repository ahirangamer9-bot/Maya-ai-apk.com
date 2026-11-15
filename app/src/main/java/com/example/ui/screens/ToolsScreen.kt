package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundBlue
import com.example.ui.theme.MayaBlue
import com.example.ui.theme.TextDark

data class ToolItem(
    val id: String,
    val icon: String,
    val name: String,
    val category: String,
    val desc: String
)

@Composable
fun ToolsScreen(
    onSelectTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTools = listOf(
        ToolItem("📚 Padhai", "📚", "Padhai", "Education", "Study assistant & quiz"),
        ToolItem("💻 Coding", "💻", "Coding", "Developer", "Code helper & bug solver"),
        ToolItem("📷 Camera Scan", "📷", "Camera Scan", "Vision", "OCR & photo analyzer"),
        ToolItem("🧠 Yaadein", "🧠", "Yaadein", "Personal", "AI memory vault"),
        ToolItem("🎵 Gaane", "🎵", "Gaane", "Creative", "Music & lyrics writer"),
        ToolItem("📝 Journal", "📝", "Journal", "Personal", "Daily diary & mood tracker"),
        ToolItem("🌤️ Mausam", "🌤️", "Mausam", "Utility", "Live weather & tips"),
        ToolItem("📈 Markets", "📈", "Markets", "Finance", "Indices & investment guides"),
        ToolItem("📢 Marketing", "📢", "Marketing", "Business", "Social captions & ads"),
        ToolItem("📄 Documents", "📄", "Documents", "Productivity", "Summarizer & bullet notes"),
        ToolItem("🖼️ Meri Photos", "🖼️", "Meri Photos", "Media", "Photo gallery & captions"),
        ToolItem("🌐 Browser", "🌐", "Browser", "Utility", "Quick AI web browser"),
        ToolItem("🔐 License", "🔐", "License", "System", "Premium perks & status")
    )

    var selectedFilter by remember { mutableStateOf("All") }
    val categories = listOf("All", "Education", "Developer", "Personal", "Business", "Utility")

    val filteredTools = if (selectedFilter == "All") allTools else allTools.filter { it.category == selectedFilter }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlue)
            .padding(16.dp)
    ) {
        Text(
            text = "Maya AI Tools ⚡",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MayaBlue
        )
        Text(
            text = "स्मार्ट टूल्स का संग्रह (All-in-one AI Suite)",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(14.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(4).forEach { cat ->
                FilterChip(
                    selected = selectedFilter == cat,
                    onClick = { selectedFilter = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MayaBlue,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTools) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTool(tool.id) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(BackgroundBlue, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(tool.icon, fontSize = 22.sp)
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = tool.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = tool.desc,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
