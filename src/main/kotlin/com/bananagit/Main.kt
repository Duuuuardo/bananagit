package com.bananagit

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.bananagit.i18n.LocalStrings
import com.bananagit.theme.BananaGitTheme
import com.bananagit.ui.dialogs.CloneDialog
import com.bananagit.ui.screens.HomeScreen
import com.bananagit.ui.screens.RepositoryScreen
import com.bananagit.viewmodel.RepositoryViewModel
import com.bananagit.viewmodel.Screen
import com.bananagit.viewmodel.Tab

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val viewModel = remember { RepositoryViewModel() }
    val windowState = rememberWindowState(width = 1100.dp, height = 750.dp)

    Window(
        onCloseRequest = { viewModel.dispose(); exitApplication() },
        title = "BananaGit",
        state = windowState,
    ) {
        val appState = viewModel.state
        val strings = remember(appState.language) { appState.language.strings() }

        BananaGitTheme(darkTheme = appState.isDarkTheme) {
            CompositionLocalProvider(LocalStrings provides strings) {
                Box(
                    modifier = Modifier.fillMaxSize().onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) handleShortcut(event, viewModel) else false
                    }
                ) {
                    when (appState.screen) {
                        Screen.HOME -> HomeScreen(
                            state = appState,
                            onOpenRepository = { viewModel.openRepository(it) },
                            onInitRepository = { viewModel.initRepository(it) },
                            onShowCloneDialog = { viewModel.showCloneDialog() },
                            onOpenRecentRepo = { viewModel.openRepository(it) },
                            onRemoveRecentRepo = { viewModel.removeRecentRepo(it) }
                        )
                        Screen.REPOSITORY -> RepositoryScreen(state = appState, viewModel = viewModel)
                    }

                    if (appState.showCloneDialog) {
                        CloneDialog(
                            cloneUrl = appState.cloneUrl, clonePath = appState.clonePath,
                            onUrlChange = { viewModel.updateCloneUrl(it) },
                            onPathChange = { viewModel.updateClonePath(it) },
                            onClone = { viewModel.cloneRepository() },
                            onDismiss = { viewModel.hideCloneDialog() }
                        )
                    }

                    if (appState.error != null) {
                        Snackbar(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                            action = { TextButton(onClick = { viewModel.clearError() }) { Text(strings.ok) } },
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) { Text(appState.error) }
                    }

                    if (appState.isLoading) {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f)) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.height(16.dp))
                                    Text(strings.loading, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleShortcut(event: KeyEvent, vm: RepositoryViewModel): Boolean {
    val ctrl = event.isCtrlPressed || event.isMetaPressed; val shift = event.isShiftPressed
    return when {
        (ctrl && event.key == Key.R) || event.key == Key.F5 -> { vm.refreshStatus(); true }
        ctrl && shift && event.key == Key.P -> { vm.push(); true }
        ctrl && shift && event.key == Key.L -> { vm.pull(); true }
        ctrl && shift && event.key == Key.S -> { vm.stash(); true }
        ctrl && event.key == Key.One -> { vm.setTab(Tab.STATUS); true }
        ctrl && event.key == Key.Two -> { vm.setTab(Tab.HISTORY); true }
        ctrl && event.key == Key.Three -> { vm.setTab(Tab.BRANCHES); true }
        ctrl && event.key == Key.Four -> { vm.setTab(Tab.DIFF); true }
        ctrl && event.key == Key.T -> { vm.toggleTheme(); true }
        else -> false
    }
}
