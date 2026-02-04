package dedeadend.dterminal.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dedeadend.dterminal.ui.main.MainViewModel
import dedeadend.dterminal.ui.terminal.Terminal

@Composable
fun AppNavigation(navController: NavHostController, mainVM: MainViewModel) {
    NavHost(navController = navController, startDestination = AppDestinations.TERMINAL.name) {
        AppDestinations.entries.forEach { screen ->
            composable(screen.name) {
                screen.GetContent(mainVM)
            }
        }
    }
}

enum class AppDestinations(
    val icon: ImageVector
) {
    TERMINAL(Icons.Default.Home),
    HISTORY(Icons.Default.DateRange);

    @Composable
    fun GetContent(mainViewModel: MainViewModel) {
        when (this) {
            TERMINAL -> Terminal(mainVM = mainViewModel)
            HISTORY -> Terminal(mainVM = mainViewModel) //test
        }
    }
}
