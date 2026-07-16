package com.approagency.base.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.approagency.base.theme.LocalBaseActivity
import org.koin.androidx.compose.koinViewModel

inline fun <reified T : Any> NavGraphBuilder.drawerAwareComposable(
    noinline enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    noinline exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    noinline popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    noinline popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T>(
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
    ) { backStackEntry ->
        val activity = LocalBaseActivity.current
        val drawerState = activity.drawerState

        BackHandler(
            enabled = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open
        ) {
            activity.closeDrawer()
        }

        content(backStackEntry)
    }
}

fun NavGraphBuilder.drawerAwareComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition
    ) { backStackEntry ->
        val activity = LocalBaseActivity.current
        val drawerState = activity.drawerState


        BackHandler(
            enabled = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open
        ) {
            activity.closeDrawer()
        }

        content(backStackEntry)
    }
}

@Composable
inline fun <reified T : ViewModel> NavController.sharedViewModel(): T {
    val navGraphRoute =
        this.graph.startDestinationRoute ?: return koinViewModel<T>()

    val parentEntry = getBackStackEntry(navGraphRoute)

    return koinViewModel<T>(viewModelStoreOwner = parentEntry)
}

@Composable
inline fun <reified T : ViewModel> NavController.sharedViewModel(route: String): T {
    val parentEntry = remember(currentBackStackEntry) {
        getBackStackEntry(route)
    }

    return koinViewModel<T>(viewModelStoreOwner = parentEntry)
}

fun NavController.popBackStackSafely() {
    currentBackStackEntry?.lifecycle?.let {
        if (it.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            popBackStack()
        }
    }
}

fun <T : Any> NavController.navigateSafely(route: T) {
    val currentRoute = currentBackStackEntry?.destination?.route

    if (route::class.java.canonicalName?.let { currentRoute?.startsWith(it) } == false) {
        navigate(route) {
            launchSingleTop = true
        }
    }
}

fun <T : Any> NavController.navigateAndClean(route: T, startDeputation: T) {
    navigate(route = route) {
        popUpTo(startDeputation) { inclusive = true }
        launchSingleTop = true
    }
}