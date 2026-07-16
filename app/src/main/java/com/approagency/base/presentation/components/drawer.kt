package com.approagency.base.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.approagency.base.model.ui.DrawerColors
import com.approagency.base.model.ui.DrawerItem
import com.approagency.base.utils.Icon
import com.approagency.base.utils.applyEnabledAlpha
import com.approagency.base.utils.asAnnotatedString
import com.approagency.base.utils.resolveColor

@Composable
fun DrawerContent(
    items: List<DrawerItem>,
    modifier: Modifier = Modifier,
    progress: Float = 0f,
    colors: DrawerColors = DrawerColors.defaultColors(),
    shape: Shape = RoundedCornerShape(
        topEnd = 16.dp * progress, bottomEnd = 16.dp * progress
    ),
    shadowElevation: Dp = 8.dp * progress,
    shadowColor: Color = colors.contentColor.copy(alpha = 0.9f),
    verticalPadding: Dp = 8.dp * progress,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 8.dp, vertical = 4.dp
    ),
    itemSpacing: Dp = 4.dp,
    itemShape: Shape = MaterialTheme.shapes.medium,
    itemPadding: PaddingValues = PaddingValues(
        horizontal = 8.dp, vertical = 10.dp
    ),
    itemIconSize: Dp = 24.dp,
    itemIconSpacing: Dp = 8.dp,
    itemTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    childTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    dividerPadding: PaddingValues = PaddingValues(vertical = 4.dp),
    header: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = verticalPadding)
            .shadow(
                elevation = shadowElevation,
                ambientColor = shadowColor,
                spotColor = shadowColor,
                shape = shape
            )
            .background(colors.containerColor, shape)
            .clip(shape)
    ) {
        header?.invoke(this)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            itemsIndexed(
                items = items,
                key = { index, item -> "${item::class.simpleName}-$index" }) { _, item ->
                when (item) {
                    is DrawerItem.Simple -> SimpleDrawerItem(
                        item = item,
                        colors = colors,
                        shape = itemShape,
                        contentPadding = itemPadding,
                        iconSize = itemIconSize,
                        iconSpacing = itemIconSpacing,
                        textStyle = itemTextStyle
                    )

                    is DrawerItem.DropDown -> DropdownDrawerItem(
                        item = item,
                        colors = colors,
                        shape = itemShape,
                        contentPadding = itemPadding,
                        iconSize = itemIconSize,
                        iconSpacing = itemIconSpacing,
                        textStyle = itemTextStyle,
                        childTextStyle = childTextStyle
                    )

                    is DrawerItem.Divider -> HorizontalDivider(
                        modifier = Modifier.padding(dividerPadding),
                        color = item.color.resolveColor(colors.dividerColor)
                    )
                }
            }
        }

        footer?.invoke(this)
    }
}

@Composable
fun SimpleDrawerItem(
    item: DrawerItem.Simple,
    modifier: Modifier = Modifier,
    colors: DrawerColors = DrawerColors.defaultColors(),
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 8.dp, vertical = 10.dp
    ),
    iconSize: Dp = 24.dp,
    iconSpacing: Dp = 8.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val defaultContainerColor = if (item.selected) {
        colors.selectedItemContainerColor
    } else {
        colors.itemContainerColor
    }
    val defaultContentColor = if (item.selected) {
        colors.selectedItemContentColor
    } else {
        colors.itemContentColor
    }
    val containerColor = item.backgroundColor.resolveColor(defaultContainerColor)
    val contentColor = item.foregroundColor.resolveColor(defaultContentColor)
        .applyEnabledAlpha(item.enabled, colors.disabledAlpha)
    val label = item.title.asAnnotatedString()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, shape)
            .clip(shape)
            .clickable(
                enabled = item.enabled, onClick = item.onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.icon?.Icon(
            tint = contentColor, contentDescription = label.text, modifier = Modifier.size(iconSize)
        )

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = contentColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        item.badgeCount?.takeIf { it > 0 }?.let {
            Badge(
                containerColor = colors.badgeContainerColor, contentColor = colors.badgeContentColor
            ) {
                Text(if (it > 99) "99+" else it.toString())
            }
        }

        trailingContent?.invoke(this)
    }
}

