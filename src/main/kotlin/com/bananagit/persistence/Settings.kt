package com.bananagit.persistence

import java.io.File
import java.util.Properties

data class AppSettings(
    val isDarkTheme: Boolean = true,
    val githubToken: String = "",
    val language: String = "pt-BR"
)

class Settings {
    private val configDir = File(System.getProperty("user.home"), ".bananagit")
    private val file = File(configDir, "settings.properties")

    fun load(): AppSettings {
        if (!file.exists()) return AppSettings()
        return try {
            val props = Properties()
            file.inputStream().use { props.load(it) }
            AppSettings(
                isDarkTheme = props.getProperty("darkTheme", "true").toBooleanStrictOrNull() ?: true,
                githubToken = props.getProperty("githubToken", ""),
                language = props.getProperty("language", "pt-BR")
            )
        } catch (e: Exception) { AppSettings() }
    }

    fun save(s: AppSettings) {
        configDir.mkdirs()
        val props = Properties()
        props.setProperty("darkTheme", s.isDarkTheme.toString())
        props.setProperty("githubToken", s.githubToken)
        props.setProperty("language", s.language)
        file.outputStream().use { props.store(it, null) }
    }
}
