# 🚀 Quick Start Setup Guide

This guide will get your Personal Voice Assistant up and running in 5 minutes.

## Prerequisites Checklist

- ✅ Android Studio installed ([Download](https://developer.android.com/studio))
- ✅ Android device/emulator with API 24+ (Android 6.0+)
- ✅ Internet connection
- ✅ One of the following API keys:
  - OpenAI API key ([Get here](https://platform.openai.com/api-keys))
  - OR Google Gemini key ([Get here](https://ai.google.dev))
  - OR Anthropic Claude key ([Get here](https://console.anthropic.com))
  - OR Ollama installed locally ([Download](https://ollama.ai))

---

## Step 1: Clone & Setup Project (2 minutes)

```bash
# Clone the repository
git clone https://github.com/DARKNESS1426/Gg.git
cd Gg

# Open in Android Studio
# File → Open → Select Gg folder
```

---

## Step 2: Configure Your AI Backend (1 minute)

### Option A: OpenAI GPT (Recommended) ⭐

1. Go to [OpenAI Platform](https://platform.openai.com/api-keys)
2. Create new API key
3. Copy your key
4. Open `app/src/main/java/com/example/aiassistant/AssistantConfig.kt`
5. Replace on line 11:
```kotlin
const val OPENAI_API_KEY = "sk-your-actual-api-key-here"
```
with your actual key.

**Cost:** ~$0.0005-0.002 per request

---

### Option B: Google Gemini (Free) ✨

1. Go to [Google AI Studio](https://ai.google.dev)
2. Click "Get API Key"
3. Create new API key for Android
4. Copy the key
5. Open `AssistantConfig.kt`
6. Replace line 15:
```kotlin
const val GEMINI_API_KEY = "your-gemini-api-key-here"
```
7. Change line 7:
```kotlin
const val ACTIVE_LLM_BACKEND = "gemini"
```

**Cost:** Free tier available!

---

### Option C: Claude by Anthropic 🎯

1. Go to [Anthropic Console](https://console.anthropic.com)
2. Create API key
3. Open `AssistantConfig.kt`
4. Replace line 19:
```kotlin
const val CLAUDE_API_KEY = "your-claude-api-key-here"
```
5. Change line 7:
```kotlin
const val ACTIVE_LLM_BACKEND = "claude"
```

**Cost:** Paid API

---

### Option D: Ollama (Local, Free) 💻

Perfect if you want to run AI locally without any API costs!

**On your computer:**

```bash
# Install Ollama from https://ollama.ai

# Start Ollama service
ollama serve

# In another terminal, pull a model
ollama pull llama2

# Note your computer's IP address (for Android to connect)
# Windows: ipconfig | find "IPv4"
# Mac/Linux: ifconfig | grep "inet "
```

**In Android app:**

1. Open `AssistantConfig.kt`
2. Replace line 23 with your computer's IP:
```kotlin
const val OLLAMA_URL = "http://192.168.1.100:11434/api/generate"  // Replace IP
```
3. Change line 7:
```kotlin
const val ACTIVE_LLM_BACKEND = "ollama"
```

**Cost:** Free! Runs on your computer.

---

## Step 3: Build & Install (2 minutes)

### Using Android Studio:
1. Click "Build" → "Make Project"
2. Click "Run" → "Run 'app'"
3. Select your device
4. Wait for installation

### Using Command Line:
```bash
./gradlew installDebug
```

---

## Step 4: Grant Permissions

When app launches:
1. ✅ Tap "Allow" for microphone access
2. ✅ Tap "Allow" for battery optimization exemption
3. ✅ Done!

---

## Step 5: Test Your Assistant

### Test Commands:

**Basic:**
- "What time is it?"
- "What's today's date?"
- "Tell me a joke"

**Smart (LLM-powered):**
- "Explain quantum computing"
- "What's the capital of France?"
- "How do I make coffee?"
- "What are the benefits of meditation?"

**System:**
- "Open settings"
- "Open camera"
- "Check battery status"

---

## Troubleshooting

### Problem: "API Key Error"

**Solution:**
- Verify API key is correct (no extra spaces)
- Check your API key has remaining credits
- For OpenAI: https://platform.openai.com/account/usage/overview

### Problem: "Speech Recognition Not Available"

**Solution:**
- Ensure microphone permission is granted
- Go to Settings → Apps → AI Assistant → Permissions → Allow Microphone
- Restart the app

### Problem: "No Response from Ollama"

**Solution:**
```bash
# Check Ollama is running
ollama serve

# Check IP address matches
# Computer IP should match OLLAMA_URL in config

# Try from phone
# Open browser on phone and go to: http://192.168.1.100:11434
# Should show Ollama page
```

### Problem: "App Crashes on Startup"

**Solution:**
```bash
# Clear cache
# Settings → Apps → AI Assistant → Storage → Clear Cache & Data

# Reinstall
./gradlew installDebug --force
```

### Problem: "TTS Voice Not Working"

**Solution:**
- Go to Settings → Accessibility → Text-to-Speech
- Ensure a TTS engine is installed and set as default
- Increase speaker volume

---

## Next Steps

### After Setup Works:

1. **Customize Voice Settings**
   - Open `AssistantConfig.kt`
   - Adjust `TTS_PITCH` (0.5 = lower, 2.0 = higher)
   - Adjust `TTS_SPEECH_RATE` (0.5 = slower, 2.0 = faster)

2. **Add Custom Commands**
   - Edit `processCommand()` in `AssistantService.kt`
   - Add your own voice commands

3. **Enable More Features**
   - Set `ENABLE_LOGGING = true` in `AssistantConfig.kt`
   - Check logs: `adb logcat | grep VoiceAssistant`

4. **Monitor Usage**
   - Track API costs if using cloud backends
   - Switch to Ollama if costs are high

---

## Quick Reference

| Feature | Status | Setup Time |
|---------|--------|-----------|
| Speech Recognition | ✅ Working | 0 min |
| Text-to-Speech | ✅ Working | 0 min |
| OpenAI Integration | ✅ Ready | 2 min |
| Gemini Integration | ✅ Ready | 2 min |
| Claude Integration | ✅ Ready | 2 min |
| Ollama Integration | ✅ Ready | 5 min |
| Command Parsing | ✅ Ready | 0 min |
| Logging & Analytics | ✅ Ready | 0 min |

---

## API Key Cost Comparison

| Provider | Setup Time | Cost | Latency | Quality |
|----------|-----------|------|---------|---------|
| **OpenAI** | 2 min | $0.0015/req | ⚡ 1-2s | ⭐⭐⭐⭐⭐ |
| **Gemini** | 2 min | 💰 Free | ⚡ 1-2s | ⭐⭐⭐⭐ |
| **Claude** | 2 min | $ Variable | ⚡ 2-3s | ⭐⭐⭐⭐⭐ |
| **Ollama** | 5 min | 🆓 Free | 🐌 5-10s | ⭐⭐⭐ |

**Recommendation for beginners:** Start with **Gemini (free)** or **Ollama (local)**

---

## Security Tips

⚠️ **Important:**

1. **Never commit API keys to Git:**
   ```bash
   # Add to .gitignore
   echo "AssistantConfig.kt" >> .gitignore
   ```

2. **Use Environment Variables:**
   ```kotlin
   // Instead of hardcoding
   val apiKey = System.getenv("OPENAI_API_KEY")
   ```

3. **Store keys securely:**
   ```kotlin
   // Use Android Keystore for production
   val keyStore = KeyStore.getInstance("AndroidKeyStore")
   keyStore.load(null)
   ```

---

## Getting Help

- 📖 Full README: See `README.md`
- 🐛 Report Issues: Open GitHub issue
- 💬 Discussions: Check GitHub discussions
- 📧 Contact: Check repository for contact info

---

## What's Next?

✅ Assistant is working!

Now explore:
- Advanced features in `AssistantUtils.kt`
- Custom commands in `AssistantService.kt`
- Configuration options in `AssistantConfig.kt`
- Voice settings in `MainActivity.kt`

**Happy Voice Assisting! 🎉**
