package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.media.VoiceRecognitionManager
import com.example.ui.theme.*

@Composable
fun VoiceDictationButton(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "voice_dictation_mic_button",
    hintLabel: String = ""
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var baselineText by remember { mutableStateOf("") }
    var recognitionError by remember { mutableStateOf<String?>(null) }

    // Sound activation RMS dB animation trigger
    var rmsLevel by remember { mutableStateOf(0f) }

    // Create and remember VoiceRecognitionManager
    val voiceRecognitionManager = remember {
        VoiceRecognitionManager(
            context = context,
            onRecognitionResult = { transcription, isFinal ->
                val separator = if (baselineText.isBlank() || baselineText.endsWith(" ")) "" else " "
                onTextChange(baselineText + separator + transcription)
            },
            onRecognitionError = { errorMessage ->
                recognitionError = errorMessage
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                isListening = false
            },
            onListeningStateChanged = { listening ->
                isListening = listening
            },
            onRmsLevelChanged = { rms ->
                rmsLevel = rms
            }
        )
    }

    // Always destroy recognizer on dispose
    DisposableEffect(voiceRecognitionManager) {
        onDispose {
            voiceRecognitionManager.destroy()
        }
    }

    // Simple permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            baselineText = text
            isListening = true
            recognitionError = null
            voiceRecognitionManager.startListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice typing.", Toast.LENGTH_LONG).show()
        }
    }

    fun toggleListening() {
        if (isListening) {
            voiceRecognitionManager.stopListening()
            isListening = false
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            )
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                baselineText = text
                isListening = true
                recognitionError = null
                voiceRecognitionManager.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Dynamic wave expansion animation based on RMS Db levels and state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f + (rmsLevel.coerceIn(0f, 10f) / 10f * 0.35f) else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.5f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "haloAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .wrapContentSize()
            .minimumInteractiveComponentSize()
    ) {
        // Pulsing background bubble that glows as speaker sounds waves
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(pulseScale)
                    .background(
                        BentoPrimaryGreenText.copy(alpha = haloAlpha),
                        shape = CircleShape
                    )
            )
        }

        IconButton(
            onClick = { toggleListening() },
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = if (isListening) BentoPrimaryGreenText else BentoBg,
                    shape = CircleShape
                )
                .testTag(testTag)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = if (isListening) "Stop dictating" else "Dictate text",
                tint = if (isListening) Color.White else BentoPrimaryGreenText,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
