package com.bananagit.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bananagit.git.*
import com.bananagit.i18n.Language
import com.bananagit.i18n.Strings
import com.bananagit.persistence.AppSettings
import com.bananagit.persistence.RecentRepo
import com.bananagit.persistence.RecentRepos
import com.bananagit.persistence.Settings
import kotlinx.coroutines.*
import java.io.File

enum class Screen { HOME, REPOSITORY }
enum class Tab { STATUS, HISTORY, BRANCHES, DIFF }

data class AppState(
    val screen: Screen = Screen.HOME,
    val currentTab: Tab = Tab.STATUS,
    val repoPath: String = "",
    val currentBranch: String = "",
    val branches: List<BranchInfo> = emptyList(),
    val files: List<FileChange> = emptyList(),
    val commits: List<CommitInfo> = emptyList(),
    val commitMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val statusMessage: String = "",
    val selectedFiles: Set<String> = emptySet(),
    val cloneUrl: String = "",
    val clonePath: String = "",
    val showCloneDialog: Boolean = false,
    val newBranchName: String = "",
    val showNewBranchDialog: Boolean = false,
    val githubToken: String = "",
    val recentRepos: List<RecentRepo> = emptyList(),
    val isDarkTheme: Boolean = true,
    val diffResult: List<FileDiff> = emptyList(),
    val selectedDiffFile: String? = null,
    val commitSuggestion: String = "",
    val stashEntries: List<StashEntry> = emptyList(),
    val tags: List<TagInfo> = emptyList(),
    val showSettingsDialog: Boolean = false,
    val isAmend: Boolean = false,
    val showTagDialog: Boolean = false,
    val newTagName: String = "",
    val newTagMessage: String = "",
    val language: Language = Language.PT_BR
)

class RepositoryViewModel {
    var state by mutableStateOf(AppState())
        private set

    private var repo: GitRepository? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val recentRepos = RecentRepos()
    private val settings = Settings()
    private var strings: Strings

    init {
        val saved = settings.load()
        val lang = Language.fromCode(saved.language)
        strings = lang.strings()
        state = state.copy(isDarkTheme = saved.isDarkTheme, githubToken = saved.githubToken, recentRepos = recentRepos.load(), language = lang)
    }

    private fun persist() { settings.save(AppSettings(state.isDarkTheme, state.githubToken, state.language.code)) }

    fun goHome() { repo?.close(); repo = null; state = AppState(recentRepos = recentRepos.load(), isDarkTheme = state.isDarkTheme, githubToken = state.githubToken, language = state.language) }

    fun setTab(tab: Tab) {
        state = state.copy(currentTab = tab)
        when (tab) { Tab.STATUS -> refreshStatus(); Tab.HISTORY -> refreshLog(); Tab.BRANCHES -> { refreshBranches(); refreshTags() }; Tab.DIFF -> loadDiff() }
    }

    fun loadRecentRepos() { state = state.copy(recentRepos = recentRepos.load()) }
    fun removeRecentRepo(path: String) { recentRepos.remove(path); state = state.copy(recentRepos = recentRepos.load()) }
    fun toggleTheme() { state = state.copy(isDarkTheme = !state.isDarkTheme); persist() }

    fun showSettings() { state = state.copy(showSettingsDialog = true) }
    fun hideSettings() { state = state.copy(showSettingsDialog = false) }
    fun saveSettings(token: String, lang: Language) { strings = lang.strings(); state = state.copy(githubToken = token, showSettingsDialog = false, language = lang); persist() }

