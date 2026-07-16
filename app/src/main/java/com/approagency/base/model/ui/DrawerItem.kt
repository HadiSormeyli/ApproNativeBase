package com.approagency.base.model.ui

import androidx.compose.ui.graphics.Color

sealed interface DrawerItem {
    data class Simple(
        val title: Label,
        val icon: Icon? = null,
        val enabled: Boolean = true,
        val selected: Boolean = false,
        val badgeCount: Int? = null,
        val backgroundColor: Color = Color.Unspecified,
        val foregroundColor: Color = Color.Unspecified,
        val onClick: () -> Unit = {}
    ) : DrawerItem

    data class DropDown(
        val title: Label,
        val icon: Icon? = null,
        val expanded: Boolean = false,
        val enabled: Boolean = true,
        val backgroundColor: Color = Color.Unspecified,
        val foregroundColor: Color = Color.Unspecified,
        val children: List<Simple> = emptyList(),
        val onClick: () -> Unit = {}
    ) : DrawerItem

    data class Divider(
        val color: Color = Color.Unspecified
    ) : DrawerItem
}