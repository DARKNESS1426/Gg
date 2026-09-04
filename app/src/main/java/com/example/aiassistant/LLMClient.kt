package com.example.aiassistant

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LLMClient {

    private const val TAG = "LLMClient"

    // Replace with your actual API key
    private const val OPENAI_API_KEY = "your_openai_api_key_here"
    private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"

    // Alternative: Use Ollama for local LLM (free, runs locally)
    private const val OLLAMA_URL = "http://localhost:11434/api/generate"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Query OpenAI GPT for a response
     * Requires: OpenAI API key in OPENAI_API_KEY
     */
    suspend fun queryGPT(prompt: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val requestBody = JSONObject().apply {
                put("model", "gpt-3.5-turbo")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are a helpful voice assistant. Keep responses concise and conversational. Aim for 1-2 sentences.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 150)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer $OPENAI_API_KEY")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "No response from API"
                val jsonResponse = JSONObject(responseBody)
                val message = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                Log.d(TAG, "GPT Response: $message")
                message
            } else {
                Log.e(TAG, "API Error: ${response.code}")
                "Sorry, I'm having trouble connecting to the AI service. Please check your internet and API key."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying GPT", e)
            "I encountered an error: ${e.message}"
        }
    }

    /**
     * Query Ollama (local LLM - free alternative)
     * Requires: Ollama running locally on port 11434
     * Download from: https://ollama.ai
     * Run: ollama run llama2
     */
    suspend fun queryOllama(prompt: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val requestBody = JSONObject().apply {
                put("model", "llama2")
                put("prompt", prompt)
                put("stream", false)
                put("temperature", 0.7)
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(OLLAMA_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "No response from Ollama"
                val jsonResponse = JSONObject(responseBody)
                val message = jsonResponse.getString("response").trim()

                Log.d(TAG, "Ollama Response: $message")
                message
            } else {
                Log.e(TAG, "Ollama Error: ${response.code}")
                "Ollama service is not available. Make sure it's running on localhost:11434"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Ollama", e)
            "I couldn't reach the local AI service. Make sure Ollama is running."
        }
    }

    /**
     * Query Google Generative AI (Gemini)
     * Get free API key: https://ai.google.dev
     */
    suspend fun queryGemini(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey"

            val requestBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "No response from Gemini"
                val jsonResponse = JSONObject(responseBody)
                val message = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                Log.d(TAG, "Gemini Response: $message")
                message
            } else {
                Log.e(TAG, "Gemini API Error: ${response.code}")
                "Sorry, I'm having trouble with the AI service."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Gemini", e)
            "I encountered an error processing your request."
        }
    }

    /**
     * Query Claude via Anthropic API
     * Get API key: https://console.anthropic.com
     */
    suspend fun queryClaude(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = "https://api.anthropic.com/v1/messages"

            val requestBody = JSONObject().apply {
                put("model", "claude-3-sonnet-20240229")
                put("max_tokens", 150)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(url)
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "No response from Claude"
                val jsonResponse = JSONObject(responseBody)
                val message = jsonResponse
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .getString("text")
                    .trim()

                Log.d(TAG, "Claude Response: $message")
                message
            } else {
                Log.e(TAG, "Claude API Error: ${response.code}")
                "Sorry, I'm having trouble with Claude AI service."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Claude", e)
            "I encountered an error processing your request."
        }
    }

    /**
     * Simple command parser for local processing
     * Useful for offline commands
     */
    fun parseLocalCommand(command: String): String? {
        return when {
            command.contains("hello", ignoreCase = true) || 
            command.contains("hi", ignoreCase = true) -> "Hello! How can I help you today?"

            command.contains("thank", ignoreCase = true) -> "You're welcome! Happy to help."

            command.contains("bye", ignoreCase = true) || 
            command.contains("goodbye", ignoreCase = true) -> "Goodbye! Have a great day!"

            command.contains("how are you", ignoreCase = true) -> "I'm doing great! Ready to assist you."

            command.contains("name", ignoreCase = true) -> "I'm your personal voice assistant. You can call me Assistant."

            command.contains("what can you do", ignoreCase = true) -> 
                "I can recognize your voice commands, access your phone's features, and answer questions using AI. Just speak naturally!"

            else -> null
        }
    }
}
