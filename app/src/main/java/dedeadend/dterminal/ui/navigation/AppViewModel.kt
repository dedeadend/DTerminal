package dedeadend.dterminal.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor() : ViewModel() {
    private var _terminalCommandChannel = Channel<String>(Channel.BUFFERED)
    val terminalCommandChannel = _terminalCommandChannel.receiveAsFlow()

    fun onItemExecuteClicked(command: String) {
        viewModelScope.launch {
            _terminalCommandChannel.send(command)
        }
    }
}