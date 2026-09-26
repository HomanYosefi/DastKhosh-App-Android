package com.homan.dastkhosh.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.homan.dastkhosh.presentation.theme.AppIcons

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object AddExpense : Screen(
        route = "add_expense",
        title = "ثبت هزینه",
        icon = AppIcons.Add
    )

    data object History : Screen(
        route = "history",
        title = "تاریخچه",
        icon = AppIcons.History
    )

    data object Settings : Screen(
        route = "settings",
        title = "تنظیمات",
        icon = AppIcons.Setting
    )

    companion object {
        val items: List<Screen>
            get() = listOf(AddExpense, History)
        val start: Screen get() = AddExpense
    }
}