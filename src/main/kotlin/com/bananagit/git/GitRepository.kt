package com.bananagit.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.ByteArrayOutputStream
import java.io.File

class GitRepository private constructor(
    private val git: Git
) : AutoCloseable {

    val workTree: File get() = git.repository.workTree

    companion object {
        fun open(path: File): GitRepository {
            val repo = FileRepositoryBuilder()
                .setGitDir(File(path, ".git"))
                .readEnvironment()
                .findGitDir()
                .build()
            return GitRepository(Git(repo))
        }

        fun clone(url: String, directory: File): GitRepository {
            val git = Git.cloneRepository()
                .setURI(url)
                .setDirectory(directory)
                .call()
            return GitRepository(git)
        }

        fun init(directory: File): GitRepository {
            val git = Git.init()
                .setDirectory(directory)
                .setInitialBranch("main")
                .call()
            return GitRepository(git)
        }
    }

    fun getStatus(): List<FileChange> {
        val status = git.status().call()
        val changes = mutableListOf<FileChange>()

        status.added.forEach {
            changes.add(FileChange(it, FileChangeType.ADDED, isStaged = true))
        }
        status.changed.forEach {
            changes.add(FileChange(it, FileChangeType.MODIFIED, isStaged = true))
        }
        status.removed.forEach {
            changes.add(FileChange(it, FileChangeType.DELETED, isStaged = true))
        }

        status.modified.forEach {
            changes.add(FileChange(it, FileChangeType.MODIFIED, isStaged = false))
        }
        status.untracked.forEach {
            changes.add(FileChange(it, FileChangeType.UNTRACKED, isStaged = false))
        }
        status.missing.forEach {
            changes.add(FileChange(it, FileChangeType.DELETED, isStaged = false))
        }
        status.conflicting.forEach {
            changes.add(FileChange(it, FileChangeType.CONFLICTING, isStaged = false))
        }

        return changes.sortedBy { it.path }
    }

    fun stage(paths: List<String>) {
        val addCommand = git.add()
        paths.forEach { addCommand.addFilepattern(it) }
        addCommand.call()
    }

    fun stageAll() {
        git.add().addFilepattern(".").call()
        git.add().addFilepattern(".").setUpdate(true).call()
    }

    fun unstage(paths: List<String>) {
        val resetCommand = git.reset()
        paths.forEach { resetCommand.addPath(it) }
        resetCommand.call()
    }

    fun unstageAll() {
        git.reset().call()
    }

    fun commit(message: String) {
        git.commit()
            .setMessage(message)
            .call()
    }

    fun push(token: String? = null) {
        val pushCommand = git.push()
        if (token != null) {
            pushCommand.setCredentialsProvider(
                UsernamePasswordCredentialsProvider(token, "")
            )
        }
        pushCommand.call()
    }

    fun pull(token: String? = null) {
        val pullCommand = git.pull()
        if (token != null) {
            pullCommand.setCredentialsProvider(
                UsernamePasswordCredentialsProvider(token, "")
            )
        }
        pullCommand.call()
    }

    fun getBranches(): List<BranchInfo> {
        val currentBranch = getCurrentBranch()

        val localBranches = git.branchList().call().map { ref ->
            val name = Repository.shortenRefName(ref.name)
            BranchInfo(
                name = name,
                isCurrent = name == currentBranch,
                isRemote = false
            )
        }

        val remoteBranches = git.branchList()
            .setListMode(ListBranchCommand.ListMode.REMOTE)
            .call()
            .map { ref ->
                BranchInfo(
                    name = Repository.shortenRefName(ref.name),
                    isCurrent = false,
                    isRemote = true
                )
            }

        return localBranches + remoteBranches
    }

    fun getCurrentBranch(): String {
        return git.repository.branch ?: "HEAD"
    }

    fun createBranch(name: String) {
        git.branchCreate()
            .setName(name)
            .call()
    }

    fun checkoutBranch(name: String) {
        git.checkout()
            .setName(name)
            .call()
    }

    fun createAndCheckoutBranch(name: String) {
        git.checkout()
            .setCreateBranch(true)
            .setName(name)
            .call()
    }

    fun deleteBranch(name: String) {
        git.branchDelete()
            .setBranchNames(name)
            .setForce(true)
            .call()
    }

    fun getLog(maxCount: Int = 50): List<CommitInfo> {
        return try {
            git.log()
                .setMaxCount(maxCount)
                .call()
                .map { commit ->
                    CommitInfo(
                        hash = commit.name,
                        shortHash = commit.name.take(7),
                        message = commit.shortMessage,
                        author = commit.authorIdent.name,
                        date = commit.commitTime.toLong() * 1000
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getRemoteUrl(): String? {
        return git.repository.config.getString("remote", "origin", "url")
    }

    fun getDiff(staged: Boolean = false): List<FileDiff> {
        val diffs = git.diff().setCached(staged).call()
        return diffs.map { entry ->
            val baos = ByteArrayOutputStream()
            val formatter = DiffFormatter(baos)
            formatter.setRepository(git.repository)
            formatter.format(entry)
            formatter.flush()
            val text = baos.toString("UTF-8")
            formatter.close()
            parseDiffOutput(text, entry.newPath ?: entry.oldPath)
        }
    }

    fun getFileDiff(path: String, staged: Boolean = false): FileDiff? {
        return getDiff(staged).find { it.path == path }
    }

    private fun parseDiffOutput(raw: String, path: String): FileDiff {
        var additions = 0
        var deletions = 0
        val diffLines = mutableListOf<DiffLine>()

        for (line in raw.lines()) {
            when {
                line.startsWith("diff ") || line.startsWith("index ") ||
                line.startsWith("---") || line.startsWith("+++") ||
                line.startsWith("@@") -> {
                    diffLines.add(DiffLine(line, DiffLineType.HEADER))
                }
                line.startsWith("+") -> {
                    additions++
                    diffLines.add(DiffLine(line, DiffLineType.ADD))
                }
                line.startsWith("-") -> {
                    deletions++
                    diffLines.add(DiffLine(line, DiffLineType.REMOVE))
                }
                else -> {
                    diffLines.add(DiffLine(line, DiffLineType.CONTEXT))
                }
            }
        }

        return FileDiff(path, diffLines, additions, deletions)
    }

    fun stash(message: String? = null) {
        val cmd = git.stashCreate()
        if (message != null) {
            cmd.setWorkingDirectoryMessage(message)
        }
        cmd.call()
    }

    fun stashPop() {
        git.stashApply().call()
        git.stashDrop().setStashRef(0).call()
    }

    fun stashList(): List<StashEntry> {
        return try {
            git.stashList().call().mapIndexed { index, commit ->
                StashEntry(index, commit.shortMessage)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun stashDrop(index: Int) {
        git.stashDrop().setStashRef(index).call()
    }

    fun listTags(): List<TagInfo> {
        return try {
            git.tagList().call().map { ref ->
                TagInfo(
                    name = ref.name.removePrefix("refs/tags/"),
                    hash = ref.objectId?.name?.take(7)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun createTag(name: String, message: String? = null) {
        val cmd = git.tag().setName(name)
        if (message != null) {
            cmd.setMessage(message)
        }
        cmd.call()
    }

    fun deleteTag(name: String) {
        git.tagDelete().setTags(name).call()
    }

    fun discardChanges(paths: List<String>) {
        val cmd = git.checkout()
        paths.forEach { cmd.addPath(it) }
        cmd.call()
    }

    fun amendCommit(message: String) {
        git.commit()
            .setMessage(message)
            .setAmend(true)
            .call()
    }

    fun mergeBranch(branchName: String): Boolean {
        val ref = git.repository.resolve(branchName)
            ?: throw IllegalArgumentException("Branch not found: $branchName")
        val result = git.merge()
            .include(ref)
            .setFastForward(MergeCommand.FastForwardMode.FF)
            .call()
        return result.mergeStatus.isSuccessful
    }

    override fun close() {
        git.close()
    }
}
