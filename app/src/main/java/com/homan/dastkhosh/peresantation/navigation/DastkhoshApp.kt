package com.homan.dastkhosh.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.homan.dastkhosh.peresantation.navigation.FloatingNavBar
import com.homan.dastkhosh.peresantation.theme.DastkhoshTheme
import com.homan.dastkhosh.presentation.expense.AddExpenseScreen
import com.homan.dastkhosh.presentation.expense.ExpenseEvent
import com.homan.dastkhosh.presentation.expense.ExpenseHistoryScreen
import com.homan.dastkhosh.presentation.expense.ExpenseViewModel
import com.homan.dastkhosh.presentation.settings.SettingsScreen

@Composable
fun DastkhoshApp() {
    DastkhoshTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    val navController = rememberNavController()
    val viewModel: ExpenseViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }

    val navItems = remember { Screen.items }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            when (event) {
                is ExpenseEvent.Message -> snackbarHostState.showSnackbar(event.text)
                is ExpenseEvent.Deleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.text,
                        actionLabel = "بازگردانی",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = Screen.start.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { slideInHorizontally(tween(380)) { -it / 3 } + fadeIn(tween(280)) },
            exitTransition  = { slideOutHorizontally(tween(380)) { it / 3 } + fadeOut(tween(180)) },
            popEnterTransition = { slideInHorizontally(tween(380)) { it / 3 } + fadeIn(tween(280)) },
            popExitTransition  = { slideOutHorizontally(tween(380)) { -it / 3 } + fadeOut(tween(180)) }
        ) {
            composable(Screen.AddExpense.route) { AddExpenseScreen(viewModel = viewModel) }
            composable(Screen.History.route) {
                ExpenseHistoryScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                actionColor = MaterialTheme.colorScheme.primaryContainer
            )
        }

        FloatingNavBar(
            items = navItems,
            currentRoute = currentRoute,
            onItemClick = { screen ->
                if (currentRoute != screen.route) {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}