package com.bananagit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bananagit.i18n.LocalStrings

@Composable
fun CommitPanel(
    commitMessage: String,
    onMessageChange: (String) -> Unit,
    onCommit: () -> Unit,
    stagedCount: Int,
    commitSuggestion: String = "",
    onUseSuggestion: () -> Unit = {},
    isAmend: Boolean = false,
    onToggleAmend: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = if (isAmend) s.amendCommit else s.commit,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = s.filesReady(stagedCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isAmend,
                    onCheckedChange = { onToggleAmend() },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    s.amendLastCommit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (commitSuggestion.isNotBlank() && commitMessage.isBlank() && !isAmend) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onUseSuggestion,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(commitSuggestion, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = commitMessage,
                onValueChange = onMessageChange,
                label = { Text(s.commitMsgLabel) },
                placeholder = { Text(s.commitMsgHint) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(8.dp),
                maxLines = 8
            )
            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onCommit,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                enabled = commitMessage.isNotBlank() && (stagedCount > 0 || isAmend),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isAmend) s.amendBtn else s.savePoint,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (stagedCount == 0 && !isAmend) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = s.stageFirst,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}
