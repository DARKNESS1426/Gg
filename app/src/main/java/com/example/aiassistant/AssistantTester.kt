package com.example.aiassistant

import android.content.Context
import android.widget.Toast

/**
 * Testing and Debugging Utilities for Development
 * Use these functions to test various features without voice input
 */
object AssistantTester {

    /**
     * Simulate voice command processing for testing
     */
    fun simulateCommand(context: Context, command: String) {
        Toast.makeText(context, "Testing: $command", Toast.LENGTH_SHORT).show()
        AssistantLogger.d("Simulated command: $command")
        AssistantAnalytics.recordCommand(
            command = command,
            duration = 500,
            success = true,
            backend = AssistantConfig.ACTIVE_LLM_BACKEND
        )
    }

    /**
     * Test speech recognition with predefined responses
     */
    fun testSpeechRecognition(context: Context) {
        val testCommands = listOf(
            "What time is it?",
            "Tell me a joke",
            "Open settings",
            "What's the capital of France?",
            "Set a reminder for tomorrow"
        )

        testCommands.forEach { command ->
            simulateCommand(context, command)
        }

        Toast.makeText(context, "Speech recognition test complete", Toast.LENGTH_LONG).show()
    }

    /**
     * Test TTS output
     */
    fun testTTS(context: Context, text: String = "Hello! This is a test of the text to speech system.") {
        Toast.makeText(context, "Testing TTS", Toast.LENGTH_SHORT).show()
        AssistantLogger.d("TTS Test: $text")
    }

    /**
     * Test API connectivity
     */
    suspend fun testAPIConnectivity(context: Context): Boolean {
        return try {
            val testPrompt = "Say 'Test successful' if you receive this"
            val response = when (AssistantConfig.ACTIVE_LLM_BACKEND) {
                "openai" -> LLMClient.queryGPT(testPrompt)
                "gemini" -> LLMClient.queryGemini(testPrompt, AssistantConfig.GEMINI_API_KEY)
                "claude" -> LLMClient.queryClaude(testPrompt, AssistantConfig.CLAUDE_API_KEY)
                "ollama" -> LLMClient.queryOllama(testPrompt)
                else -> "Unknown backend"
            }

            val success = response.isNotEmpty() && !response.contains("error", ignoreCase = true)
            AssistantLogger.i("API Test Result: $response")
            success
        } catch (e: Exception) {
            AssistantLogger.e("API Test Failed", e)
            false
        }
    }

    /**
     * Print system diagnostics
     */
    fun printDiagnostics(context: Context) {
        val diagnostics = buildString {
            append("=== SYSTEM DIAGNOSTICS ===\n")
            append("Active Backend: ${AssistantConfig.ACTIVE_LLM_BACKEND}\n")
            append("API Key Status: ${checkAPIKeyStatus()}\n")
            append("TTS Pitch: ${AssistantConfig.TTS_PITCH}\n")
            append("TTS Speech Rate: ${AssistantConfig.TTS_SPEECH_RATE}\n")
            append("Logging Enabled: ${AssistantConfig.ENABLE_LOGGING}\n")
            append("Local Command Fallback: ${AssistantConfig.ENABLE_LOCAL_COMMAND_FALLBACK}\n")
            append("\n=== STATISTICS ===\n")
            val stats = AssistantAnalytics.getStatistics()
            stats.forEach { (key, value) ->
                append("$key: $value\n")
            }
            append("\n=== RECENT LOGS ===\n")
            AssistantLogger.getLogs().takeLast(10).forEach { log ->
                append("$log\n")
            }
        }

        AssistantLogger.d(diagnostics)
    }

    /**
     * Validate configuration
     */
    fun validateConfiguration(): List<String> {
        return AssistantConfig.validateConfiguration()
    }

    /**
     * Test command parsing
     */
    fun testCommandParsing() {
        val testCommands = listOf(
            "Open settings",
            "Call John",
            "Send message to Sarah",
            "Play music",
            "Search for pizza recipes"
        )

        testCommands.forEach { command ->
            val parsed = CommandParser.parseCommand(command)
            AssistantLogger.d(
                "Parsed: '$command' → Action: ${parsed.action}, Target: ${parsed.target}, " +
                "Confidence: ${parsed.confidence}"
            )
        }
    }

    /**
     * Test utility functions
     */
    fun testUtilityFunctions() {
        val testCases = mapOf(
            "What is AI?" to AssistantUtils.isQuestion("What is AI?"),
            "Open camera" to AssistantUtils.isCommand("Open camera"),
            "1234567890" to (AssistantUtils.extractPhoneNumber("Call 1234567890") != null),
            "test@email.com" to (AssistantUtils.extractEmail("Email test@email.com") != null)
        )

        testCases.forEach { (test, result) ->
            AssistantLogger.d("Utility Test: $test → $result")
        }
    }

