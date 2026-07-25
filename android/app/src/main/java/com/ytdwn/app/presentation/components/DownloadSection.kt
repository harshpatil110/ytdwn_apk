package com.ytdwn.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Download Action Section (Section 6)
 */
@Composable
fun DownloadSection(
    downloadPath: String,
    onDownloadClick: () -> Unit,
    onChangeLocationClick: () -> Unit,
    onOpenFileClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SAVE DESTINATION",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[CHANGE]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onChangeLocationClick() }.padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = downloadPath,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            
            if (isCompleted && onOpenFileClick != null) {
                PrimaryButton(
                    text = "OPEN FILE",
                    onClick = onOpenFileClick,
                    enabled = true,
                    modifier = Modifier.padding(start = 16.dp)
                )
            } else {
                PrimaryButton(
                    text = "DOWNLOAD",
                    onClick = onDownloadClick,
                    enabled = enabled,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
