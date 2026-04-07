package com.bananagit.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bananagit.i18n.Language
import com.bananagit.i18n.LocalStrings

@Composable
fun SettingsDialog(
    currentToken: String,
    currentLanguage: Language,
    onSave: (token: String, language: Language) -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalStrings.current
    var token by remember { mutableStateOf(currentToken) }
    var lang by remember { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.settingsTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(s.githubLabel, style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = token, onValueChange = { token = it },
                    label = { Text(s.tokenLabel) }, placeholder = { Text(s.tokenHint) },
                    singleLine = true, visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(s.tokenHelp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                HorizontalDivider()

                Text(s.languageLabel, style = MaterialTheme.typography.labelLarge)
                Language.entries.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { lang = option }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = lang == option, onClick = { lang = option })
                        Spacer(Modifier.width(8.dp))
                        Text(option.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(token, lang) }) { Text(s.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } }
    )
}
