package dedeadend.dterminal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dedeadend.dterminal.domin.AppDestinations
import dedeadend.dterminal.domin.UiEvent
import dedeadend.dterminal.ui.history.History
import dedeadend.dterminal.ui.main.MainViewModel
import dedeadend.dterminal.ui.script.Script
import dedeadend.dterminal.ui.terminal.Terminal

@Composable
fun AppNavigation(navController: NavHostController, mainVM: MainViewModel) {
    LaunchedEffect(Unit) {
        mainVM.navigationEvent.collect { event ->
            if (event is UiEvent.Navigate)
                navController.navigate(event.route) {
                    popUpTo(AppDestinations.TERMINAL.name)
                    launchSingleTop = true
                }
        }
    }
    NavHost(navController = navController, startDestination = AppDestinations.TERMINAL.name) {
        composable(AppDestinations.TERMINAL.name) {
            Terminal(terminalCommand = mainVM.terminalCommand)
        }
        composable(AppDestinations.HISTORY.name) {
            History(onHistoryItemClick = { command ->
                mainVM.onHistoryItemClicked(command)
            })
        }
        composable(AppDestinations.Scripts.name) {
            Script()
        }
    }
}
