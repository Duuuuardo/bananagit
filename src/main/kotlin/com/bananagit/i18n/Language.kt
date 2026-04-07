package com.bananagit.i18n

import androidx.compose.runtime.staticCompositionLocalOf

val LocalStrings = staticCompositionLocalOf<Strings> { StringsPtBr() }

enum class Language(val code: String, val label: String) {
    PT_BR("pt-BR", "Portugues (BR)"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String): Language = entries.find { it.code == code } ?: PT_BR
    }

    fun strings(): Strings = when (this) {
        PT_BR -> StringsPtBr()
        EN -> StringsEn()
    }
}

interface Strings {
    val appName: String
    val slogan: String
    val loading: String
    val ok: String
    val create: String
    val cancel: String
    val save: String
    val remove: String

    val openRepo: String
    val openRepoDesc: String
    val cloneRepo: String
    val cloneRepoDesc: String
    val newRepo: String
    val newRepoDesc: String
    val recentRepos: String
    val chooseFolderOpen: String
    val chooseFolderCreate: String

    val tabStatus: String
    val tabHistory: String
    val tabBranches: String
    val tabDiff: String

    val stashSave: String
    fun stashRestore(count: Int): String
    val pullAction: String
    val pushAction: String
    val toggleTheme: String
    val settings: String
    val refresh: String

    val staged: String
    val unstaged: String
    val unstageAll: String
    val selectAll: String
    val stageAllBtn: String
    val noStagedFiles: String
    val noModifiedFiles: String
    fun stageN(count: Int): String
    fun unstageN(count: Int): String
    val clearSelection: String
    val discard: String
    val fileNew: String
    val fileModified: String
    val fileDeleted: String
    val fileUntracked: String
    val fileConflict: String

    val commit: String
    val amendCommit: String
    val amendLastCommit: String
    fun filesReady(count: Int): String
    val commitMsgLabel: String
    val commitMsgHint: String
    val savePoint: String
    val amendBtn: String
    val stageFirst: String

    val branches: String
    val newBranch: String
    val newBranchEllipsis: String
    val localLabel: String
    val remoteLabel: String
    val currentLabel: String
    val switchLabel: String
    val mergeLabel: String
    val branchNameLabel: String

    val tags: String
    val newTag: String
    val noTags: String
    val tagNameLabel: String
    val tagMsgLabel: String

    val noDiff: String
    val selectFile: String
    val noCommits: String

    val cloneTitle: String
    val repoUrlLabel: String
    val repoUrlHint: String
    val saveToLabel: String
    val saveToHint: String
    val chooseSaveFolder: String
    val cloneBtn: String

    val settingsTitle: String
    val githubLabel: String
    val tokenLabel: String
    val tokenHint: String
    val tokenHelp: String
    val languageLabel: String

    fun repoOpened(name: String): String
    fun repoCreated(name: String): String
    val repoCloned: String
    fun nStaged(count: Int): String
    val allStaged: String
    fun nUnstaged(count: Int): String
    val stageCleared: String
    fun discarded(path: String): String
    fun commitDone(msg: String): String
    val amended: String
    val pushDone: String
    val pullDone: String
    val stashDone: String
    val stashRestored: String
    val stashDropped: String
    fun branchCreated(name: String): String
    fun branchSwitched(name: String): String
    fun branchRemoved(name: String): String
    fun mergeDone(name: String): String
    fun mergeFailed(name: String): String
    fun tagCreated(name: String): String
    fun tagRemoved(name: String): String
    val commitMsgRequired: String
    val notGitRepo: String
    fun fmtError(action: String, detail: String?): String
    val errOpen: String
    val errCreate: String
    val errClone: String
    val errStage: String
    val errUnstage: String
    val errClearStage: String
    val errDiscard: String
    val errCommit: String
    val errPush: String
    val errPull: String
    val errStash: String
    val errStashPop: String
    val errStashDrop: String
    val errNewBranch: String
    val errCheckout: String
    val errDelBranch: String
    val errMerge: String
    val errNewTag: String
    val errDelTag: String
    val errStatus: String
    val errHistory: String
    val errBranches: String
    val errDiff: String
}
