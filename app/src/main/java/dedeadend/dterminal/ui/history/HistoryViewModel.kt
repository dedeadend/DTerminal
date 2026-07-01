package dedeadend.dterminal.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.UiEvent
import dedeadend.dterminal.domain.model.History
import dedeadend.dterminal.domain.repository.HistoryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {
    val history = historyRepository.getHistory()
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private var historyBackup: List<History>? = null

    private var _eventFlow = Channel<UiEvent>(Channel.RENDEZVOUS)
    val eventFlow = _eventFlow.receiveAsFlow()


    fun clearHistory() {
        viewModelScope.launch(dispatchers.io) {
            if (history.value.isNotEmpty()) {
                historyBackup = history.value.toList()
                historyRepository.clearHistory()
                _eventFlow.send(UiEvent.ShowSnackbar("History Cleared", "Undo"))
            }
        }
    }

    fun deleteHistoryItem(history: History) {
        viewModelScope.launch(dispatchers.io) {
            historyBackup = listOf(history)
            historyRepository.deleteHistoryWithId(history.id)
            _eventFlow.send(UiEvent.ShowSnackbar("History Item Deleted", "Undo"))
        }
    }

    fun undoDeleteHistoryItems() {
        viewModelScope.launch {
            historyBackup?.let {
                historyRepository.restoreHistory(it)
                historyBackup = null
            }
        }
    }
}