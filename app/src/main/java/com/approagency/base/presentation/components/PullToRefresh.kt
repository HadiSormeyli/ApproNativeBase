package com.approagency.base.presentation.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullToRefresh(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    refreshDelay: Long = 500L,
    state: PullToRefreshState = rememberPullToRefreshState(),
    indicatorAlignment: Alignment = Alignment.TopCenter,
    indicatorContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    indicator: @Composable BoxScope.(
        state: PullToRefreshState,
        isRefreshing: Boolean
    ) -> Unit = { pullState, refreshing ->
        Indicator(
            modifier = Modifier.align(indicatorAlignment),
            isRefreshing = refreshing,
            containerColor = indicatorContainerColor,
            color = indicatorColor,
            state = pullState
        )
    },
    content: @Composable BoxScope.() -> Unit
) {
    var isRefreshing by rememberSaveable {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        state = state,
        onRefresh = {
            if (enabled && !isRefreshing) {
                scope.launch {
                    isRefreshing = true

                    try {
                        onRefresh()
                        delay(refreshDelay)
                    } finally {
                        isRefreshing = false
                    }
                }
            }
        },
        indicator = {
            indicator(state, isRefreshing)
        },
        content = content
    )
}