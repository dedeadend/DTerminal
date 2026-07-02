package dedeadend.dterminal.ui.history

import androidx.compose.runtime.Immutable
import dedeadend.dterminal.domain.model.History

@Immutable
data class HistoryUiState(
    val history: List<History> = emptyList(),
    val historyBackup: List<History> = emptyList(),
    val isLoading: Boolean = true
) {
    val showEmptyState get() = history.isEmpty() && !isLoading
}