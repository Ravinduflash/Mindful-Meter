package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.updateAll
import com.example.MindfulApplication
import com.example.data.MoodLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MindfulWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val application = context.applicationContext as MindfulApplication
        val moodRepository = application.container.moodRepository

        // Fetch logs and journals safely with a timeout fallback
        val logs = try {
            withTimeoutOrNull(1500) { moodRepository.getAllLogs().first() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val journals = try {
            withTimeoutOrNull(1500) { moodRepository.getAllJournalEntries().first() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val timestamps = logs.map { it.timestamp } + journals.map { it.timestamp }
        val streak = calculateStreak(timestamps)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFFDFDF5))
                    .cornerRadius(16.dp)
                    .padding(12.dp),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Title & Streak Row
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "Mindful Meter",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF1B1C17)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFFE2F1AF))
                            .cornerRadius(8.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (streak > 0) "🔥 $streak d" else "💤 --",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF386B01)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Header message instruction
                Text(
                    text = "Quick Mindful Mood Check-in",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF46483C)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = GlanceModifier.height(10.dp))

                // The 5 Mood option Emoji circular elements
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    val moodOptions = listOf(
                        Triple("😊", "Happy", Color(0xFFF9E8B6)),
                        Triple("😌", "Calm", Color(0xFFC0E5DF)),
                        Triple("😬", "Stressed", Color(0xFFFAD1C5)),
                        Triple("😰", "Anxious", Color(0xFFCFE5FC)),
                        Triple("😢", "Sad", Color(0xFFD2D2ED))
                    )

                    moodOptions.forEach { (emoji, name, color) ->
                        Box(
                            modifier = GlanceModifier
                                .padding(horizontal = 4.dp)
                                .background(color)
                                .cornerRadius(12.dp)
                                .padding(6.dp)
                                .clickable(
                                    actionRunCallback<MoodLogActionCallback>(
                                        actionParametersOf(MoodLogActionCallback.MoodParam to name)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                style = TextStyle(fontSize = 18.sp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        try {
            val localDates = timestamps.map {
                Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }.toSet()

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            var current = if (localDates.contains(today)) today else if (localDates.contains(yesterday)) yesterday else null
            if (current == null) return 0

            var streak = 1
            var checkDate = current.minusDays(1)
            while (localDates.contains(checkDate)) {
                streak++
                checkDate = checkDate.minusDays(1)
            }
            return streak
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
}

class MoodLogActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val mood = parameters[MoodParam] ?: return

        try {
            val application = context.applicationContext as MindfulApplication
            val moodRepository = application.container.moodRepository
            moodRepository.insertLog(MoodLog(mood = mood, note = "Quick Log from Widget"))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Force reload widgets on tap
        MindfulWidget().updateAll(context)
    }

    companion object {
        val MoodParam = ActionParameters.Key<String>("mood")
    }
}

class MindfulWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MindfulWidget()
}
