package dedeadend.dterminal.ui.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.data.Repository
import dedeadend.dterminal.domain.CommandExecutor
import dedeadend.dterminal.domain.History
import dedeadend.dterminal.domain.TerminalLog
import dedeadend.dterminal.domain.TerminalState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val ioDispatcher: CoroutineDispatcher,
    private val repository: Repository
) : ViewModel() {

    val logs = repository.getLogs()
        .flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    var state by mutableStateOf(TerminalState.Idle)
        private set

    var toolsMenu by mutableStateOf(false)
        private set

    var isRoot by mutableStateOf(false)
        private set

    var command by mutableStateOf("")
        private set

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
        viewModelScope.launch(ioDispatcher) {
            repository.deleteLogs()
        }
    }

    fun execute() {
        if (command.trim().isEmpty())
            return
        state = TerminalState.Running
        viewModelScope.launch(ioDispatcher) {
            val cmd = command.trim()
            command = ""
            repository.insertToHistory(History(cmd))
            repository.insertToLogs(
                TerminalLog(
                    TerminalState.Info,
                    (if (isRoot) "#: " else "$: ") + cmd
                )
            )
            try {
                commandExecutor.execute(cmd, isRoot).collect { log ->
                    repository.insertToLogs(log)
                }
            } catch (e: Exception) {
                repository.insertToLogs(
                    TerminalLog(
                        TerminalState.Error,
                        e.message ?: "Unknown error"
                    )
                )
            } finally {
                withContext(NonCancellable + Dispatchers.Main) {
                    state = TerminalState.Idle
                }
            }
        }
    }

    fun terminate() =
        viewModelScope.launch(ioDispatcher) { repository.insertToLogs(commandExecutor.cancel()) }
}

fun terminalLog2String(terminalLog: TerminalLog): String {
    return if (terminalLog.state == TerminalState.Info)
        "\n\n" + SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()
        ).format(terminalLog.date) + "\n" + terminalLog.message
    else
        terminalLog.message

}