    fun openRepository(path: String) {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val dir = File(path)
                if (!File(dir, ".git").exists()) { state = state.copy(isLoading = false, error = strings.notGitRepo); return@launch }
                repo = GitRepository.open(dir); recentRepos.add(path)
                state = state.copy(screen = Screen.REPOSITORY, repoPath = path, currentBranch = repo!!.getCurrentBranch(), isLoading = false, statusMessage = strings.repoOpened(dir.name))
                refreshStatus(); refreshBranches()
            } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errOpen, e.message)) }
        }
    }

    fun initRepository(path: String) {
        scope.launch {
            state = state.copy(isLoading = true, error = null)
            try {
                val dir = File(path); dir.mkdirs(); repo = GitRepository.init(dir); recentRepos.add(path)
                state = state.copy(screen = Screen.REPOSITORY, repoPath = path, currentBranch = repo!!.getCurrentBranch(), isLoading = false, statusMessage = strings.repoCreated(dir.name))
                refreshStatus()
            } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errCreate, e.message)) }
        }
    }

    fun showCloneDialog() { state = state.copy(showCloneDialog = true) }
    fun hideCloneDialog() { state = state.copy(showCloneDialog = false, cloneUrl = "", clonePath = "") }
    fun updateCloneUrl(url: String) { state = state.copy(cloneUrl = url) }
    fun updateClonePath(path: String) { state = state.copy(clonePath = path) }

    fun cloneRepository() {
        val url = state.cloneUrl; val path = state.clonePath; if (url.isBlank() || path.isBlank()) return
        scope.launch {
            state = state.copy(isLoading = true, error = null, showCloneDialog = false)
            try {
                repo = GitRepository.clone(url, File(path)); recentRepos.add(path)
                state = state.copy(screen = Screen.REPOSITORY, repoPath = path, currentBranch = repo!!.getCurrentBranch(), isLoading = false, statusMessage = strings.repoCloned, cloneUrl = "", clonePath = "")
                refreshStatus(); refreshBranches()
            } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errClone, e.message)) }
        }
    }

    fun toggleFileSelection(path: String) { val s = state.selectedFiles.toMutableSet(); if (path in s) s.remove(path) else s.add(path); state = state.copy(selectedFiles = s) }
    fun selectAllFiles() { state = state.copy(selectedFiles = state.files.filter { !it.isStaged }.map { it.path }.toSet()) }
    fun clearSelection() { state = state.copy(selectedFiles = emptySet()) }

    fun stageSelected() { val p = state.selectedFiles.toList(); if (p.isEmpty()) return; scope.launch { try { repo?.stage(p); state = state.copy(selectedFiles = emptySet(), statusMessage = strings.nStaged(p.size)); refreshStatus(); generateCommitSuggestion() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errStage, e.message)) } } }
    fun stageAll() { scope.launch { try { repo?.stageAll(); state = state.copy(selectedFiles = emptySet(), statusMessage = strings.allStaged); refreshStatus(); generateCommitSuggestion() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errStage, e.message)) } } }
    fun unstageSelected() { val p = state.selectedFiles.toList(); if (p.isEmpty()) return; scope.launch { try { repo?.unstage(p); state = state.copy(selectedFiles = emptySet(), statusMessage = strings.nUnstaged(p.size)); refreshStatus(); generateCommitSuggestion() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errUnstage, e.message)) } } }
    fun unstageAll() { scope.launch { try { repo?.unstageAll(); state = state.copy(selectedFiles = emptySet(), statusMessage = strings.stageCleared); refreshStatus(); generateCommitSuggestion() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errClearStage, e.message)) } } }

    fun discardFile(path: String) { scope.launch { try { repo?.discardChanges(listOf(path)); state = state.copy(statusMessage = strings.discarded(path)); refreshStatus() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errDiscard, e.message)) } } }

    fun updateCommitMessage(message: String) { state = state.copy(commitMessage = message) }
    fun toggleAmend() { val a = !state.isAmend; state = state.copy(isAmend = a); if (a && state.commitMessage.isBlank()) state = state.copy(commitMessage = state.commits.firstOrNull()?.message ?: "") }

    fun commit() {
        val msg = state.commitMessage.trim(); if (msg.isBlank()) { state = state.copy(error = strings.commitMsgRequired); return }
        scope.launch {
            try {
                if (state.isAmend) { repo?.amendCommit(msg); state = state.copy(commitMessage = "", commitSuggestion = "", isAmend = false, statusMessage = strings.amended) }
                else { repo?.commit(msg); state = state.copy(commitMessage = "", commitSuggestion = "", statusMessage = strings.commitDone(msg)) }
                refreshStatus(); refreshLog()
            } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errCommit, e.message)) }
        }
    }

    fun push() { scope.launch { state = state.copy(isLoading = true, error = null); try { repo?.push(state.githubToken.ifBlank { null }); state = state.copy(isLoading = false, statusMessage = strings.pushDone) } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errPush, e.message)) } } }
    fun pull() { scope.launch { state = state.copy(isLoading = true, error = null); try { repo?.pull(state.githubToken.ifBlank { null }); state = state.copy(isLoading = false, statusMessage = strings.pullDone); refreshStatus(); refreshLog() } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errPull, e.message)) } } }

    fun stash() { scope.launch { try { repo?.stash(); state = state.copy(statusMessage = strings.stashDone); refreshStatus(); refreshStash() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errStash, e.message)) } } }
    fun stashPop() { scope.launch { try { repo?.stashPop(); state = state.copy(statusMessage = strings.stashRestored); refreshStatus(); refreshStash() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errStashPop, e.message)) } } }
    fun stashDrop(index: Int) { scope.launch { try { repo?.stashDrop(index); state = state.copy(statusMessage = strings.stashDropped); refreshStash() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errStashDrop, e.message)) } } }
    fun refreshStash() { scope.launch { try { state = state.copy(stashEntries = repo?.stashList() ?: emptyList()) } catch (_: Exception) {} } }

    fun showNewBranchDialog() { state = state.copy(showNewBranchDialog = true, newBranchName = "") }
    fun hideNewBranchDialog() { state = state.copy(showNewBranchDialog = false, newBranchName = "") }
    fun updateNewBranchName(name: String) { state = state.copy(newBranchName = name) }
    fun createBranch() { val n = state.newBranchName.trim(); if (n.isBlank()) return; scope.launch { try { repo?.createAndCheckoutBranch(n); state = state.copy(showNewBranchDialog = false, newBranchName = "", currentBranch = repo?.getCurrentBranch() ?: n, statusMessage = strings.branchCreated(n)); refreshBranches(); refreshStatus() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errNewBranch, e.message)) } } }
    fun checkoutBranch(name: String) { scope.launch { state = state.copy(isLoading = true); try { repo?.checkoutBranch(name); state = state.copy(isLoading = false, currentBranch = name, statusMessage = strings.branchSwitched(name)); refreshStatus(); refreshBranches(); refreshLog() } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errCheckout, e.message)) } } }
    fun deleteBranch(name: String) { scope.launch { try { repo?.deleteBranch(name); state = state.copy(statusMessage = strings.branchRemoved(name)); refreshBranches() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errDelBranch, e.message)) } } }
    fun mergeBranch(name: String) { scope.launch { state = state.copy(isLoading = true, error = null); try { val ok = repo?.mergeBranch(name) ?: false; state = if (ok) state.copy(isLoading = false, statusMessage = strings.mergeDone(name)) else state.copy(isLoading = false, error = strings.mergeFailed(name)); refreshStatus(); refreshLog(); refreshBranches() } catch (e: Exception) { state = state.copy(isLoading = false, error = strings.fmtError(strings.errMerge, e.message)) } } }

    fun showTagDialog() { state = state.copy(showTagDialog = true, newTagName = "", newTagMessage = "") }
    fun hideTagDialog() { state = state.copy(showTagDialog = false, newTagName = "", newTagMessage = "") }
    fun updateNewTagName(name: String) { state = state.copy(newTagName = name) }
    fun updateNewTagMessage(msg: String) { state = state.copy(newTagMessage = msg) }
    fun createTag() { val n = state.newTagName.trim(); if (n.isBlank()) return; scope.launch { try { repo?.createTag(n, state.newTagMessage.ifBlank { null }); state = state.copy(showTagDialog = false, newTagName = "", newTagMessage = "", statusMessage = strings.tagCreated(n)); refreshTags() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errNewTag, e.message)) } } }
    fun deleteTag(name: String) { scope.launch { try { repo?.deleteTag(name); state = state.copy(statusMessage = strings.tagRemoved(name)); refreshTags() } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errDelTag, e.message)) } } }
    fun refreshTags() { scope.launch { try { state = state.copy(tags = repo?.listTags() ?: emptyList()) } catch (_: Exception) {} } }

    fun loadDiff() { scope.launch { try { val s = repo?.getDiff(true) ?: emptyList(); val u = repo?.getDiff(false) ?: emptyList(); val all = s + u; state = state.copy(diffResult = all, selectedDiffFile = all.firstOrNull()?.path) } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errDiff, e.message)) } } }
    fun selectDiffFile(path: String?) { state = state.copy(selectedDiffFile = path) }

    fun generateCommitSuggestion() {
        val staged = state.files.filter { it.isStaged }; if (staged.isEmpty()) { state = state.copy(commitSuggestion = ""); return }
        val added = staged.filter { it.type == FileChangeType.ADDED }; val modified = staged.filter { it.type == FileChangeType.MODIFIED }; val deleted = staged.filter { it.type == FileChangeType.DELETED }
        val suggestion = when {
            staged.size == 1 -> { val f = staged.first(); val n = f.path.substringAfterLast("/"); when (f.type) { FileChangeType.ADDED -> "Add $n"; FileChangeType.MODIFIED -> "Update $n"; FileChangeType.DELETED -> "Remove $n"; else -> "Update $n" } }
            added.size == staged.size -> "Add ${staged.size} files"; deleted.size == staged.size -> "Remove ${staged.size} files"; modified.size == staged.size -> "Update ${staged.size} files"
            else -> { val p = mutableListOf<String>(); if (added.isNotEmpty()) p.add("add ${added.size}"); if (modified.isNotEmpty()) p.add("update ${modified.size}"); if (deleted.isNotEmpty()) p.add("remove ${deleted.size}"); p.joinToString(", ").replaceFirstChar { it.uppercase() } }
        }
        state = state.copy(commitSuggestion = suggestion)
    }

    fun applySuggestion() { if (state.commitSuggestion.isNotBlank()) state = state.copy(commitMessage = state.commitSuggestion) }
    fun updateGithubToken(token: String) { state = state.copy(githubToken = token) }
    fun clearError() { state = state.copy(error = null) }
    fun refreshStatus() { scope.launch { try { state = state.copy(files = repo?.getStatus() ?: emptyList()) } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errStatus, e.message)) } } }
    fun refreshLog() { scope.launch { try { state = state.copy(commits = repo?.getLog() ?: emptyList()) } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errHistory, e.message)) } } }
    fun refreshBranches() { scope.launch { try { state = state.copy(branches = repo?.getBranches() ?: emptyList()) } catch (e: Exception) { state = state.copy(error = strings.fmtError(strings.errBranches, e.message)) } } }
    fun dispose() { repo?.close(); scope.cancel() }
}
