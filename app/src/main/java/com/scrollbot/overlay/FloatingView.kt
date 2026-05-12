package com.scrollbot.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scrollbot.data.AppTarget

@Composable
fun FloatingView(
    onSearch: (String, AppTarget) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf(AppTarget.LAZADA) }

    if (expanded) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search...", color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2C2C2E),
                    unfocusedContainerColor = Color(0xFF2C2C2E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTarget.entries.forEach { app ->
                    FilterChip(
                        selected = selectedApp == app,
                        onClick = { selectedApp = app },
                        label = { Text(app.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        expanded = false
                        onSearch(query, selectedApp)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scan")
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color(0xFF0A84FF), RoundedCornerShape(26.dp))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text("🔍", style = MaterialTheme.typography.titleMedium)
        }
    }
}
