package com.bananagit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananagit.i18n.LocalStrings
import com.bananagit.persistence.RecentRepo
import com.bananagit.viewmodel.AppState
import javax.swing.JFileChooser

@Composable
fun HomeScreen(
    state: AppState,
    onOpenRepository: (String) -> Unit,
    onInitRepository: (String) -> Unit,
    onShowCloneDialog: () -> Unit,
    onOpenRecentRepo: (String) -> Unit,
    onRemoveRecentRepo: (String) -> Unit
) {
    val s = LocalStrings.current
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(40.dp))
            Text(s.appName, style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 42.sp), color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(s.slogan, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.widthIn(max = 700.dp)) {
                ActionCard(Modifier.weight(1f), Icons.Default.FolderOpen, s.openRepo, s.openRepoDesc) {
                    val c = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY; dialogTitle = s.chooseFolderOpen }
                    if (c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) onOpenRepository(c.selectedFile.absolutePath)
                }
                ActionCard(Modifier.weight(1f), Icons.Default.CloudDownload, s.cloneRepo, s.cloneRepoDesc, onShowCloneDialog)
                ActionCard(Modifier.weight(1f), Icons.Default.CreateNewFolder, s.newRepo, s.newRepoDesc) {
                    val c = JFileChooser().apply { fileSelectionMode = JFileChooser.DIRECTORIES_ONLY; dialogTitle = s.chooseFolderCreate }
                    if (c.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) onInitRepository(c.selectedFile.absolutePath)
                }
            }

            if (state.recentRepos.isNotEmpty()) {
                Spacer(Modifier.height(32.dp))
                Column(modifier = Modifier.widthIn(max = 700.dp)) {
                    Text(s.recentRepos, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.heightIn(max = 300.dp)) {
                        items(state.recentRepos) { repo ->
                            RecentRepoItem(repo, { onOpenRecentRepo(repo.path) }, { onRemoveRecentRepo(repo.path) })
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Text("made with \u2764 and Kotlin by duuuuardo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RecentRepoItem(repo: RecentRepo, onOpen: () -> Unit, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable(onClick = onOpen).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(repo.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(repo.path, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, LocalStrings.current.remove, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(180.dp).clip(RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, title, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
    }
}
