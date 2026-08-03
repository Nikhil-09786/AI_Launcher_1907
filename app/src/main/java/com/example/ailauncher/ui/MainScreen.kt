package com.example.ailauncher.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ailauncher.ui.theme.BorderWhite
import com.example.ailauncher.ui.theme.ChipBackground
import com.example.ailauncher.ui.theme.MutedGray
import com.example.ailauncher.ui.theme.PitchBlack
import com.example.ailauncher.ui.theme.PureWhite

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(context) {
        viewModel.registerBatteryReceiver(context)
        onDispose {
            viewModel.unregisterBatteryReceiver(context)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PitchBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section: Clock, Date & Battery
            HeaderSection(
                timeString = uiState.timeString,
                dateString = uiState.dateString,
                batteryLevel = uiState.batteryLevel,
                isCharging = uiState.isCharging
            )

            // Center AI Interaction Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GreetingAndInputSection(
                    commandText = uiState.commandText,
                    isLoading = uiState.isLoading,
                    lastSubmitted = uiState.lastSubmittedCommand,
                    aiResponseText = uiState.aiResponseCardText,
                    actionStatus = uiState.executedActionStatus,
                    onCommandChange = viewModel::onCommandTextChange,
                    onSubmit = { viewModel.submitCommand(context) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Preset Suggestions
                QuickSuggestionsSection(
                    suggestions = uiState.suggestions,
                    onSuggestionClick = viewModel::onSuggestionSelected
                )
            }

            // Bottom Bar: Launcher Exit Button
            BottomExitSection(
                onExitClick = { viewModel.openHomeSettings(context) }
            )
        }
    }
}

@Composable
fun HeaderSection(
    timeString: String,
    dateString: String,
    batteryLevel: Int,
    isCharging: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeString.ifEmpty { "10:00 AM" },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 54.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-1).sp
            ),
            color = PureWhite
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateString.ifEmpty { "Monday, Jan 1" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = MutedGray
            )

            Text(
                text = "  •  ",
                color = MutedGray,
                fontSize = 12.sp
            )

            Icon(
                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = "Battery Status",
                tint = PureWhite,
                modifier = Modifier.height(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (batteryLevel >= 0) "$batteryLevel%" else "--%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = PureWhite
            )
        }
    }
}

@Composable
fun GreetingAndInputSection(
    commandText: String,
    isLoading: Boolean,
    lastSubmitted: String?,
    aiResponseText: String?,
    actionStatus: String?,
    onCommandChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Greeting Header
        Text(
            text = "What do you want to do?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = PureWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Text Answer Card Display (General Knowledge Responses)
        if (!aiResponseText.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .border(BorderStroke(1.dp, BorderWhite), RoundedCornerShape(12.dp))
                    .background(ChipBackground, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Gemini AI",
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GEMINI RESPONSE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = PureWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = aiResponseText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = PureWhite
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Executed System Action Status Chip Display
        if (!actionStatus.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MutedGray), RoundedCornerShape(8.dp))
                    .background(PitchBlack, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "⚡ $actionStatus",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = PureWhite
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Outlined Text Input Box
        OutlinedTextField(
            value = commandText,
            onValueChange = onCommandChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Type an AI command...",
                    color = MutedGray
                )
            },
            singleLine = true,
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PitchBlack,
                unfocusedContainerColor = PitchBlack,
                focusedBorderColor = BorderWhite,
                unfocusedBorderColor = MutedGray,
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite,
                cursorColor = PureWhite
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onSubmit()
                }
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Submit Button with Loading Indicator
        Button(
            onClick = {
                keyboardController?.hide()
                onSubmit()
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = PitchBlack,
                disabledContainerColor = MutedGray,
                disabledContentColor = PitchBlack
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PitchBlack,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PROCESSING...",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                } else {
                    Text(
                        text = "SUBMIT COMMAND",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Submit",
                        tint = PitchBlack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickSuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "QUICK SUGGESTIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            ),
            color = MutedGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .background(ChipBackground, RoundedCornerShape(20.dp))
                        .border(BorderStroke(1.dp, MutedGray), RoundedCornerShape(20.dp))
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
fun BottomExitSection(
    onExitClick: () -> Unit
) {
    OutlinedButton(
        onClick = onExitClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MutedGray),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = PureWhite
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Exit Launcher",
                tint = MutedGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Exit to Normal Android",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = PureWhite
            )
        }
    }
}
