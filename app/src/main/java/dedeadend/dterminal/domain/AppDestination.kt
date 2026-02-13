package dedeadend.dterminal.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestinations(
    val icon: ImageVector
) {
    Scripts(Icons.Default.DensitySmall),
    TERMINAL(Icons.Default.Code),
    HISTORY(Icons.Default.History)
}
