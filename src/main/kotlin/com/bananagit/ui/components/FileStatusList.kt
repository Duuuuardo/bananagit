package com.bananagit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bananagit.git.FileChange
import com.bananagit.git.FileChangeType
import com.bananagit.i18n.LocalStrings
import com.bananagit.theme.*

@Composable
fun FileStatusList(
    files: List<FileChange>,
    selectedFiles: Set<String>,
    onToggleFile: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onStageSelected: () -> Unit,
    onStageAll: () -> Unit,
    onUnstageSelected: () -> Unit,
    onUnstageAll: () -> Unit,
    onDiscardFile: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val s = LocalStrings.current
    val staged = files.filter { it.isStaged }
    val unstaged = files.filter { !it.isStaged }

    Column(modifier = modifier.padding(16.dp)) {
        SectionHeader(title = s.staged, count = staged.size) {
            if (staged.isNotEmpty()) {
                TextButton(onClick = onUnstageAll) { Text(s.unstageAll, style = MaterialTheme.typography.labelSmall) }
            }
        }
        if (staged.isEmpty()) {
            EmptySection(s.noStagedFiles)
        } else {
            LazyColumn(modifier = Modifier.weight(1f, fill = false).heightIn(max = 250.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(staged) { file -> FileItem(file, selectedFiles.contains(file.path), { onToggleFile(file.path) }) }
            }
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        Spacer(Modifier.height(16.dp))

        SectionHeader(title = s.unstaged, count = unstaged.size) {
            if (unstaged.isNotEmpty()) {
                TextButton(onClick = onSelectAll) { Text(s.selectAll, style = MaterialTheme.typography.labelSmall) }
                TextButton(onClick = onStageAll) { Text(s.stageAllBtn, style = MaterialTheme.typography.labelSmall) }
            }
        }
        if (unstaged.isEmpty()) {
            EmptySection(s.noModifiedFiles)
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(unstaged) { file ->
                    FileItemWithDiscard(
                        file = file,
                        isSelected = selectedFiles.contains(file.path),
                        onToggle = { onToggleFile(file.path) },
                        onDiscard = { onDiscardFile(file.path) }
                    )
                }
            }
        }

        if (selectedFiles.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onStageSelected, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp)); Text(s.stageN(selectedFiles.size))
                }
                OutlinedButton(onClick = onUnstageSelected, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp)); Text(s.unstageN(selectedFiles.size))
                }
                TextButton(onClick = onClearSelection) { Text(s.clearSelection) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int, actions: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("$title ($count)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.weight(1f))
        actions()
    }
}

@Composable
private fun EmptySection(message: String) {
    Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

@Composable
private fun FileItem(file: FileChange, isSelected: Boolean, onToggle: () -> Unit) {
    val s = LocalStrings.current
    val (statusColor, statusLabel, statusText) = fileDisplayInfo(file, s)
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() }, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        StatusBadge(statusLabel, statusColor)
        Spacer(Modifier.width(8.dp))
        Text(file.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor.copy(alpha = 0.8f))
    }
}

@Composable
private fun FileItemWithDiscard(file: FileChange, isSelected: Boolean, onToggle: () -> Unit, onDiscard: () -> Unit) {
    val s = LocalStrings.current
    val (statusColor, statusLabel, statusText) = fileDisplayInfo(file, s)
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable(onClick = onToggle).padding(start = 10.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { onToggle() }, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        StatusBadge(statusLabel, statusColor)
        Spacer(Modifier.width(8.dp))
        Text(file.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor.copy(alpha = 0.8f))
        if (file.type != FileChangeType.UNTRACKED) {
            IconButton(onClick = onDiscard, modifier = Modifier.size(28.dp)) {
                Icon(Icons.AutoMirrored.Filled.Undo, s.discard, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(4.dp), color = color.copy(alpha = 0.2f)) {
        Text(label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

private data class FileDisplayInfo(val color: androidx.compose.ui.graphics.Color, val label: String, val text: String)

private fun fileDisplayInfo(file: FileChange, s: com.bananagit.i18n.Strings): FileDisplayInfo = when (file.type) {
    FileChangeType.ADDED -> FileDisplayInfo(BananaGreen, "A", s.fileNew)
    FileChangeType.MODIFIED -> FileDisplayInfo(BananaOrange, "M", s.fileModified)
    FileChangeType.DELETED -> FileDisplayInfo(BananaRed, "D", s.fileDeleted)
    FileChangeType.UNTRACKED -> FileDisplayInfo(BananaBlue, "?", s.fileUntracked)
    FileChangeType.CONFLICTING -> FileDisplayInfo(BananaPurple, "!", s.fileConflict)
}
