package com.approagency.base.presentation.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.approagency.base.model.ui.BottomNavigationItem
import com.approagency.base.model.ui.BottomNavigationLayout
import com.approagency.base.utils.Icon
import com.approagency.base.utils.asAnnotatedString

@Composable
fun <T> BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavigationItem<T>>,
    onNavigate: (T) -> Unit,
    routeMatcher: (T, String?) -> Boolean,
    modifier: Modifier = Modifier,
    layout: BottomNavigationLayout = BottomNavigationLayout.AUTO,
    isVisible: (String?) -> Boolean = { true },
    showHorizontalLabels: Boolean = true,
    showVerticalLabels: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    indicatorColor: Color = selectedColor,
    dividerColor: Color = MaterialTheme.colorScheme.outlineVariant,
    horizontalContentPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    verticalContentPadding: PaddingValues = PaddingValues(
        start = 8.dp, top = 24.dp, bottom = 12.dp
    ),
    verticalWidth: Dp = 64.dp,
    verticalShape: Shape = MaterialTheme.shapes.large,
    indicatorThickness: Dp = 4.dp,
    animationDuration: Int = 300
) {
    val configuration = LocalConfiguration.current
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val selectedIndex = items.indexOfFirst { routeMatcher(it.route, currentRoute) }
    val resolvedLayout = when (layout) {
        BottomNavigationLayout.AUTO -> {
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                BottomNavigationLayout.HORIZONTAL
            } else {
                BottomNavigationLayout.VERTICAL
            }
        }

        else -> layout
    }

    if (!isVisible(currentRoute)) return

    when (resolvedLayout) {
        BottomNavigationLayout.HORIZONTAL -> HorizontalBottomBarTabs(
            tabs = items,
            selectedItem = selectedIndex,
            onTabSelected = { onNavigate(it.route) },
            modifier = modifier,
            showLabels = showHorizontalLabels,
            selectedColor = selectedColor,
            unselectedColor = unselectedColor,
            containerColor = containerColor,
            indicatorColor = indicatorColor,
            dividerColor = dividerColor,
            contentPadding = horizontalContentPadding,
            indicatorThickness = indicatorThickness,
            animationDuration = animationDuration
        )

        BottomNavigationLayout.VERTICAL -> VerticalBottomBarTabs(
            tabs = items,
            selectedItem = selectedIndex,
            onTabSelected = { onNavigate(it.route) },
            modifier = modifier,
            showLabels = showVerticalLabels,
            selectedColor = selectedColor,
            unselectedColor = unselectedColor,
            containerColor = containerColor,
            indicatorColor = indicatorColor,
            contentPadding = verticalContentPadding,
            width = verticalWidth,
            shape = verticalShape,
            indicatorThickness = indicatorThickness,
            animationDuration = animationDuration
        )

        else -> Unit
    }
}

@Composable
fun <T> BottomTabItem(
    item: BottomNavigationItem<T>,
    isSelected: Boolean,
    showLabel: Boolean,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: Dp = 28.dp,
    spacing: Dp = 4.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    val color by animateColorAsState(
        targetValue = if (isSelected) selectedColor else unselectedColor,
        animationSpec = tween(250),
        label = "bottomTabColor"
    )
    val label = item.label.asAnnotatedString()
    val icon = if (isSelected) item.selectedIcon else item.icon

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                item.badgeCount?.takeIf { it > 0 }?.let {
                    Badge {
                        Text(if (it > 99) "99+" else it.toString())
                    }
                }
            }) {
            icon.Icon(
                modifier = Modifier.size(iconSize),
                tint = color,
                contentDescription = label.text
            )
        }
        if (showLabel) {
            Spacer(Modifier.height(spacing))
            Text(
                text = label,
                color = color,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun <T> HorizontalBottomBarTabs(
    tabs: List<BottomNavigationItem<T>>,
    selectedItem: Int,
    onTabSelected: (BottomNavigationItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    indicatorColor: Color = selectedColor,
    dividerColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    itemPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    iconSize: Dp = 28.dp,
    indicatorThickness: Dp = 4.dp,
    animationDuration: Int = 300
) {
    val layoutDirection = LocalLayoutDirection.current
    val animatedIndex by animateFloatAsState(
        targetValue = selectedItem.coerceAtLeast(0).toFloat(),
        animationSpec = tween(animationDuration),
        label = "horizontalIndicator"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .navigationBarsPadding()
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(indicatorThickness)
        ) {
            drawLine(
                color = dividerColor, start = Offset.Zero, end = Offset(size.width, 0f)
            )
            if (selectedItem >= 0 && tabs.isNotEmpty()) {
                val tabWidth = size.width / tabs.size
                val left = if (layoutDirection == LayoutDirection.Ltr) {
                    tabWidth * animatedIndex
                } else {
                    size.width - tabWidth * (animatedIndex + 1f)
                }
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(left, 0f),
                    size = Size(tabWidth, indicatorThickness.toPx()),
                    cornerRadius = CornerRadius(indicatorThickness.toPx())
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            tabs.forEachIndexed { index, item ->
                BottomTabItem(
                    item = item,
                    isSelected = selectedItem == index,
                    showLabel = showLabels,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    iconSize = iconSize,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = item.enabled, onClick = { onTabSelected(item) })
                        .padding(itemPadding)
                )
            }
        }
    }
}

@Composable
fun <T> VerticalBottomBarTabs(
    tabs: List<BottomNavigationItem<T>>,
    selectedItem: Int,
    onTabSelected: (BottomNavigationItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = false,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    indicatorColor: Color = selectedColor,
    contentPadding: PaddingValues = PaddingValues(
        start = 8.dp, top = 24.dp, bottom = 12.dp
    ),
    itemPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    width: Dp = 64.dp,
    shape: Shape = MaterialTheme.shapes.large,
    iconSize: Dp = 28.dp,
    indicatorThickness: Dp = 4.dp,
    animationDuration: Int = 300
) {
    val animatedIndex by animateFloatAsState(
        targetValue = selectedItem.coerceAtLeast(0).toFloat(),
        animationSpec = tween(animationDuration),
        label = "verticalIndicator"
    )

    Box(
        modifier = modifier
            .width(width)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(contentPadding)
            .background(containerColor, shape)
    ) {
        Column {
            tabs.forEachIndexed { index, item ->
                BottomTabItem(
                    item = item,
                    isSelected = selectedItem == index,
                    showLabel = showLabels,
                    selectedColor = selectedColor,
                    unselectedColor = unselectedColor,
                    iconSize = iconSize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable(
                            enabled = item.enabled, onClick = { onTabSelected(item) })
                        .padding(itemPadding)
                )
            }
        }
        if (selectedItem >= 0 && tabs.isNotEmpty()) {
            Canvas(Modifier.matchParentSize()) {
                val itemHeight = size.height / tabs.size
                drawRoundRect(
                    color = indicatorColor,
                    topLeft = Offset(0f, itemHeight * animatedIndex),
                    size = Size(indicatorThickness.toPx(), itemHeight),
                    cornerRadius = CornerRadius(indicatorThickness.toPx())
                )
            }
        }
    }
}