package dedeadend.dterminal.ui.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.domin.CommandExecutor
import dedeadend.dterminal.domin.TerminalMessage
import dedeadend.dterminal.domin.TerminalState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    var state by mutableStateOf(TerminalState.Idle)
        private set

    var toolsMenu by mutableStateOf(false)
        private set

    var isRoot by mutableStateOf(false)
        private set

    var command by mutableStateOf("")
        private set

    private val _output = MutableStateFlow<List<TerminalMessage>>(emptyList())
    val output = _output.asStateFlow()


    fun toggleToolsMenu(show: Boolean) {
        toolsMenu = show
    }

    fun toggleRoot() {
        isRoot = !isRoot
    }


    fun onCommandChange(newCommand: String) {
        command = newCommand
    }

    fun clearOutput() {
        _output.update { emptyList() }
    }

    fun execute() {
        viewModelScope.launch {
            state = TerminalState.Running
            val cmd = command
            command = ""
            try {
                commandExecutor.execute(cmd, isRoot).flowOn(ioDispatcher).collect { message ->
                    _output.update { it + message }
                }
            } catch (e: Exception) {
                _output.update {
                    it + TerminalMessage(
                        TerminalState.Error,
                        e.message ?: "Unknown Error"
                    )
                }
            } finally {
                state = TerminalState.Idle
            }
        }
    }

    fun terminate() = _output.update { it + commandExecutor.cancel() }
}