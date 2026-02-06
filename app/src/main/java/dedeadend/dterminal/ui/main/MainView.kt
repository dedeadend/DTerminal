package dedeadend.dterminal.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dedeadend.dterminal.domin.AppDestinations
import dedeadend.dterminal.navigation.AppNavigation

@Composable
fun Main(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    //HandleNavigationState(navController, viewModel)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.name
                        )
                    },
                    label = { Text(it.name) },
                    selected = navBackStackEntry?.destination?.route == it.name,
                    onClick = {
                        navController.navigate(it.name) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        AppNavigation(navController, viewModel)
    }
}
/*
@Composable
fun HandleNavigationState(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    LaunchedEffect(viewModel.currentScreen) {
        if (navController.currentDestination?.route != viewModel.currentScreen.name) {
            navController.navigate(viewModel.currentScreen.name) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        val route = navBackStackEntry?.destination?.route
        if (route != null) {
            val screen = AppDestinations.valueOf(route)
            if (viewModel.currentScreen != screen)
                viewModel.navigateTO(screen)
        }
    }
}*/