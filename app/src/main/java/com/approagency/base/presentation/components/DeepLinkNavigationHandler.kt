package com.approagency.base.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.approagency.base.config.ApproConfig
import com.approagency.base.presentation.navigation.navigateDeepLink
import com.approagency.base.theme.LocalBaseActivity
import com.approagency.base.utils.DeepLinkManager
import com.approagency.base.utils.openLink
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun DeepLinkNavigationHandler(
    navController: NavController,
    deepLinkManager: DeepLinkManager = koinInject(),
    config: ApproConfig = koinInject()
) {
    val activity = LocalBaseActivity.current

    LaunchedEffect(
        navController,
        deepLinkManager
    ) {
        deepLinkManager.events.collectLatest { event ->
            if (config.isDeepLink(event.input.uri)) {
                if (event.target != null) {
                    navController.navigateDeepLink(event.target)
                }
            } else {
                activity.openLink(event.input.uri)
            }

            deepLinkManager.consume(event.id)
        }
    }
}