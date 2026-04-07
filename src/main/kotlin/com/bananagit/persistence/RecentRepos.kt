package com.bananagit.persistence

import java.io.File

data class RecentRepo(
    val path: String,
    val name: String,
    val lastOpened: Long
)

class RecentRepos {

    private val configDir = File(System.getProperty("user.home"), ".bananagit")
    private val file = File(configDir, "recent_repos.txt")
    private val maxEntries = 10

    fun load(): List<RecentRepo> {
        if (!file.exists()) return emptyList()
        return try {
            file.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("\t", limit = 2)
                    if (parts.size == 2) {
                        val path = parts[1]
                        val timestamp = parts[0].toLongOrNull() ?: return@mapNotNull null
                        val dir = File(path)
                        if (dir.exists() && File(dir, ".git").exists()) {
                            RecentRepo(path, dir.name, timestamp)
                        } else null
                    } else null
                }
                .sortedByDescending { it.lastOpened }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(path: String) {
        val entries = load().toMutableList()
        entries.removeAll { it.path == path }
        val dir = File(path)
        entries.add(0, RecentRepo(path, dir.name, System.currentTimeMillis()))
        if (entries.size > maxEntries) {
            entries.subList(maxEntries, entries.size).clear()
        }
        save(entries)
    }

    fun remove(path: String) {
        val entries = load().toMutableList()
        entries.removeAll { it.path == path }
        save(entries)
    }

    private fun save(entries: List<RecentRepo>) {
        configDir.mkdirs()
        file.writeText(
            entries.joinToString("\n") { "${it.lastOpened}\t${it.path}" }
        )
    }
}
