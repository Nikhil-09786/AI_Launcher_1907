package com.example.ailauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ailauncher.ai.GeminiManager
import com.example.ailauncher.ai.GeminiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MainUiState(
    val timeString: String = "",
    val dateString: String = "",
    val batteryLevel: Int = -1,
    val isCharging: Boolean = false,
    val commandText: String = "",
    val isLoading: Boolean = false,
    val lastSubmittedCommand: String? = null,
    val aiResponseCardText: String? = null,
    val executedActionStatus: String? = null,
    val suggestions: List<String> = listOf("Call Mom", "Navigate Home", "Set alarm for 7 AM", "Open WhatsApp", "What is quantum computing?")
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val geminiManager = GeminiManager()
    private var batteryReceiver: BroadcastReceiver? = null

    init {
        startClockUpdates()
    }

    private fun startClockUpdates() {
        viewModelScope.launch {
            while (true) {
                val now = Date()
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

                _uiState.update { current ->
                    current.copy(
                        timeString = timeFormat.format(now),
                        dateString = dateFormat.format(now)
                    )
                }
                delay(1000L)
            }
        }
    }

    fun registerBatteryReceiver(context: Context) {
        if (batteryReceiver != null) return

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
                _uiState.update { current ->
                    current.copy(batteryLevel = pct, isCharging = isCharging)
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyStatus = context.registerReceiver(receiver, filter)
        batteryReceiver = receiver

        stickyStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
            _uiState.update { current ->
                current.copy(batteryLevel = pct, isCharging = isCharging)
            }
        }
    }

    fun unregisterBatteryReceiver(context: Context) {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore unregistration error
            }
            batteryReceiver = null
        }
    }

    fun onCommandTextChange(newText: String) {
        _uiState.update { it.copy(commandText = newText) }
    }

    fun onSuggestionSelected(suggestion: String) {
        _uiState.update { it.copy(commandText = suggestion) }
    }

    fun submitCommand(context: Context) {
        val prompt = _uiState.value.commandText.trim()
        if (prompt.isEmpty() || _uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                lastSubmittedCommand = prompt,
                commandText = "",
                aiResponseCardText = null,
                executedActionStatus = null
            )
        }

        viewModelScope.launch {
            when (val result = geminiManager.processUserPrompt(context.applicationContext, prompt)) {
                is GeminiResult.SuccessText -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            aiResponseCardText = result.text
                        )
                    }
                }
                is GeminiResult.ExecutedAction -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            executedActionStatus = "${result.actionDescription} • ${result.resultText}"
                        )
                    }
                }
                is GeminiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            aiResponseCardText = "Error: ${result.message}"
                        )
                    }
                }
            }
        }
    }

    fun openHomeSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Intent(Settings.ACTION_HOME_SETTINGS)
            } else {
                Intent(Settings.ACTION_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Select Home Launcher"))
        }
    }
}
