package com.example.aiassistant

/**
 * Configuration file for AI Assistant
 * Update these values with your own API keys and settings
 */
object AssistantConfig {

    // ==================== AI BACKEND SELECTION ====================
    // Set which AI backend to use: "openai", "gemini", "claude", or "ollama"
    const val ACTIVE_LLM_BACKEND = "openai"

    // ==================== OPENAI CONFIGURATION ====================
    const val OPENAI_API_KEY = "sk-your-actual-api-key-here"
    const val OPENAI_MODEL = "gpt-3.5-turbo"
    const val OPENAI_MAX_TOKENS = 150
    const val OPENAI_TEMPERATURE = 0.7f

    // ==================== GOOGLE GEMINI CONFIGURATION ====================
    const val GEMINI_API_KEY = "your-gemini-api-key-here"
    const val GEMINI_MODEL = "gemini-pro"

    // ==================== ANTHROPIC CLAUDE CONFIGURATION ====================
    const val CLAUDE_API_KEY = "your-claude-api-key-here"
    const val CLAUDE_MODEL = "claude-3-sonnet-20240229"
    const val CLAUDE_MAX_TOKENS = 150

    // ==================== OLLAMA CONFIGURATION ====================
    const val OLLAMA_URL = "http://192.168.1.100:11434/api/generate"  // Replace with your device IP
    const val OLLAMA_MODEL = "llama2"
    const val OLLAMA_TIMEOUT_SECONDS = 60

    // ==================== SPEECH & VOICE SETTINGS ====================
    // TTS (Text-to-Speech) Settings
    const val TTS_LANGUAGE = "en-US"
    const val TTS_PITCH = 1.0f      // Range: 0.5 (lower) to 2.0 (higher)
    const val TTS_SPEECH_RATE = 1.0f // Range: 0.5 (slower) to 2.0 (faster)

    // Speech Recognition Settings
    const val SPEECH_LANGUAGE = "en-US"
    const val SPEECH_PARTIAL_RESULTS = true
    const val SPEECH_MAX_RESULTS = 1
    const val SPEECH_TIMEOUT_MILLIS = 10000  // How long to listen for

    // ==================== SERVICE BEHAVIOR ====================
    // Notification Settings
    const val NOTIFICATION_CHANNEL_ID = "assistant_service_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Voice Assistant"
    const val NOTIFICATION_ID = 1

    // Listening Behavior
    const val RESUME_LISTENING_DELAY_MS = 2000L  // Delay before resuming listening
    const val ERROR_RETRY_DELAY_MS = 1000L
    const val FIRST_LISTEN_DELAY_MS = 1000L

    // ==================== FEATURE FLAGS ====================
    const val ENABLE_PROACTIVE_CHAT = false  // Enable periodic chatter
    const val ENABLE_LOGGING = true          // Enable detailed logging
    const val ENABLE_LOCAL_COMMAND_FALLBACK = true  // Use local commands if LLM fails

    // ==================== NETWORK SETTINGS ====================
    const val CONNECT_TIMEOUT_SECONDS = 30
    const val READ_TIMEOUT_SECONDS = 30
    const val WRITE_TIMEOUT_SECONDS = 30

    // ==================== SYSTEM COMMANDS ====================
    // Keywords that trigger local system commands instead of LLM
    val LOCAL_COMMAND_KEYWORDS = listOf(
        "time", "date", "battery", "open", "call", "message", "reminder", "weather"
    )

    // ==================== HELPER FUNCTIONS ====================
    fun getActiveLLMBackend(): String = ACTIVE_LLM_BACKEND

    fun isValidApiKey(key: String): Boolean {
        return key.isNotEmpty() && key != "your-" && !key.contains("here")
    }

    fun validateConfiguration(): List<String> {
        val errors = mutableListOf<String>()

        when (ACTIVE_LLM_BACKEND) {
            "openai" -> {
                if (!isValidApiKey(OPENAI_API_KEY)) {
                    errors.add("OpenAI API key is not configured properly")
                }
            }
            "gemini" -> {
                if (!isValidApiKey(GEMINI_API_KEY)) {
                    errors.add("Gemini API key is not configured properly")
                }
            }
            "claude" -> {
                if (!isValidApiKey(CLAUDE_API_KEY)) {
                    errors.add("Claude API key is not configured properly")
                }
            }
            "ollama" -> {
                if (OLLAMA_URL.isEmpty()) {
                    errors.add("Ollama URL is not configured")
                }
            }
            else -> {
                errors.add("Invalid LLM backend: $ACTIVE_LLM_BACKEND")
            }
        }

        if (TTS_PITCH < 0.5f || TTS_PITCH > 2.0f) {
            errors.add("TTS pitch must be between 0.5 and 2.0")
        }

        if (TTS_SPEECH_RATE < 0.5f || TTS_SPEECH_RATE > 2.0f) {
            errors.add("TTS speech rate must be between 0.5 and 2.0")
        }

        return errors
    }
}
