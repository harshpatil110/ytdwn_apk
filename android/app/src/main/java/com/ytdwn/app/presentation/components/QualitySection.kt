package com.ytdwn.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Quality List Section (Sections 4 & 5)
 */
@Composable
fun QualitySection(
    title: String,
    items: List<QualityItemUiModel>,
    selectedId: String?,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        items.forEach { item ->
            QualityCard(
                title = item.title,
                subtitle = item.subtitle,
                isSelected = item.id == selectedId,
                onClick = { onItemSelected(item.id) },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (items.isEmpty()) {
            Text(
                text = "No options available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

// Simple model just for the UI presentation
data class QualityItemUiModel(
    val id: String,
    val title: String,
    val subtitle: String
)