@Composable
fun DropdownDrawerItem(
    item: DrawerItem.DropDown,
    modifier: Modifier = Modifier,
    colors: DrawerColors = DrawerColors.defaultColors(),
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 8.dp, vertical = 10.dp
    ),
    iconSize: Dp = 24.dp,
    iconSpacing: Dp = 8.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    childTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    childrenSpacing: Dp = 4.dp,
    childrenStartPadding: Dp = 24.dp,
    indicatorSize: Dp = 4.dp,
    showChildIndicator: Boolean = true,
    expandAnimationDuration: Int = 250
) {
    var expanded by remember(item) {
        mutableStateOf(item.expanded)
    }

    LaunchedEffect(item.expanded) {
        expanded = item.expanded
    }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(expandAnimationDuration),
        label = "drawerDropDownRotation"
    )
    val containerColor = item.backgroundColor.resolveColor(colors.itemContainerColor)
    val contentColor = item.foregroundColor.resolveColor(colors.itemContentColor)
        .applyEnabledAlpha(item.enabled, colors.disabledAlpha)
    val label = item.title.asAnnotatedString()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(childrenSpacing)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(containerColor, shape)
                .clip(shape)
                .clickable(
                    enabled = item.enabled, onClick = {
                        expanded = !expanded
                        item.onClick()
                    })
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(iconSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.icon?.Icon(
                tint = contentColor,
                contentDescription = label.text,
                modifier = Modifier.size(iconSize)
            )

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                color = contentColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = label.text,
                modifier = Modifier
                    .size(iconSize)
                    .rotate(rotation),
                tint = contentColor
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(childrenSpacing)
            ) {
                item.children.forEach { child ->
                    DrawerChildItem(
                        item = child,
                        colors = colors,
                        shape = shape,
                        contentPadding = contentPadding,
                        iconSize = iconSize,
                        iconSpacing = iconSpacing,
                        textStyle = childTextStyle,
                        startPadding = childrenStartPadding,
                        indicatorSize = indicatorSize,
                        showIndicator = showChildIndicator
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerChildItem(
    item: DrawerItem.Simple,
    colors: DrawerColors,
    shape: Shape,
    contentPadding: PaddingValues,
    iconSize: Dp,
    iconSpacing: Dp,
    textStyle: TextStyle,
    startPadding: Dp,
    indicatorSize: Dp,
    showIndicator: Boolean
) {
    val defaultContainerColor = if (item.selected) {
        colors.selectedItemContainerColor
    } else {
        colors.itemContainerColor
    }
    val defaultContentColor = if (item.selected) {
        colors.selectedItemContentColor
    } else {
        colors.itemContentColor
    }
    val containerColor = item.backgroundColor.resolveColor(defaultContainerColor)
    val contentColor = item.foregroundColor.resolveColor(defaultContentColor)
        .applyEnabledAlpha(item.enabled, colors.disabledAlpha)
    val label = item.title.asAnnotatedString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding)
            .background(containerColor, shape)
            .clip(shape)
            .clickable(
                enabled = item.enabled, onClick = item.onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showIndicator && item.icon == null) {
            Box(
                modifier = Modifier
                    .size(indicatorSize)
                    .background(
                        color = contentColor.copy(alpha = 0.5f), shape = CircleShape
                    )
            )
        }

        item.icon?.Icon(
            tint = contentColor, contentDescription = label.text, modifier = Modifier.size(iconSize)
        )

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = contentColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        item.badgeCount?.takeIf { it > 0 }?.let {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = colors.badgeContainerColor,
                        contentColor = colors.badgeContentColor
                    ) {
                        Text(if (it > 99) "99+" else it.toString())
                    }
                }) {}
        }
    }
}