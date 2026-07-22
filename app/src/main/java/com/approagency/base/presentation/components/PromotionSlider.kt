package com.approagency.base.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.approagency.base.R
import com.approagency.base.config.ApproConfig
import com.approagency.base.model.ui.Promotion
import com.approagency.base.model.ui.UiState
import com.approagency.base.theme.LocalBaseActivity
import com.approagency.base.utils.openLink
import com.approagency.base.utils.pageOffset
import kotlinx.coroutines.delay
import org.koin.compose.koinInject


@Composable
fun PromotionSliderState(
    state: UiState<List<Promotion>>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    loadingContent: (@Composable BoxScope.() -> Unit)? = {
        ShimmerContainer(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .height(250.dp)
                .clip(MaterialTheme.shapes.medium)
        )
    },
    errorContent: (@Composable BoxScope.(onRetry: () -> Unit) -> Unit)? = { retry ->
        FilledTextButton(
            text = stringResource(R.string.retry), onClick = retry
        )
    },
    emptyContent: (@Composable BoxScope.() -> Unit)? = null,
    sliderContent: @Composable (List<Promotion>) -> Unit = { promotions ->
        PromotionSlider(
            items = promotions, userScrollEnabled = userScrollEnabled
        )
    }
) {
    when (state) {
        is UiState.Idle, is UiState.Loading -> {
            loadingContent?.let { content ->
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center,
                    content = content
                )
            }
        }

        is UiState.Error -> {
            errorContent?.let { content ->
                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    content(onRetry)
                }
            }
        }

        is UiState.Success -> {
            if (state.data.isNotEmpty()) {
                Box(modifier = modifier) {
                    sliderContent(state.data)
                }
            } else {
                emptyContent?.let { content ->
                    Box(
                        modifier = modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                        content = content
                    )
                }
            }
        }
    }
}


@Composable
fun PromotionSlider(
    items: List<Promotion>,
    modifier: Modifier = Modifier,
    userScrollEnabled: Boolean = true,
    autoScrollEnabled: Boolean = true,
    autoScrollDelay: Long = 5_000L,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp),
    pageSpacing: Dp = (-16).dp,
    verticalSpacing: Dp = 8.dp,
    itemHeight: Dp = 200.dp,
    itemShape: Shape = MaterialTheme.shapes.medium,
    itemElevation: Dp = 8.dp,
    itemBackgroundColor: Color = MaterialTheme.colorScheme.background,
    inactiveScale: Float = 0.85f,
    activeScale: Float = 1f,
    inactiveAlpha: Float = 0.5f,
    activeAlpha: Float = 1f,
    showIndicator: Boolean = items.size > 1,
    indicatorAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    approConfig: ApproConfig = koinInject(),
    onPromotionClick: ((Promotion) -> Unit)? = null,
    emptyContent: @Composable BoxScope.() -> Unit = {},
    promotionContent: @Composable ColumnScope.(
        promotion: Promotion, onClick: () -> Unit
    ) -> Unit = { promotion, onClick ->
        NetworkImage(
            imageUrl = promotion.imageUrl,
            contentDescription = promotion.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp, vertical = 4.dp
                ), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = promotion.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )

            FilledTextButton(
                text = promotion.actionText, onClick = onClick
            )
        }
    },
    indicator: @Composable (
        pageCount: Int, currentPage: Int
    ) -> Unit = { pageCount, currentPage ->
        PromotionSliderIndicator(
            pageCount = pageCount, currentPage = currentPage
        )
    }
) {
    val activity = LocalBaseActivity.current

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(), content = emptyContent
        )
        return
    }

    val pagerState = rememberPagerState(
        pageCount = items::size
    )

    val openPromotion = remember(
        activity, approConfig, onPromotionClick
    ) {
        { promotion: Promotion ->
            if (onPromotionClick != null) {
                onPromotionClick(promotion)
            } else {
                val link = if (approConfig.isBazaar()) {
                    promotion.urlBazzar
                } else if (approConfig.isMyket()) {
                    promotion.urlMyket
                } else if (approConfig.isGooglePlay()) {
                    promotion.urlGooglePlay
                } else {
                    promotion.urlSite
                }

                (link ?: promotion.urlSite)?.let(activity::openLink)
            }
        }
    }

    LaunchedEffect(
        autoScrollEnabled, autoScrollDelay, items.size, pagerState
    ) {
        if (!autoScrollEnabled || items.size <= 1) {
            return@LaunchedEffect
        }

        while (true) {
            delay(autoScrollDelay)

            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = (pagerState.currentPage + 1) % items.size
                )
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = contentPadding,
            pageSpacing = pageSpacing,
            userScrollEnabled = userScrollEnabled,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val promotion = items[page]
            val pageOffset = pagerState.pageOffset(page)

            Column(modifier = Modifier
                .graphicsLayer {
                    scaleX = lerp(
                        inactiveScale, activeScale, 1f - pageOffset
                    )
                    scaleY = lerp(
                        inactiveScale, activeScale, 1f - pageOffset
                    )
                    alpha = lerp(
                        inactiveAlpha, activeAlpha, 1f - pageOffset
                    )
                }
                .shadow(
                    elevation = itemElevation, shape = itemShape
                )
                .background(
                    color = itemBackgroundColor, shape = itemShape
                )
                .clickable {
                    openPromotion(promotion)
                }
                .fillMaxWidth()) {
                promotionContent(
                    promotion, { openPromotion(promotion) })
            }
        }

        if (showIndicator) {
            Box(
                modifier = Modifier.align(indicatorAlignment)
            ) {
                indicator(
                    items.size, pagerState.currentPage
                )
            }
        }
    }
}

@Composable
private fun PromotionSliderIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    selectedWidth: Dp = 32.dp,
    unselectedWidth: Dp = 8.dp,
    height: Dp = 8.dp,
    spacing: Dp = 2.dp,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 4.dp,
    elevation: Dp = 8.dp,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.tertiary,
    containerShape: Shape = MaterialTheme.shapes.medium,
    indicatorShape: Shape = CircleShape
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation, shape = containerShape
            )
            .background(
                color = containerColor, shape = containerShape
            )
            .padding(
                horizontal = horizontalPadding, vertical = verticalPadding
            ), contentAlignment = Alignment.Center
    ) {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                spacing, Alignment.CenterHorizontally
            )
        ) {
            items(pageCount) { page ->
                val selected = page == currentPage

                val width by animateDpAsState(
                    targetValue = if (selected) {
                        selectedWidth
                    } else {
                        unselectedWidth
                    }, label = "promotionIndicatorWidth"
                )

                Box(
                    modifier = Modifier
                        .height(height)
                        .width(width)
                        .background(
                            color = if (selected) {
                                selectedColor
                            } else {
                                unselectedColor
                            }, shape = indicatorShape
                        )
                )
            }
        }
    }
}