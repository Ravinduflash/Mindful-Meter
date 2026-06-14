package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.AuthViewModel
import com.example.ui.screens.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.ui.MoodViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.JournalViewModel
import com.example.ui.MeditationViewModel
import com.example.ui.ProfileViewModel
import com.example.ui.SleepViewModel
import com.example.ui.LibraryViewModel
import com.example.ui.CommunityViewModel
import com.example.ui.CoachingViewModel
import com.example.ui.DailyIntentionViewModel
import com.example.ui.RoutineViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MoodTrackerScreen
import com.example.ui.screens.BreathingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.MeditationScreen
import com.example.ui.screens.ProfileChallengesScreen
import com.example.ui.screens.SleepScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.CoachingScreen
import com.example.ui.screens.DailyIntentionsScreen
import com.example.ui.screens.GroundingScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.RoutineScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MindfulAppNavigation(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MindfulAppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    
    // Instantiate our ViewModels with their robust custom factories
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModel.Factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val journalViewModel: JournalViewModel = viewModel(factory = JournalViewModel.Factory)
    val meditationViewModel: MeditationViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
    val sleepViewModel: SleepViewModel = viewModel()
    val libraryViewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.Factory)
    val communityViewModel: CommunityViewModel = viewModel(factory = CommunityViewModel.Factory)
    val coachingViewModel: CoachingViewModel = viewModel(factory = CoachingViewModel.Factory)
    val dailyIntentionViewModel: DailyIntentionViewModel = viewModel(factory = DailyIntentionViewModel.Factory)
    val routineViewModel: RoutineViewModel = viewModel(factory = RoutineViewModel.Factory)

    val currentUser = try {
        FirebaseAuth.getInstance().currentUser
    } catch (e: Throwable) {
        null
    }
    val startDest = if (currentUser != null) "dashboard" else "login"

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = moodViewModel,
                onNavigateToMood = { navController.navigate("mood") },
                onNavigateToBreathing = { navController.navigate("breathing") },
                onNavigateToProfileChallenges = { navController.navigate("profile_challenges") },
                onNavigateToJournal = { navController.navigate("journal") },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                onNavigateToMeditation = { navController.navigate("meditation") },
                onNavigateToSleep = { navController.navigate("sleep") },
                onNavigateToLibrary = { navController.navigate("library") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToCoaching = { navController.navigate("coaching") },
                onNavigateToIntentions = { navController.navigate("intentions") },
                onNavigateToFocus = { navController.navigate("focus") },
                onNavigateToGrounding = { navController.navigate("grounding") },
                onNavigateToRoutine = { navController.navigate("routine") }
            )
        }

        composable("routine") {
            RoutineScreen(
                viewModel = routineViewModel,
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.popBackStack() }
            )
        }

        composable("focus") {
            FocusScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("mood") {
            MoodTrackerScreen(
                viewModel = moodViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("breathing") {
            BreathingScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionCompleted = { profileViewModel.incrementBreathingSessions() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("profile_challenges") {
            ProfileChallengesScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("journal") {
            JournalScreen(
                viewModel = journalViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGrounding = { navController.navigate("grounding") }
            )
        }

        composable("analytics") {
            AnalyticsScreen(
                viewModel = moodViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("meditation") {
            MeditationScreen(
                viewModel = meditationViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("sleep") {
            SleepScreen(
                viewModel = sleepViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("library") {
            LibraryScreen(
                viewModel = libraryViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("community") {
            CommunityScreen(
                viewModel = communityViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGrounding = { navController.navigate("grounding") }
            )
        }

        composable("coaching") {
            CoachingScreen(
                viewModel = coachingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("intentions") {
            DailyIntentionsScreen(
                viewModel = dailyIntentionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("grounding") {
            GroundingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
