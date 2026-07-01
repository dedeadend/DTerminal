package dedeadend.dterminal.ui.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.model.Settings
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState
import dedeadend.dterminal.domain.repository.CommandExecutor
import dedeadend.dterminal.domain.repository.SettingsRepository
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val dispatchers: AppDispatchers,
    private val settingsRepository: SettingsRepository,
    private val terminalLogRepository: TerminalLogRepository
) : ViewModel() {

    val settings = settingsRepository.getSystemSettings()
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Lazily, Settings())

    val logs = terminalLogRepository.getLogs()
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var state by mutableStateOf(TerminalState.Idle)
        private set

    var toolsMenu by mutableStateOf(false)
        private set

    var isRoot by mutableStateOf(false)
        private set

    var command by mutableStateOf("")
        private set

    init {
        viewModelScope.launch(dispatchers.io) {
            if (settingsRepository.getSystemSettings().first().isFirstBoot) {
                showWelcomeMessage()
                settingsRepository.setFirstBootCompleted()
            }
        }
    }

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
        viewModelScope.launch(dispatchers.io) {
            terminalLogRepository.clearLogs()
        }
    }

    fun execute() {
        if (command.trim().isEmpty() || state != TerminalState.Idle)
            return
        state = TerminalState.Running
        viewModelScope.launch {
            val cmd = command.trim()
            command = ""
            try {
                commandExecutor.execute(cmd, isRoot)
            } catch (_: Exception) {
            } finally {
                state = TerminalState.Idle
            }
        }
    }

    fun terminate() =
        viewModelScope.launch {
            commandExecutor.cancel()
        }

    private suspend fun showWelcomeMessage() {
        val welcomeMessage = """
                        
            
             _____  _____                   _              _ 
            |  _  \|_   _|                 (_)            | |
            | |  | | | | ___ _ __ _ __ ___  _ _ __   __ _ | |
            | |  | | | |/ _ \ '__| '_ ` _ \| | '_ \ / _` || |
            | |__/ / | |  __/ |  | | | | | | | | | | (_| || |
            |_____/  \_/\___|_|  |_| |_| |_|_|_| |_|\__,_||_|
                                            
                                                  
                                                              
            😍 I'm finally installed! I was getting bored on the Github.com/dedeadend...
            
            ✨ Execute 'help' command to see DTerminal commands
            
            ☕ Coffee is not included!
            
            💚 Enjoy :)
            
            -------------------------------------------------
            
            """.trimIndent()

        terminalLogRepository.addLog(TerminalLog(TerminalState.Success, welcomeMessage))
    }
}

fun terminalLog2String(terminalLog: TerminalLog): String {
    return if (terminalLog.state == TerminalState.Info)
        "\n\n\n" + terminalLog.date + "\n" + terminalLog.message + "\n"
    else
        terminalLog.message

}