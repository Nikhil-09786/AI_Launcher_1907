package com.example.ailauncher.ai

import android.content.Context
import com.example.ailauncher.system.SystemActions
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.FunctionCallPart
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class GeminiResult {
    data class SuccessText(val text: String) : GeminiResult()
    data class ExecutedAction(val actionDescription: String, val resultText: String) : GeminiResult()
    data class Error(val message: String) : GeminiResult()
}

class GeminiManager {

    companion object {
        // User provided API key (AQ format)
        private const val API_KEY = "AQ.Ab8RN6KXDP9OB9lg4ExtwaFgoxiD-xTKZAyCDoLopeSa32RVGQ"
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    private val makePhoneCallTool = defineFunction(
        name = "makePhoneCall",
        description = "Make a phone call or dial a phone number/contact",
        parameters = listOf(
            Schema.str("target", "Name or phone number of contact to dial/call")
        )
    )

    private val sendSmsTool = defineFunction(
        name = "sendSms",
        description = "Send an SMS text message to a recipient",
        parameters = listOf(
            Schema.str("phoneNumber", "Phone number or contact name"),
            Schema.str("message", "Text message body to send")
        )
    )

    private val setAlarmTool = defineFunction(
        name = "setAlarm",
        description = "Set an alarm for a specified hour and minute",
        parameters = listOf(
            Schema.int("hour", "Hour of alarm in 24-hour format (0-23)"),
            Schema.int("minute", "Minute of alarm (0-59)"),
            Schema.str("label", "Optional alarm label or description")
        )
    )

    private val openAppTool = defineFunction(
        name = "openApp",
        description = "Open an installed Android app by name (e.g. WhatsApp, YouTube, Camera, Settings, Chrome)",
        parameters = listOf(
            Schema.str("appName", "Name of the app to launch")
        )
    )

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY,
            tools = listOf(
                Tool(
                    listOf(
                        makePhoneCallTool,
                        sendSmsTool,
                        setAlarmTool,
                        openAppTool
                    )
                )
            )
        )
    }

    private val plainGenerativeModel by lazy {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY
        )
    }

    suspend fun processUserPrompt(context: Context, userPrompt: String): GeminiResult = withContext(Dispatchers.IO) {
        // 1. First check heuristic shortcuts for instant action response
        val heuristicResult = handleHeuristicAction(context, userPrompt)
        if (heuristicResult != null) {
            return@withContext heuristicResult
        }

        // 2. Query Gemini API with Function Calling Tools
        try {
            val response = generativeModel.generateContent(userPrompt)
            val functionCalls = response.functionCalls

            if (functionCalls.isNotEmpty()) {
                val firstCall = functionCalls.first()
                return@withContext executeFunctionCall(context, firstCall)
            }

            val textResponse = response.text
            if (!textResponse.isNullOrBlank()) {
                return@withContext GeminiResult.SuccessText(textResponse.trim())
            }
        } catch (toolException: Exception) {
            // If tool model fails, fall back to plain text query
            try {
                val plainResponse = plainGenerativeModel.generateContent(userPrompt)
                val textResponse = plainResponse.text
                if (!textResponse.isNullOrBlank()) {
                    return@withContext GeminiResult.SuccessText(textResponse.trim())
                }
            } catch (plainException: Exception) {
                val errorDetails = plainException.localizedMessage ?: plainException.message ?: plainException.toString()
                return@withContext GeminiResult.Error(errorDetails)
            }

            val toolErrorDetails = toolException.localizedMessage ?: toolException.message ?: toolException.toString()
            return@withContext GeminiResult.Error(toolErrorDetails)
        }

        return@withContext GeminiResult.SuccessText("No response returned from Gemini.")
    }

    private fun executeFunctionCall(context: Context, call: FunctionCallPart): GeminiResult {
        val fnName = call.name
        val args = call.args

        return when (fnName) {
            "makePhoneCall" -> {
                val target = args["target"] ?: ""
                val result = SystemActions.makePhoneCall(context, target)
                GeminiResult.ExecutedAction("Calling $target", result)
            }
            "sendSms" -> {
                val phone = args["phoneNumber"] ?: ""
                val msg = args["message"] ?: ""
                val result = SystemActions.sendSms(context, phone, msg)
                GeminiResult.ExecutedAction("Sending SMS to $phone", result)
            }
            "setAlarm" -> {
                val hour = args["hour"]?.toIntOrNull() ?: 7
                val minute = args["minute"]?.toIntOrNull() ?: 0
                val label = args["label"] ?: "Alarm"
                val result = SystemActions.setAlarm(context, hour, minute, label)
                GeminiResult.ExecutedAction("Setting alarm for $hour:$minute", result)
            }
            "openApp" -> {
                val appName = args["appName"] ?: ""
                val result = SystemActions.openApp(context, appName)
                GeminiResult.ExecutedAction("Opening $appName", result)
            }
            else -> GeminiResult.Error("Unknown tool call: $fnName")
        }
    }

    private fun handleHeuristicAction(context: Context, prompt: String): GeminiResult? {
        val lower = prompt.lowercase().trim()
        return when {
            lower.startsWith("call ") || lower.startsWith("dial ") -> {
                val target = prompt.substringAfter(" ").trim()
                val res = SystemActions.makePhoneCall(context, target)
                GeminiResult.ExecutedAction("Calling $target", res)
            }
            lower.startsWith("open ") || lower.startsWith("launch ") -> {
                val appName = prompt.substringAfter(" ").trim()
                val res = SystemActions.openApp(context, appName)
                GeminiResult.ExecutedAction("Opening $appName", res)
            }
            lower.startsWith("set alarm") || lower.contains("alarm for") -> {
                val res = SystemActions.setAlarm(context, 7, 0, "Alarm")
                GeminiResult.ExecutedAction("Setting Alarm", res)
            }
            else -> null
        }
    }
}
