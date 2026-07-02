package dedeadend.dterminal.ui.history

import dedeadend.dterminal.domain.model.History

sealed interface HistoryUiEvent {
    data class DeleteHistoryItem(val history: History) : HistoryUiEvent
    object UndoDeleteHistoryItems : HistoryUiEvent
    object ClearHistory : HistoryUiEvent
}