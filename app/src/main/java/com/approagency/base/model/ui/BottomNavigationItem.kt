package com.approagency.base.model.ui

data class BottomNavigationItem<T>(
    val label: Label,
    val icon: Icon,
    val route: T,
    val selectedIcon: Icon = icon,
    val enabled: Boolean = true,
    val badgeCount: Int? = null
)

