package dedeadend.dterminal.domin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val icon: ImageVector
) {
    Scripts(Icons.Default.Favorite),
    TERMINAL(Icons.Default.Home),
    HISTORY(Icons.Default.DateRange)
}
