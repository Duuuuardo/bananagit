package com.bananagit.git

enum class FileChangeType {
    ADDED,
    MODIFIED,
    DELETED,
    UNTRACKED,
    CONFLICTING
}

data class FileChange(
    val path: String,
    val type: FileChangeType,
    val isStaged: Boolean
)

data class CommitInfo(
    val hash: String,
    val shortHash: String,
    val message: String,
    val author: String,
    val date: Long
)

data class BranchInfo(
    val name: String,
    val isCurrent: Boolean,
    val isRemote: Boolean
)

enum class DiffLineType {
    CONTEXT,
    ADD,
    REMOVE,
    HEADER
}

data class DiffLine(
    val content: String,
    val type: DiffLineType
)

data class FileDiff(
    val path: String,
    val lines: List<DiffLine>,
    val additions: Int,
    val deletions: Int
)

data class StashEntry(
    val index: Int,
    val message: String
)

data class TagInfo(
    val name: String,
    val hash: String?
)
