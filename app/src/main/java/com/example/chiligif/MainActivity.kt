package com.example.chiligif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chiligif.ui.components.NetworkStatusBanner
import com.example.chiligif.ui.screen.detail.DetailScreen
import com.example.chiligif.ui.screen.search.SearchScreen
import com.example.chiligif.ui.theme.ChiligifTheme
import com.example.chiligif.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @androidx.compose.animation.ExperimentalSharedTransitionApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChiligifTheme {
                ChiligifApp(networkMonitor = networkMonitor)
            }
        }
    }
}

@androidx.compose.animation.ExperimentalSharedTransitionApi
@Composable
fun ChiligifApp(networkMonitor: NetworkMonitor) {
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
                        animatedVisibilityScope = this
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
