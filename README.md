# 🎙️ Personal Voice Assistant for Android

A fully-featured voice-activated AI assistant that runs on Android with speech recognition, natural language processing, and intelligent command execution.

## Features

✨ **Core Features:**
- 🎤 Continuous speech recognition in the background
- 🤖 AI-powered responses using multiple LLM backends (OpenAI, Gemini, Claude, Ollama)
- 💬 Natural language command understanding
- 🔊 Text-to-speech responses
- 📱 Foreground service for always-on operation
- 🎯 Local command parsing for offline functionality
- 🔋 Battery optimization handling
- 🔐 Audio focus management

## Supported AI Backends

### 1. **OpenAI GPT-3.5 Turbo** (Recommended)
- Requires API key from [openai.com](https://openai.com/api)
- Best for intelligent, contextual responses
- Requires internet connection

### 2. **Google Gemini** (Free)
- Free API key from [ai.google.dev](https://ai.google.dev)
- Good balance of quality and cost
- Requires internet connection

### 3. **Anthropic Claude** (Premium)
- API key from [console.anthropic.com](https://console.anthropic.com)
- Excellent for nuanced conversations
- Requires internet connection

### 4. **Ollama** (Local, Free)
- Download from [ollama.ai](https://ollama.ai)
- Runs locally on your computer
- No API key needed
- Requires local network connection
- Great for privacy

## Installation

### Prerequisites
- Android 6.0+ (API 24)
- Microphone permission
- Internet permission

### Steps

1. **Clone the repository**
```bash
git clone https://github.com/DARKNESS1426/Gg.git
cd Gg
```

2. **Open in Android Studio**
- File → Open → Select project folder

3. **Configure your AI Backend**

#### Option A: OpenAI (Recommended)
```kotlin
// In LLMClient.kt, line 15
private const val OPENAI_API_KEY = "sk-your-actual-api-key-here"
```

#### Option B: Google Gemini
```kotlin
// In AssistantService.kt, modify queryLLM()
private suspend fun queryLLM(prompt: String): String {
    return LLMClient.queryGemini(prompt, "your-gemini-api-key")
}
```

#### Option C: Claude
```kotlin
// In AssistantService.kt, modify queryLLM()
private suspend fun queryLLM(prompt: String): String {
    return LLMClient.queryClaude(prompt, "your-claude-api-key")
}
```

#### Option D: Local Ollama (Free)
```bash
# On your computer
ollama pull llama2
ollama serve
```

```kotlin
// In AssistantService.kt, modify queryLLM()
private suspend fun queryLLM(prompt: String): String {
    return LLMClient.queryOllama(prompt)
}
```

4. **Build and Run**
```bash
# Using Android Studio: Run → Run 'app'
# Or via command line:
./gradlew installDebug
```

5. **Grant Permissions**
- Allow microphone access when prompted
- Allow battery optimization exemption

## Usage

### Starting the Assistant
1. Open the app
2. Tap "Activate Assistant"
3. Start speaking your commands

### Example Commands

**Time & Date:**
- "What time is it?"
- "What's today's date?"

**System Control:**
- "Open settings"
- "Open camera"

**AI-Powered Questions:**
- "What's the capital of France?"
- "Explain quantum computing"
- "Tell me a joke"
- "How do I make pasta?"

**Smart Features:**
- "Set a reminder"
- "Check battery status"
- "What can you do?"

## Project Structure

```
app/src/main/java/com/example/aiassistant/
├── MainActivity.kt          # Main UI with activation button
├── AssistantService.kt      # Background service with speech recognition
└── LLMClient.kt            # AI backend integration

app/src/main/
├── AndroidManifest.xml     # Permissions and service declarations
└── res/
    ├── values/
    └── layout/
```

## Permissions Required

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

## Configuration

### Adjust Speech Recognition Settings
Edit `AssistantService.kt` in the `startListening()` function:

```kotlin
private fun startListening() {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.language)
        // Adjust these:
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
    }
    speechRecognizer.startListening(intent)
}
```

### Adjust TTS Settings
Edit `AssistantService.kt` in the `onInit()` function:

```kotlin
override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
        tts.language = Locale.US
        tts.pitch = 1.0f      // 0.5 = lower, 2.0 = higher
        tts.setSpeechRate(1.0f) // 0.5 = slower, 2.0 = faster
    }
}
```

## Advanced Features

### Adding Custom Commands
Edit the `processCommand()` function in `AssistantService.kt`:

```kotlin
private fun processCommand(command: String) {
    val response = when {
        command.contains("custom keyword", ignoreCase = true) -> {
            // Your custom logic here
            "Custom response"
        }
        // ... other commands
        else -> queryLLM(command)
    }
    speak(response)
}
```

### Extending to Multiple Languages
```kotlin
// Modify onInit()
when (getSystemLanguage()) {
    "es" -> tts.language = Locale("es", "ES")
    "fr" -> tts.language = Locale("fr", "FR")
    else -> tts.language = Locale.US
}
```

### Adding Intent-based Actions
```kotlin
// In processCommand()
command.contains("call", ignoreCase = true) -> {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:+1234567890")
    }
    startActivity(intent)
    "Calling..."
}
```

## Troubleshooting

### "API Key Error"
- Check your API key is correct and has remaining credits
- Verify internet connection
- Check API key permissions in provider settings

### "Speech Recognition Not Available"
- Ensure microphone permission is granted
- Restart the app
- Check if Google Speech Recognition is available on your device

### "No Response from Ollama"
- Verify Ollama is running: `ollama serve`
- Check local network connectivity
- Ensure model is pulled: `ollama pull llama2`

### App Crashes on Startup
- Clear app cache: Settings → Apps → AI Assistant → Storage → Clear Cache
- Uninstall and reinstall the app
- Check Android version compatibility (requires API 24+)

### TTS Not Working
- Go to Settings → Accessibility → Text-to-Speech Options
- Verify TTS engine is installed and set as default
- Check speaker volume

## Performance Optimization

### Reduce Battery Drain
1. Reduce speech recognition timeout:
```kotlin
// In startListening(), reduce delay between recognition attempts
handler.postDelayed({ startListening() }, 5000) // 5 seconds instead of 2
```

2. Use local Ollama instead of cloud APIs

3. Implement conditional listening (only when screen is on)

### Reduce Network Usage
- Use Ollama for frequent commands
- Cache common responses
- Compress audio before sending

## API Cost Estimation

| Provider | Cost | Latency | Quality |
|----------|------|---------|---------|
| OpenAI | $0.0005-0.002/request | Low (1-2s) | Excellent |
| Gemini | $0.075/1M tokens | Low (1-2s) | Good |
| Claude | $0.003-0.03/request | Low (2-3s) | Excellent |
| Ollama | Free | Medium (5-10s) | Good |

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## Security Considerations

⚠️ **Important:**
- Never commit API keys to version control
- Use environment variables or secure storage
- Consider using Android Keystore for sensitive data
- Implement rate limiting to prevent API abuse
- Add input validation for voice commands

```kotlin
// Example: Using SharedPreferences securely
val preferences = getSharedPreferences("assistant", Context.MODE_PRIVATE)
val apiKey = preferences.getString("openai_key", "")
```

## Future Enhancements

- [ ] Multi-language support
- [ ] Custom voice training
- [ ] Offline mode improvements
- [ ] Integration with smart home devices
- [ ] Machine learning-based command prediction
- [ ] Voice biometrics/authentication
- [ ] Advanced task automation
- [ ] Calendar and reminder integration
- [ ] Email and messaging support
- [ ] Real-time translation

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues and feature requests, please open an GitHub Issue.

## Author

Created by DARKNESS1426

---

**Made with ❤️ for voice enthusiasts**

Remember: Always respect user privacy and follow API provider's terms of service!
