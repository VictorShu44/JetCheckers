package com.example.jetcheckers.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.example.jetcheckers.CatDetail
import com.example.jetcheckers.CatList


@Composable
fun SceneNav() {
    val backStack = rememberNavBackStack(Profile)
    val showDialog = remember { mutableStateOf(false) }
    NavDisplay(
        backStack = backStack,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        onBack = { backStack.removeAt(backStack.lastIndex) },
        entryProvider =
            entryProvider({ NavEntry(it) { Text(text = "Invalid Key") } }) {
                entry<Profile> {
                    val viewModel = viewModel<ProfileViewModel>()
                    Profile(viewModel, { backStack.add(it) }) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                entry<Scrollable> {
                    Scrollable({ backStack.add(it) }) { backStack.removeAt(backStack.lastIndex) }
                }
                entry<DialogBase> {
                    DialogBase(onClick = { showDialog.value = true }) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
                entry<Dashboard>(
                    metadata =
                        NavDisplay.predictivePopTransitionSpec { swipeEdge ->
                            if (swipeEdge == NavigationEvent.EDGE_RIGHT) {
                                slideInHorizontally(tween(700)) { it / 2 } togetherWith
                                        slideOutHorizontally(tween(700)) { -it / 2 }
                            } else {
                                slideInHorizontally(tween(700)) { -it / 2 } togetherWith
                                        slideOutHorizontally(tween(700)) { it / 2 }
                            }
                        }
                ) { dashboardArgs ->
                    val userId = dashboardArgs.userId
                    Dashboard(userId, onBack = { backStack.removeAt(backStack.lastIndex) })
                }
            },
    )
    if (showDialog.value) {
        DialogContent(onDismissRequest = { showDialog.value = false })
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)

@Composable
fun SceneNavSharedEntrySample() {

    /** The [SharedTransitionScope] of the [NavDisplay]. */
    val localNavSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
        compositionLocalOf {
            throw IllegalStateException(
                "Unexpected access to LocalNavSharedTransitionScope. You must provide a " +
                        "SharedTransitionScope from a call to SharedTransitionLayout() or " +
                        "SharedTransitionScope()"
            )
        }

    /**
     * A [NavEntryDecorator] that wraps each entry in a shared element that is controlled by the
     * [Scene].
     */
    val sharedEntryInSceneNavEntryDecorator =
        NavEntryDecorator<Any> { entry ->
            with(localNavSharedTransitionScope.current) {
                Box(
                    Modifier.sharedElement(
                        rememberSharedContentState(entry.contentKey),
                        animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                    )
                ) {
                    entry.Content()
                }
            }
        }

    val backStack = rememberNavBackStack(CatList)
    SharedTransitionLayout {
        CompositionLocalProvider(localNavSharedTransitionScope provides this) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeAt(backStack.lastIndex) },
                entryDecorators =
                    listOf(
                        sharedEntryInSceneNavEntryDecorator,
                        rememberSaveableStateHolderNavEntryDecorator(),
                    ),
                entryProvider =
                    entryProvider {
                        entry<CatList> {
                            CatList(this@SharedTransitionLayout) { cat ->
                                backStack.add(CatDetail(cat))
                            }
                        }
                        entry<CatDetail> { args ->
                            CatDetail(args.cat, this@SharedTransitionLayout) {
                                backStack.removeAt(backStack.lastIndex)
                            }
                        }
                    },
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)

@Composable
fun SceneNavSharedElementSample() {
    val backStack = rememberNavBackStack(CatList)
    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeAt(backStack.lastIndex) },
            entryProvider =
                entryProvider {
                    entry<CatList> {
                        CatList(this@SharedTransitionLayout) { cat ->
                            backStack.add(CatDetail(cat))
                        }
                    }
                    entry<CatDetail> { args ->
                        CatDetail(args.cat, this@SharedTransitionLayout) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                },
        )
    }
}