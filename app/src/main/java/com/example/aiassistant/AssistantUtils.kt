package com.example.aiassistant

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Logging utility for the assistant
 * Handles both console and file logging
 */
object AssistantLogger {

    private const val TAG = "VoiceAssistant"
    private val logs = mutableListOf<String>()
    private val maxLogsInMemory = 500

    fun d(message: String) {
        if (AssistantConfig.ENABLE_LOGGING) {
            Log.d(TAG, message)
            addLog("DEBUG", message)
        }
    }

    fun e(message: String, exception: Exception? = null) {
        Log.e(TAG, message, exception)
        addLog("ERROR", "$message ${exception?.message ?: ""}")
    }

    fun i(message: String) {
        Log.i(TAG, message)
        addLog("INFO", message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
        addLog("WARN", message)
    }

    private fun addLog(level: String, message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logEntry = "[$timestamp] [$level] $message"
        
        logs.add(logEntry)
        if (logs.size > maxLogsInMemory) {
            logs.removeAt(0)
        }
    }

    fun getLogs(): List<String> = logs.toList()

    fun clearLogs() {
        logs.clear()
    }
}

/**
 * Analytics tracker for user interactions
 */
object AssistantAnalytics {

    private data class CommandRecord(
        val command: String,
        val timestamp: Long,
        val duration: Long,
        val success: Boolean,
        val backend: String
    )

    private val commandHistory = mutableListOf<CommandRecord>()
    private val maxHistorySize = 1000

    fun recordCommand(
        command: String,
        duration: Long,
        success: Boolean,
        backend: String = AssistantConfig.ACTIVE_LLM_BACKEND
    ) {
        val record = CommandRecord(
            command = command,
            timestamp = System.currentTimeMillis(),
            duration = duration,
            success = success,
            backend = backend
        )
        
        commandHistory.add(record)
        if (commandHistory.size > maxHistorySize) {
            commandHistory.removeAt(0)
        }

        AssistantLogger.d("Command recorded: $command (${duration}ms) - Success: $success")
    }

    fun getStatistics(): Map<String, Any> {
        val totalCommands = commandHistory.size
        val successfulCommands = commandHistory.count { it.success }
        val failedCommands = totalCommands - successfulCommands
        val averageDuration = if (commandHistory.isNotEmpty()) {
            commandHistory.map { it.duration }.average().toLong()
        } else {
            0L
        }

        val backendUsage = commandHistory.groupingBy { it.backend }
            .eachCount()

        return mapOf(
            "totalCommands" to totalCommands,
            "successfulCommands" to successfulCommands,
            "failedCommands" to failedCommands,
            "successRate" to if (totalCommands > 0) (successfulCommands * 100 / totalCommands) else 0,
            "averageDuration" to averageDuration,
            "backendUsage" to backendUsage
        )
    }

    fun getCommandHistory(): List<String> {
        return commandHistory.map { record ->
            val status = if (record.success) "✓" else "✗"
            "$status [${record.backend}] ${record.command} (${record.duration}ms)"
        }
    }

    fun clearHistory() {
        commandHistory.clear()
    }
}

/**
 * Utility functions for common tasks
 */
object AssistantUtils {

    /**
     * Format duration in milliseconds to readable string
     */
    fun formatDuration(millis: Long): String {
        return when {
            millis < 1000 -> "${millis}ms"
            millis < 60000 -> "${millis / 1000}s"
            else -> "${millis / 60000}m ${(millis % 60000) / 1000}s"
        }
    }

    /**
     * Check if text appears to be a question
     */
    fun isQuestion(text: String): Boolean {
        return text.trim().endsWith("?") || 
               text.startsWith("what", ignoreCase = true) ||
               text.startsWith("how", ignoreCase = true) ||
               text.startsWith("why", ignoreCase = true) ||
               text.startsWith("when", ignoreCase = true) ||
               text.startsWith("where", ignoreCase = true) ||
               text.startsWith("who", ignoreCase = true) ||
               text.startsWith("which", ignoreCase = true)
    }

    /**
     * Check if text is a command (starts with action verb)
     */
    fun isCommand(text: String): Boolean {
        val commandVerbs = listOf(
            "open", "close", "start", "stop", "call", "send", "show", 
            "play", "pause", "next", "previous", "set", "enable", "disable"
        )
        return commandVerbs.any { text.startsWith(it, ignoreCase = true) }
    }

    /**
     * Extract phone number or email from text
     */
    fun extractPhoneNumber(text: String): String? {
        val phonePattern = """\b\d{10}\b|\b\d{3}-\d{3}-\d{4}\b|\b\(\d{3}\)\s?\d{3}-\d{4}\b""".toRegex()
        return phonePattern.find(text)?.value
    }

