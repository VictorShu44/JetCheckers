package com.example.jetcheckers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.Companion
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.example.jetcheckers.di.EntryProviderInstaller
import com.example.jetcheckers.di.Navigator
import com.example.jetcheckers.navigation.NavigateBackButton
import com.example.jetcheckers.navigation.NavigateButton
import com.example.jetcheckers.ui.theme.JetCheckersTheme
import kotlinx.serialization.Serializable
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetCheckersTheme {
                JetCheckersApp(navigator = navigator, entryProviderScopes = entryProviderScopes )
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun JetCheckersApp(navigator: Navigator,entryProviderScopes: Set<@JvmSuppressWildcards EntryProviderInstaller> ) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val navSuiteType =
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())

 //   val backStack = rememberNavBackStack(CatList)
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

            SharedTransitionLayout {
                NavDisplay(
                    backStack = navigator.backStack,
                    modifier = Modifier.padding(innerPadding),
                    onBack = { navigator.goBack() },
                    entryProvider = entryProvider {
                        entryProviderScopes.forEach { builder -> this.builder() }
                    }
                    /*backStack = backStack,
                    onBack = { backStack.removeAt(backStack.lastIndex) },
                    entryProvider =
                        entryProvider {
                            entry<CatList> {
                                CatList(this@SharedTransitionLayout) { cat ->
                                    backStack.add(CatDetail(cat))
                                }
                            }
                            entry<CatDetail> { args ->
                                CatDetail(
                                    args.cat,
                                    this@SharedTransitionLayout
                                ) {
                                    backStack.removeAt(backStack.lastIndex)
                                }
                            }
                        },*/
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JetCheckersTheme {
        Greeting("Android")
    }
}

@Serializable object CatList : NavKey

@Serializable data class CatDetail(val cat: Cat) : NavKey

@Serializable
data class Cat(@DrawableRes val imageId: Int, val name: String, val description: String)

private val catList: List<Cat> =
    listOf(
        Cat(R.drawable.cat_1, "happy", "cat lying down"),
        Cat(R.drawable.cat_2, "lucky", "cat playing"),
        Cat(R.drawable.cat_3, "chocolate cake", "cat upside down"),
    )

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CatList(sharedScope: SharedTransitionScope, onClick: (cat: Cat) -> Unit) {
    Column {
        catList.forEach { cat: Cat ->
            Row(Modifier.clickable { onClick(cat) }) {
                with(sharedScope) {
                    val imageModifier =
                        Modifier.size(100.dp)
                            .sharedElement(
                                sharedScope.rememberSharedContentState(key = cat.imageId),
                                animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            )
                    Image(painterResource(cat.imageId), cat.description, imageModifier)
                    Text(cat.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CatDetail(cat: Cat, sharedScope: SharedTransitionScope, onBack: () -> Unit) {
    Column {
        Box {
            with(sharedScope) {
                val imageModifier =
                    Modifier.size(300.dp)
                        .sharedElement(
                            sharedScope.rememberSharedContentState(key = cat.imageId),
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                        )
                Image(painterResource(cat.imageId), cat.description, imageModifier)
            }
        }
        Text(cat.name)
        Text(cat.description)
        NavigateBackButton(onBack)
    }
}

@Composable
fun HomeScreen(backStackString: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxSize().then(Modifier.padding(8.dp))) {
        Text(text = "Home Page")
        Text(text = "current backStack:$backStackString")
        NavigateButton("Detail", onClick)
    }
}

@Composable
fun UserScreen(backStackString: String, onClick: () -> Unit) {
    Column(Modifier.fillMaxSize().then(Modifier.padding(8.dp))) {
        Text(text = "User Page")
        Text(text = "current backStack:$backStackString")
        NavigateButton("Detail", onClick)
    }
}

@Composable
fun DetailScreen(backStackString: String, sourceTab: String) {
    Column(Modifier.fillMaxSize().then(Modifier.padding(8.dp))) {
        Text(text = "Detail Page $sourceTab")
        Text(text = "current backStack:$backStackString")
    }
}