package com.bananagit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananagit.git.DiffLine
import com.bananagit.git.DiffLineType
import com.bananagit.git.FileDiff
import com.bananagit.i18n.LocalStrings
import com.bananagit.theme.BananaGreen
import com.bananagit.theme.BananaRed

@Composable
fun DiffView(
    diffs: List<FileDiff>,
    selectedFile: String?,
    onSelectFile: (String) -> Unit
) {
    val s = LocalStrings.current
    Row(modifier = Modifier.fillMaxSize()) {
        // file list
        Surface(
            modifier = Modifier.width(250.dp).fillMaxHeight(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(diffs) { diff ->
                    DiffFileItem(
                        diff = diff,
                        isSelected = diff.path == selectedFile,
                        onClick = { onSelectFile(diff.path) }
                    )
                }
            }
        }

        // diff content
        val activeDiff = diffs.find { it.path == selectedFile }
        if (activeDiff != null) {
            DiffContent(activeDiff, modifier = Modifier.weight(1f).fillMaxHeight())
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    s.selectFile,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun DiffFileItem(diff: FileDiff, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = diff.path.substringAfterLast("/"),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (diff.additions > 0) {
            Text(
                "+${diff.additions}",
                style = MaterialTheme.typography.labelSmall,
                color = BananaGreen
            )
        }
        if (diff.deletions > 0) {
            Spacer(Modifier.width(4.dp))
            Text(
                "-${diff.deletions}",
                style = MaterialTheme.typography.labelSmall,
                color = BananaRed
            )
        }
    }
}

@Composable
private fun DiffContent(diff: FileDiff, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = diff.path,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.weight(1f))
            Text(
                "+${diff.additions}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = BananaGreen
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "-${diff.deletions}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = BananaRed
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            val horizontalScroll = rememberScrollState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
                    .padding(8.dp)
            ) {
                items(diff.lines) { line ->
                    DiffLineRow(line)
                }
            }
        }
    }
}

@Composable
private fun DiffLineRow(line: DiffLine) {
    val bgColor = when (line.type) {
        DiffLineType.ADD -> BananaGreen.copy(alpha = 0.12f)
        DiffLineType.REMOVE -> BananaRed.copy(alpha = 0.12f)
        DiffLineType.HEADER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        DiffLineType.CONTEXT -> Color.Transparent
    }
    val textColor = when (line.type) {
        DiffLineType.ADD -> BananaGreen
        DiffLineType.REMOVE -> BananaRed
        DiffLineType.HEADER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        DiffLineType.CONTEXT -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 1.dp)
    ) {
        Text(
            text = line.content,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            ),
            color = textColor
        )
    }
}
