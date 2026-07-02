package dedeadend.dterminal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dedeadend.dterminal.ui.history.History
import dedeadend.dterminal.ui.script.Script
import dedeadend.dterminal.ui.terminal.Terminal

@Composable
fun AppNavigation(
    navController: NavHostController,
    appViewModel: AppViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = AppDestinations.TERMINAL.name) {
        composable(AppDestinations.TERMINAL.name) {
            Terminal(terminalCommandChannel = appViewModel.terminalCommandChannel)
        }
        composable(AppDestinations.HISTORY.name) {
            History(onHistoryItemExecuteClick = { command ->
                appViewModel.onItemExecuteClicked(command)
                navController.navigate(AppDestinations.TERMINAL.name) {
                    popUpTo(AppDestinations.TERMINAL.name) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
        composable(AppDestinations.Scripts.name) {
            Script(onScriptItemExecuteClick = { command ->
                appViewModel.onItemExecuteClicked(command)
                navController.navigate(AppDestinations.TERMINAL.name) {
                    popUpTo(AppDestinations.TERMINAL.name) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            })
        }
    }
}
