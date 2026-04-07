package com.bananagit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bananagit.git.BranchInfo
import com.bananagit.git.TagInfo
import com.bananagit.i18n.LocalStrings
import com.bananagit.theme.*
import com.bananagit.ui.components.BranchSelector
import com.bananagit.ui.components.CommitPanel
import com.bananagit.ui.components.DiffView
import com.bananagit.ui.components.FileStatusList
import com.bananagit.ui.components.Sidebar
import com.bananagit.ui.dialogs.SettingsDialog
import com.bananagit.viewmodel.AppState
import com.bananagit.viewmodel.RepositoryViewModel
import com.bananagit.viewmodel.Tab
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RepositoryScreen(
    state: AppState,
    viewModel: RepositoryViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            repoPath = state.repoPath,
            currentBranch = state.currentBranch,
            branches = state.branches,
            isDarkTheme = state.isDarkTheme,
            stashCount = state.stashEntries.size,
            onGoHome = { viewModel.goHome() },
            onCheckoutBranch = { viewModel.checkoutBranch(it) },
            onNewBranch = { viewModel.showNewBranchDialog() },
            onPush = { viewModel.push() },
            onPull = { viewModel.pull() },
            onRefresh = { viewModel.refreshStatus() },
            onToggleTheme = { viewModel.toggleTheme() },
            onStash = { viewModel.stash() },
            onStashPop = { viewModel.stashPop() },
            onSettings = { viewModel.showSettings() }
        )

        Row(modifier = Modifier.weight(1f)) {
            Sidebar(
                currentTab = state.currentTab,
                onTabSelected = { viewModel.setTab(it) },
                stagedCount = state.files.count { it.isStaged },
                unstagedCount = state.files.count { !it.isStaged }
            )

            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                color = MaterialTheme.colorScheme.background
            ) {
                when (state.currentTab) {
                    Tab.STATUS -> StatusTab(state, viewModel)
                    Tab.HISTORY -> HistoryTab(state)
                    Tab.BRANCHES -> BranchesTab(state, viewModel)
                    Tab.DIFF -> DiffTab(state, viewModel)
                }
            }
        }

        StatusBar(statusMessage = state.statusMessage)

        if (state.showNewBranchDialog) {
            NewBranchDialog(
                branchName = state.newBranchName,
                onNameChange = { viewModel.updateNewBranchName(it) },
                onCreate = { viewModel.createBranch() },
                onDismiss = { viewModel.hideNewBranchDialog() }
            )
        }
        if (state.showTagDialog) {
            TagDialog(
                tagName = state.newTagName,
                tagMessage = state.newTagMessage,
                onNameChange = { viewModel.updateNewTagName(it) },
                onMessageChange = { viewModel.updateNewTagMessage(it) },
                onCreate = { viewModel.createTag() },
                onDismiss = { viewModel.hideTagDialog() }
            )
        }
        if (state.showSettingsDialog) {
            SettingsDialog(
                currentToken = state.githubToken,
                currentLanguage = state.language,
                onSave = { token, lang -> viewModel.saveSettings(token, lang) },
                onDismiss = { viewModel.hideSettings() }
            )
        }
    }
}

