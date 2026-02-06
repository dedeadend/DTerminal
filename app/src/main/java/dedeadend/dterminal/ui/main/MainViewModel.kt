package dedeadend.dterminal.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.domin.AppDestinations
import dedeadend.dterminal.domin.UiEvent
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private var _navigationEvent = Channel<UiEvent>(Channel.CONFLATED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var _terminalCommand = Channel<String>(Channel.CONFLATED)
    val terminalCommand = _terminalCommand.receiveAsFlow()

    fun onHistoryItemClicked(command: String) {
        viewModelScope.launch {
            _terminalCommand.send(command)
            _navigationEvent.send(UiEvent.Navigate(AppDestinations.TERMINAL.name))
        }
    }
}
