package com.bananagit.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bananagit.i18n.LocalStrings
import javax.swing.JFileChooser

@Composable
fun CloneDialog(
    cloneUrl: String,
    clonePath: String,
    onUrlChange: (String) -> Unit,
    onPathChange: (String) -> Unit,
    onClone: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.cloneTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cloneUrl,
                    onValueChange = onUrlChange,
                    label = { Text(s.repoUrlLabel) },
                    placeholder = { Text(s.repoUrlHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clonePath,
                        onValueChange = onPathChange,
                        label = { Text(s.saveToLabel) },
                        placeholder = { Text(s.saveToHint) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val chooser = JFileChooser().apply {
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                                dialogTitle = s.chooseSaveFolder
                            }
                            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                                onPathChange(chooser.selectedFile.absolutePath)
                            }
                        }
                    ) {
                        Icon(Icons.Default.FolderOpen, s.chooseSaveFolder)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onClone, enabled = cloneUrl.isNotBlank() && clonePath.isNotBlank()) {
                Text(s.cloneBtn)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        }
    )
}
