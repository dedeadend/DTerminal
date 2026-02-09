package dedeadend.dterminal.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.data.Repository
import dedeadend.dterminal.domin.History
import dedeadend.dterminal.domin.UiEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: Repository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    val history = repository.getHistory()
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private var historyBackup: List<History>? = null

    private var _eventFlow = Channel<UiEvent>(Channel.RENDEZVOUS)
    val eventFlow = _eventFlow.receiveAsFlow()


    fun clearHistory() {
        viewModelScope.launch(ioDispatcher) {
            historyBackup = history.value.toList()
            repository.deleteAllHistory()
            _eventFlow.send(UiEvent.ShowSnackbar("History Cleared", "Undo"))
        }
    }

    fun undoDeleteHistoryItems() {
        viewModelScope.launch {
            historyBackup?.let {
                repository.restoreHistory(historyBackup!!)
                historyBackup = null
            }
        }
    }

    fun deleteHistoryCommand(historyCommand: History) {
        viewModelScope.launch(ioDispatcher) {
            historyBackup = listOf(historyCommand)
            repository.deleteHistoryWithId(historyCommand.id)
            _eventFlow.send(UiEvent.ShowSnackbar("History Item Deleted", "Undo"))
        }
    }
}