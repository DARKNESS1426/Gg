package com.example.aiassistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class AssistantService : Service(), TextToSpeech.OnInitListener, RecognitionListener {

    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())
    private val channelId = "assistant_service_channel"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var isListening = false
    private var audioFocusRequest: AudioFocusRequest? = null

    companion object {
        private const val TAG = "AssistantService"
    }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        startForegroundNotification()
        initSpeechRecognizer()
        speak("Assistant initialized and ready for commands")
        startListening()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.pitch = 1.0f
            tts.setSpeechRate(1.0f)
        }
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition not available on this device")
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(this)
    }

    private fun startListening() {
        if (isListening) return

        requestAudioFocus()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        isListening = true
        speechRecognizer.startListening(intent)
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(audioAttributes)
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!)
        }
    }

    private fun speak(text: String) {
        if (::tts.isInitialized && tts.isSpeaking.not()) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "UTTERANCE_ID")
        }
    }

    private fun processCommand(command: String) {
        scope.launch {
            try {
                // Log the command
                android.util.Log.d(TAG, "Processing command: $command")

                val response = when {
                    command.contains("time", ignoreCase = true) -> handleTimeCommand()
                    command.contains("date", ignoreCase = true) -> handleDateCommand()
                    command.contains("open", ignoreCase = true) -> handleOpenCommand(command)
                    command.contains("call", ignoreCase = true) -> handleCallCommand(command)
                    command.contains("message", ignoreCase = true) -> handleMessageCommand(command)
                    command.contains("battery", ignoreCase = true) -> handleBatteryCommand()
                    command.contains("weather", ignoreCase = true) -> "Weather feature coming soon"
                    command.contains("joke", ignoreCase = true) -> "Why did the AI go to school? To improve its learning algorithm!"
                    command.contains("reminder", ignoreCase = true) -> handleReminderCommand(command)
                    else -> queryLLM(command)  // Send to LLM for intelligent response
                }

                speak(response)
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error processing command", e)
                speak("Sorry, I encountered an error processing your request")
            }

            // Resume listening after response
            handler.postDelayed({
                startListening()
            }, 2000)
        }
    }

    private fun handleTimeCommand(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return "The current time is $hour:$minute"
    }

    private fun handleDateCommand(): String {
        val calendar = java.util.Calendar.getInstance()
        val format = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
        return "Today is ${format.format(calendar.time)}"
    }

    private fun handleOpenCommand(command: String): String {
        return when {
            command.contains("settings", ignoreCase = true) -> {
                val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                startActivity(intent)
                "Opening settings"
            }
            command.contains("camera", ignoreCase = true) -> {
                val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                startActivity(intent)
                "Opening camera"
            }
            else -> "I can open settings or camera. What would you like?"
        }
    }

    private fun handleCallCommand(command: String): String {
        return "Call feature requires additional permissions. Please enable calling permission in settings"
    }

    private fun handleMessageCommand(command: String): String {
        return "Message feature requires additional permissions. Please enable SMS permission in settings"
    }

    private fun handleBatteryCommand(): String {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        val batteryLevel = batteryManager.getIntProperty(android.os.BatteryProperty.CHARGE_COUNTER)
        return "Battery status is available. Please check system settings for details"
    }

    private fun handleReminderCommand(command: String): String {
        // This would integrate with Android's reminder/alarm system
        return "Reminder feature coming soon. Please use the system reminders for now"
    }

    private suspend fun queryLLM(prompt: String): String {
        // This is where you'd integrate with an LLM API like OpenAI, Google AI, etc.
        // For now, returning a placeholder response
        return try {
            val response = LLMClient.queryGPT(prompt)
            response
        } catch (e: Exception) {
            android.util.Log.e(TAG, "LLM query failed", e)
            "I understood your request: $prompt. However, I need internet connection to process complex queries"
        }
    }

    private fun startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Voice Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🎙️ Voice Assistant Active")
            .setContentText("Listening for commands...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    // Speech Recognition Callbacks
    override fun onReadyForSpeech(params: android.os.Bundle?) {
        android.util.Log.d(TAG, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        android.util.Log.d(TAG, "Speech started")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Audio level changed
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Buffer received
    }

    override fun onEndOfSpeech() {
        android.util.Log.d(TAG, "Speech ended")
    }

    override fun onError(error: Int) {
        android.util.Log.e(TAG, "Speech recognition error: $error")
        isListening = false

        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Unknown error"
        }

        speak("I encountered an error: $errorMessage")
        handler.postDelayed({
            startListening()
        }, 2000)
    }

    override fun onResults(results: android.os.Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        
        if (!matches.isNullOrEmpty()) {
            val command = matches[0]
            android.util.Log.d(TAG, "Recognized: $command")
            processCommand(command)
        } else {
            speak("I didn't catch that. Please try again")
            handler.postDelayed({
                startListening()
            }, 1000)
        }
    }

    override fun onPartialResults(partialResults: android.os.Bundle?) {
        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        android.util.Log.d(TAG, "Partial: $partial")
    }

    override fun onEvent(eventType: Int, params: android.os.Bundle?) {
        // Handle additional events
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        handler.removeCallbacksAndMessages(null)
        scope.coroutineContext.cancelChildren()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        }

        super.onDestroy()
    }
}

// Extension function to cancel coroutine children
private fun kotlin.coroutines.CoroutineContext.cancelChildren() {
    val job = get(kotlinx.coroutines.Job)
    job?.cancelChildren()
}
