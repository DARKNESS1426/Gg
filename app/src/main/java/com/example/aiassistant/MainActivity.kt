package com.example.aiassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var toggleButton: Button
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create UI layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        statusText = TextView(this).apply {
            text = "Assistant Status: Inactive"
            textSize = 18f
            setPadding(0, 0, 0, 20)
        }

        toggleButton = Button(this).apply {
            text = "Activate Assistant"
            textSize = 20f
            setPadding(20, 20, 20, 20)
        }

        // Scrollable log for command history
        logText = TextView(this).apply {
            text = "Command Log:\n"
            textSize = 14f
            setPadding(10, 10, 10, 10)
        }

        val scrollView = ScrollView(this).apply {
            addView(logText)
        }

        mainLayout.addView(statusText)
        mainLayout.addView(toggleButton)
        mainLayout.addView(scrollView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        ))

        setContentView(mainLayout)

        // Check if service is already running
        isServiceRunning = isAssistantServiceRunning()
        updateUI()

        requestPermissions()

        toggleButton.setOnClickListener {
            if (isServiceRunning) {
                stopAssistant()
            } else {
                startAssistant()
            }
        }
    }

    private fun startAssistant() {
        val serviceIntent = Intent(this, AssistantService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        isServiceRunning = true
        updateUI()
        addLog("Assistant activated!")
        Toast.makeText(this, "Assistant activated!", Toast.LENGTH_SHORT).show()
    }

    private fun stopAssistant() {
        val serviceIntent = Intent(this, AssistantService::class.java)
        stopService(serviceIntent)
        isServiceRunning = false
        updateUI()
        addLog("Assistant deactivated.")
        Toast.makeText(this, "Assistant deactivated.", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        if (isServiceRunning) {
            statusText.text = "🎙️ Assistant Status: Active & Listening"
            toggleButton.text = "Deactivate Assistant"
        } else {
            statusText.text = "🔴 Assistant Status: Inactive"
            toggleButton.text = "Activate Assistant"
        }
    }

    private fun addLog(message: String) {
        logText.append("• $message\n")
    }

    private fun isAssistantServiceRunning(): Boolean {
        val manager = getSystemService(android.app.ActivityManager::class.java)
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == AssistantService::class.java.name) {
                return true
            }
        }
        return false
    }

    private fun requestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 101)
        }

        // Request battery optimization exemption
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // Battery optimization request not available
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
                addLog("Permissions granted successfully")
            } else {
                Toast.makeText(this, "Some permissions were denied.", Toast.LENGTH_SHORT).show()
                addLog("Some permissions denied")
            }
        }
    }
}
