package com.example.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceRecognitionManager(
    private val context: Context,
    private val onRecognitionResult: (String, Boolean) -> Unit, // (Transcription, isFinal)
    private val onRecognitionError: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onRmsLevelChanged: (Float) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onRecognitionError("Speech recognition is not supported on this device.")
            return
        }
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d("VoiceRecognition", "Ready for speech")
                        isListening = true
                        onListeningStateChanged(true)
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d("VoiceRecognition", "Beginning of speech")
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        onRmsLevelChanged(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d("VoiceRecognition", "End of speech")
                        isListening = false
                        onListeningStateChanged(false)
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                            SpeechRecognizer.ERROR_NETWORK -> "Network error"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Unknown speech recognition error"
                        }
                        Log.e("VoiceRecognition", "Error code $error: $message")
                        isListening = false
                        onListeningStateChanged(false)
                        // Don't issue annoying errors for silent speech timeout unless it's critical
                        if (error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT && error != SpeechRecognizer.ERROR_NO_MATCH) {
                            onRecognitionError(message)
                        } else if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                            onRecognitionError("Could not recognize speech. Try speaking closer to the mic.")
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onRecognitionResult(matches[0], true)
                        }
                        isListening = false
                        onListeningStateChanged(false)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onRecognitionResult(matches[0], false)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        } catch (e: Exception) {
            Log.e("VoiceRecognition", "Failed to create SpeechRecognizer", e)
            onRecognitionError("Initialization error: ${e.localizedMessage}")
        }
    }

    fun startListening() {
        if (isListening) return

        // If recognizer wasn't initialized, try again
        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceRecognition", "Failed to start listening", e)
            onRecognitionError("Failed to start listening: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e("VoiceRecognition", "Error stopping listening", e)
        }
        isListening = false
        onListeningStateChanged(false)
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e("VoiceRecognition", "Error canceling listening", e)
        }
        isListening = false
        onListeningStateChanged(false)
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceRecognition", "Error destroying SpeechRecognizer", e)
        }
        speechRecognizer = null
    }
}
