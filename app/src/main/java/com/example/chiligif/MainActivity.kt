package com.example.chiligif

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chiligif.ui.components.NetworkStatusBanner
import com.example.chiligif.ui.screen.detail.DetailScreen
import com.example.chiligif.ui.screen.search.SearchScreen
import com.example.chiligif.ui.theme.ChiligifTheme
import com.example.chiligif.util.LocaleHelper
import com.example.chiligif.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, languageCode))
    }

    @androidx.compose.animation.ExperimentalSharedTransitionApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }
            val context = LocalContext.current

            ChiligifTheme(darkTheme = isDarkTheme) {
                ChiligifApp(
                    networkMonitor = networkMonitor,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onToggleLanguage = {
                        val newLang = LocaleHelper.toggleLanguage(context)
                        LocaleHelper.recreateActivity(this, newLang)
                    }
                )
            }
        }
    }
}

@androidx.compose.animation.ExperimentalSharedTransitionApi
@Composable
fun ChiligifApp(
    networkMonitor: NetworkMonitor,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    val navController = rememberNavController()
    
    Column(modifier = Modifier.fillMaxSize()) {
        NetworkStatusBanner(networkMonitor = networkMonitor)

        SharedTransitionLayout(modifier = Modifier.weight(1f)) {
            NavHost(
                navController = navController,
                startDestination = "search"
            ) {
                composable("search") {
                    SearchScreen(
                        onGifClick = { gifId ->
                            navController.navigate("detail/$gifId")
                        },
                        animatedVisibilityScope = this,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onToggleLanguage = onToggleLanguage
                    )
                }

                composable(
                    route = "detail/{gifId}",
                    arguments = listOf(
                        navArgument("gifId") {
                            type = NavType.StringType
                        }
                    )
                ) {
                    DetailScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        animatedVisibilityScope = this
                    )
                }
            }
        }
    }
}