    private fun checkAPIKeyStatus(): String {
        return when (AssistantConfig.ACTIVE_LLM_BACKEND) {
            "openai" -> if (AssistantConfig.isValidApiKey(AssistantConfig.OPENAI_API_KEY)) "✓" else "✗"
            "gemini" -> if (AssistantConfig.isValidApiKey(AssistantConfig.GEMINI_API_KEY)) "✓" else "✗"
            "claude" -> if (AssistantConfig.isValidApiKey(AssistantConfig.CLAUDE_API_KEY)) "✓" else "✗"
            "ollama" -> if (AssistantConfig.OLLAMA_URL.isNotEmpty()) "✓" else "✗"
            else -> "Unknown"
        }
    }
}

/**
 * Development Mode - Enables testing without voice input
 */
object DevelopmentMode {

    private var isEnabled = false

    fun enable() {
        isEnabled = true
        AssistantLogger.i("Development mode enabled")
    }

    fun disable() {
        isEnabled = false
        AssistantLogger.i("Development mode disabled")
    }

    fun isEnabled(): Boolean = isEnabled

    /**
     * Simulate a complete assistant workflow
     */
    suspend fun simulateFullWorkflow(context: Context) {
        AssistantLogger.i("Starting full workflow simulation")

        // Step 1: Configuration validation
        val validationErrors = AssistantConfig.validateConfiguration()
        if (validationErrors.isNotEmpty()) {
            AssistantLogger.w("Configuration issues: $validationErrors")
        }

        // Step 2: Test API
        val apiOk = AssistantTester.testAPIConnectivity(context)
        AssistantLogger.i("API connectivity: ${if (apiOk) "OK" else "FAILED"}")

        // Step 3: Test command parsing
        AssistantTester.testCommandParsing()

        // Step 4: Test utilities
        AssistantTester.testUtilityFunctions()

        // Step 5: Print diagnostics
        AssistantTester.printDiagnostics(context)

        AssistantLogger.i("Workflow simulation complete")
    }
}

/**
 * Mock responses for testing without API calls
 */
object MockResponses {

    private val responses = mapOf(
        "time" to "The current time is 2:30 PM",
        "date" to "Today is Thursday, September 4, 2026",
        "joke" to "Why did the AI go to school? To improve its neural network!",
        "weather" to "It's sunny and 72 degrees Fahrenheit",
        "capital" to "The capital of France is Paris",
        "help" to "I can recognize voice commands, answer questions, and help you control your device",
        "hello" to "Hello! How can I assist you today?",
        "thank" to "You're welcome! Happy to help"
    )

    fun getMockResponse(command: String): String? {
        return responses.entries.find { (key, _) -> 
            command.contains(key, ignoreCase = true) 
        }?.value
    }

    fun getAllMockResponses(): Map<String, String> = responses
}

/**
 * Performance monitoring
 */
object PerformanceMonitor {

    private val timings = mutableMapOf<String, MutableList<Long>>()

    fun startTimer(label: String): Long = System.currentTimeMillis()

    fun endTimer(label: String, startTime: Long) {
        val duration = System.currentTimeMillis() - startTime
        timings.getOrPut(label) { mutableListOf() }.add(duration)
        AssistantLogger.d("$label: ${duration}ms")
    }

    fun getAverageTime(label: String): Long {
        val times = timings[label] ?: return 0
        return if (times.isNotEmpty()) times.average().toLong() else 0
    }

    fun printReport() {
        val report = buildString {
            append("=== PERFORMANCE REPORT ===\n")
            timings.forEach { (label, times) ->
                val avg = times.average()
                val min = times.minOrNull() ?: 0
                val max = times.maxOrNull() ?: 0
                append("$label: avg=${avg.toLong()}ms, min=$min, max=$max (${times.size} samples)\n")
            }
        }
        AssistantLogger.d(report)
    }

    fun clear() {
        timings.clear()
    }
}

/**
 * Memory and resource monitoring
 */
object ResourceMonitor {

    fun getMemoryUsage(): Map<String, Long> {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory

        return mapOf(
            "total" to totalMemory / (1024 * 1024),  // MB
            "used" to usedMemory / (1024 * 1024),
            "free" to freeMemory / (1024 * 1024)
        )
    }

    fun printMemoryStatus() {
        val memory = getMemoryUsage()
        val status = buildString {
            append("=== MEMORY STATUS ===\n")
            append("Total: ${memory["total"]}MB\n")
            append("Used: ${memory["used"]}MB\n")
            append("Free: ${memory["free"]}MB\n")
        }
        AssistantLogger.i(status)
    }
}