    fun extractEmail(text: String): String? {
        val emailPattern = """([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)""".toRegex()
        return emailPattern.find(text)?.value
    }

    /**
     * Sanitize user input for safety
     */
    fun sanitizeInput(input: String): String {
        return input.trim()
            .replace(Regex("[^a-zA-Z0-9\\s?.,!-]"), "")
            .take(500) // Limit length
    }

    /**
     * Generate a response context from user command
     */
    fun buildContext(command: String): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val isQuestion = isQuestion(command)
        val isCommand = isCommand(command)
        
        return buildString {
            append("Command: $command\n")
            append("Type: ")
            when {
                isQuestion -> append("Question")
                isCommand -> append("Command")
                else -> append("Statement")
            }
            append("\nTime: $timestamp")
        }
    }
}

/**
 * Settings manager for persistent user preferences
 */
class AssistantSettingsManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("assistant_prefs", Context.MODE_PRIVATE)

    fun setApiKey(backend: String, key: String) {
        prefs.edit().putString("api_key_$backend", key).apply()
    }

    fun getApiKey(backend: String): String? {
        return prefs.getString("api_key_$backend", null)
    }

    fun setVoicePitch(pitch: Float) {
        prefs.edit().putFloat("voice_pitch", pitch).apply()
    }

    fun getVoicePitch(): Float {
        return prefs.getFloat("voice_pitch", AssistantConfig.TTS_PITCH)
    }

    fun setVoiceSpeed(speed: Float) {
        prefs.edit().putFloat("voice_speed", speed).apply()
    }

    fun getVoiceSpeed(): Float {
        return prefs.getFloat("voice_speed", AssistantConfig.TTS_SPEECH_RATE)
    }

    fun setActiveLLM(backend: String) {
        prefs.edit().putString("active_llm", backend).apply()
    }

    fun getActiveLLM(): String {
        return prefs.getString("active_llm", AssistantConfig.ACTIVE_LLM_BACKEND) 
            ?: AssistantConfig.ACTIVE_LLM_BACKEND
    }

    fun setListeningEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("listening_enabled", enabled).apply()
    }

    fun isListeningEnabled(): Boolean {
        return prefs.getBoolean("listening_enabled", true)
    }

    fun setSpeakingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("speaking_enabled", enabled).apply()
    }

    fun isSpeakingEnabled(): Boolean {
        return prefs.getBoolean("speaking_enabled", true)
    }

    fun setLastCommand(command: String) {
        prefs.edit().putString("last_command", command).apply()
    }

    fun getLastCommand(): String? {
        return prefs.getString("last_command", null)
    }

    fun incrementCommandCount() {
        val count = prefs.getInt("command_count", 0)
        prefs.edit().putInt("command_count", count + 1).apply()
    }

    fun getCommandCount(): Int {
        return prefs.getInt("command_count", 0)
    }

    fun clearAllSettings() {
        prefs.edit().clear().apply()
    }
}

/**
 * Command parser for natural language processing
 */
object CommandParser {

    data class ParsedCommand(
        val action: String,
        val target: String?,
        val parameters: Map<String, String>,
        val confidence: Float
    )

    fun parseCommand(text: String): ParsedCommand {
        val normalizedText = text.lowercase().trim()
        
        val (action, target, params) = when {
            normalizedText.contains("open") -> {
                val target = extractTarget(text, "open")
                Triple("open", target, mapOf("app" to target))
            }
            normalizedText.contains("call") -> {
                val target = extractTarget(text, "call")
                Triple("call", target, mapOf("contact" to target))
            }
            normalizedText.contains("send") -> {
                val target = extractTarget(text, "send")
                Triple("send", target, mapOf("recipient" to target))
            }
            normalizedText.contains("play") -> {
                val target = extractTarget(text, "play")
                Triple("play", target, mapOf("media" to target))
            }
            normalizedText.contains("search") || normalizedText.contains("find") -> {
                val target = extractTarget(text, "search")
                Triple("search", target, mapOf("query" to target))
            }
            else -> Triple("query", null, mapOf())
        }

        return ParsedCommand(
            action = action,
            target = target,
            parameters = params,
            confidence = calculateConfidence(normalizedText, action)
        )
    }

    private fun extractTarget(text: String, keyword: String): String? {
        val pattern = "$keyword\\s+(.+?)(?:please|thanks|now|and)?\\s*$".toRegex(RegexOption.IGNORE_CASE)
        return pattern.find(text)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun calculateConfidence(text: String, action: String): Float {
        return when {
            text.contains("please") || text.contains("can you") -> 0.95f
            text.contains("might") || text.contains("could") -> 0.75f
            else -> 0.85f
        }
    }
}
