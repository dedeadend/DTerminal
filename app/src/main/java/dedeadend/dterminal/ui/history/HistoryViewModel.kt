package dedeadend.dterminal.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.model.History
import dedeadend.dterminal.domain.repository.HistoryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<HistoryUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            historyRepository.getHistory().flowOn(dispatchers.io).collect { history ->
                _uiState.update { it.copy(history = history, isLoading = false) }
            }
        }
    }

    fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.ClearHistory -> clearHistory()
            is HistoryUiEvent.DeleteHistoryItem -> deleteHistoryItem(event.history)
            is HistoryUiEvent.UndoDeleteHistoryItems -> undoDeleteHistoryItems()
        }
    }

    private fun clearHistory() {
        viewModelScope.launch(dispatchers.io) {
            if (uiState.value.history.isNotEmpty()) {
                _uiState.update { it.copy(historyBackup = it.history.toList()) }
                historyRepository.clearHistory()
                _uiEffect.send(HistoryUiEffect.ShowSnackbar("History Cleared", "Undo"))
            }
        }
    }

    private fun deleteHistoryItem(history: History) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(historyBackup = it.history.toList()) }
            historyRepository.deleteHistoryWithId(history.id)
            _uiEffect.send(HistoryUiEffect.ShowSnackbar("History Item Deleted", "Undo"))
        }
    }

    private fun undoDeleteHistoryItems() {
        viewModelScope.launch {
            if (_uiState.value.historyBackup.isNotEmpty()) {
                historyRepository.restoreHistory(_uiState.value.historyBackup)
                _uiState.update { it.copy(historyBackup = emptyList()) }
            }
        }
    }
}