package dedeadend.dterminal.ui.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.domin.CommandExecutor
import dedeadend.dterminal.domin.TerminalMessage
import dedeadend.dterminal.domin.TerminalState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    var state by mutableStateOf<TerminalState>(TerminalState.Idle)
        private set

    var toolsMenu by mutableStateOf(false)
        private set

    var isRoot by mutableStateOf(false)
        private set

    var command by mutableStateOf("")
        private set

    private val _output = mutableStateListOf<TerminalMessage>()
    val output: List<TerminalMessage> = _output


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
        _output.clear()
    }

    fun execute() {
        viewModelScope.launch() {
            state = TerminalState.Running
            val cmd = command
            command = ""
            try {
                commandExecutor.execute(cmd, isRoot).flowOn(ioDispatcher).collect { message ->
                    _output.add(message)
                }
            } catch (e: Exception) {
                _output.add(
                    TerminalMessage(
                        TerminalState.Error,
                        e.message ?: "[An error occurred]"
                    )
                )
            } finally {
                state = TerminalState.Idle
            }
        }
    }

    fun terminate() = _output.add(commandExecutor.cancel())
}