@Composable
private fun TopBar(
    repoPath: String,
    currentBranch: String,
    branches: List<BranchInfo>,
    isDarkTheme: Boolean,
    stashCount: Int,
    onGoHome: () -> Unit,
    onCheckoutBranch: (String) -> Unit,
    onNewBranch: () -> Unit,
    onPush: () -> Unit,
    onPull: () -> Unit,
    onRefresh: () -> Unit,
    onToggleTheme: () -> Unit,
    onStash: () -> Unit,
    onStashPop: () -> Unit,
    onSettings: () -> Unit
) {
    val s = LocalStrings.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGoHome) {
                Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = repoPath.substringAfterLast("/").substringAfterLast("\\"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(max = 200.dp),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(16.dp))
            BranchSelector(
                currentBranch = currentBranch,
                branches = branches.filter { !it.isRemote },
                onCheckoutBranch = onCheckoutBranch,
                onNewBranch = onNewBranch
            )
            Spacer(Modifier.weight(1f))

            // Stash
            FilledTonalButton(onClick = onStash, modifier = Modifier.padding(horizontal = 2.dp)) {
                Icon(Icons.Default.Archive, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(s.stashSave)
            }
            if (stashCount > 0) {
                FilledTonalButton(onClick = onStashPop, modifier = Modifier.padding(horizontal = 2.dp)) {
                    Icon(Icons.Default.Unarchive, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(s.stashRestore(stashCount))
                }
            }

            Spacer(Modifier.width(8.dp))

            FilledTonalButton(onClick = onPull, modifier = Modifier.padding(horizontal = 2.dp)) {
                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(s.pullAction)
            }
            FilledTonalButton(onClick = onPush, modifier = Modifier.padding(horizontal = 2.dp)) {
                Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(s.pushAction)
            }
            IconButton(onClick = onToggleTheme) {
                Icon(
                    if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    s.toggleTheme, tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, s.settings, tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, s.refresh, tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun StatusTab(state: AppState, viewModel: RepositoryViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        FileStatusList(
            files = state.files,
            selectedFiles = state.selectedFiles,
            onToggleFile = { viewModel.toggleFileSelection(it) },
            onSelectAll = { viewModel.selectAllFiles() },
            onClearSelection = { viewModel.clearSelection() },
            onStageSelected = { viewModel.stageSelected() },
            onStageAll = { viewModel.stageAll() },
            onUnstageSelected = { viewModel.unstageSelected() },
            onUnstageAll = { viewModel.unstageAll() },
            onDiscardFile = { viewModel.discardFile(it) },
            modifier = Modifier.weight(1f).fillMaxHeight()
        )
        CommitPanel(
            commitMessage = state.commitMessage,
            onMessageChange = { viewModel.updateCommitMessage(it) },
            onCommit = { viewModel.commit() },
            stagedCount = state.files.count { it.isStaged },
            commitSuggestion = state.commitSuggestion,
            onUseSuggestion = { viewModel.applySuggestion() },
            isAmend = state.isAmend,
            onToggleAmend = { viewModel.toggleAmend() },
            modifier = Modifier.width(320.dp).fillMaxHeight()
        )
    }
}

@Composable
private fun HistoryTab(state: AppState) {
    val s = LocalStrings.current
    if (state.commits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.noCommits, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.commits) { commit ->
                CommitCard(commit.shortHash, commit.message, commit.author, commit.date)
            }
        }
    }
}

@Composable
private fun CommitCard(shortHash: String, message: String, author: String, date: Long) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.of("pt", "BR")) }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(shortHash, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(message, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$author  ${dateFormat.format(Date(date))}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun BranchesTab(state: AppState, viewModel: RepositoryViewModel) {
    val s = LocalStrings.current
    val local = state.branches.filter { !it.isRemote }
    val remote = state.branches.filter { it.isRemote }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Branches header
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s.branches, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                FilledTonalButton(onClick = { viewModel.showNewBranchDialog() }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(s.newBranch)
                }
            }
        }
        if (local.isNotEmpty()) {
            item { Text(s.localLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), modifier = Modifier.padding(top = 8.dp)) }
            items(local) { branch ->
                BranchCard(branch,
                    onCheckout = { viewModel.checkoutBranch(branch.name) },
                    onDelete = if (!branch.isCurrent) {{ viewModel.deleteBranch(branch.name) }} else null,
                    onMerge = if (!branch.isCurrent) {{ viewModel.mergeBranch(branch.name) }} else null
                )
            }
        }
        if (remote.isNotEmpty()) {
            item { Text(s.remoteLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), modifier = Modifier.padding(top = 16.dp)) }
            items(remote) { branch -> BranchCard(branch, onCheckout = { viewModel.checkoutBranch(branch.name) }, onDelete = null, onMerge = null) }
        }

        // Tags section
        item {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(s.tags, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                FilledTonalButton(onClick = { viewModel.showTagDialog() }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text(s.newTag)
                }
            }
        }
        if (state.tags.isEmpty()) {
            item {
                Text(s.noTags, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            items(state.tags) { tag ->
                TagCard(tag, onDelete = { viewModel.deleteTag(tag.name) })
            }
        }
    }
}

@Composable
private fun BranchCard(branch: BranchInfo, onCheckout: () -> Unit, onDelete: (() -> Unit)?, onMerge: (() -> Unit)?) {
    val s = LocalStrings.current
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = if (branch.isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surfaceVariant
    )) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (branch.isCurrent) Icons.Default.CheckCircle else Icons.Default.AccountTree, null,
                tint = if (branch.isCurrent) BananaGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(branch.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (branch.isCurrent) FontWeight.Bold else FontWeight.Normal),
                color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (branch.isCurrent) {
                Surface(shape = MaterialTheme.shapes.small, color = BananaGreen.copy(alpha = 0.2f)) {
                    Text(s.currentLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = BananaGreen)
                }
            }
            if (!branch.isCurrent && !branch.isRemote) {
                TextButton(onClick = onCheckout) { Text(s.switchLabel, color = MaterialTheme.colorScheme.primary) }
            }
            if (onMerge != null) {
                TextButton(onClick = onMerge) { Text(s.mergeLabel, color = MaterialTheme.colorScheme.tertiary) }
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, s.remove, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}

@Composable
private fun TagCard(tag: TagInfo, onDelete: () -> Unit) {
    val s = LocalStrings.current
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalOffer, null, tint = BananaOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(tag.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            if (tag.hash != null) {
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(tag.hash, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(8.dp))
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, s.remove, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun DiffTab(state: AppState, viewModel: RepositoryViewModel) {
    val s = LocalStrings.current
    if (state.diffResult.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.noDiff, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    } else {
        DiffView(diffs = state.diffResult, selectedFile = state.selectedDiffFile, onSelectFile = { viewModel.selectDiffFile(it) })
    }
}

@Composable
private fun StatusBar(statusMessage: String) {
    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 2.dp) {
        Text(statusMessage, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}

@Composable
private fun NewBranchDialog(branchName: String, onNameChange: (String) -> Unit, onCreate: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text(s.newBranch) },
        text = { OutlinedTextField(value = branchName, onValueChange = onNameChange, label = { Text(s.branchNameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { Button(onClick = onCreate) { Text(s.create) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } })
}

@Composable
private fun TagDialog(tagName: String, tagMessage: String, onNameChange: (String) -> Unit, onMessageChange: (String) -> Unit, onCreate: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text(s.newTag) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = tagName, onValueChange = onNameChange, label = { Text(s.tagNameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tagMessage, onValueChange = onMessageChange, label = { Text(s.tagMsgLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { Button(onClick = onCreate, enabled = tagName.isNotBlank()) { Text(s.create) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s.cancel) } })
}
