package dedeadend.dterminal.ui.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState
import dedeadend.dterminal.domain.repository.CommandExecutor
import dedeadend.dterminal.domain.repository.SettingsRepository
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val commandExecutor: CommandExecutor,
    private val dispatchers: AppDispatchers,
    private val settingsRepository: SettingsRepository,
    private val terminalLogRepository: TerminalLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TerminalUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatchers.io) {
            if (settingsRepository.getSystemSettings().first().isFirstBoot) {
                showWelcomeMessage()
                settingsRepository.setFirstBootCompleted()
            }
        }
        viewModelScope.launch {
            settingsRepository.getSystemSettings()
                .flowOn(dispatchers.io)
                .collect { latestSettings ->
                    _uiState.update { it.copy(settings = latestSettings) }
                }
        }
        viewModelScope.launch {
            terminalLogRepository.getLogs()
                .flowOn(dispatchers.io)
                .collect { latestLogs ->
                    _uiState.update { it.copy(logs = latestLogs) }
                }
        }
    }

    fun onEvent(event: TerminalUiEvent) {
        when (event) {
            is TerminalUiEvent.Execute -> execute()
            is TerminalUiEvent.Terminate -> terminate()
            is TerminalUiEvent.ToggleRoot -> _uiState.update { it.copy(isRoot = !it.isRoot) }
            is TerminalUiEvent.ClearOutput -> clearOutput()
            is TerminalUiEvent.OnCommandChange -> _uiState.update { it.copy(command = event.newCommand) }
            is TerminalUiEvent.ToggleToolsMenu -> _uiState.update { it.copy(isToolsMenuOpen = !it.isToolsMenuOpen) }
        }
    }

    private fun clearOutput() {
        viewModelScope.launch(dispatchers.io) {
            terminalLogRepository.clearLogs()
        }
    }

    private fun execute() {
        if (_uiState.value.command.isBlank() || _uiState.value.executionState != TerminalState.Idle)
            return
        _uiState.update { it.copy(executionState = TerminalState.Running) }
        viewModelScope.launch {
            val cmd = _uiState.value.command.trim()
            _uiState.update { it.copy(command = "") }
            try {
                commandExecutor.execute(cmd, _uiState.value.isRoot)
            } finally {
                _uiState.update { it.copy(executionState = TerminalState.Idle) }
            }
        }
    }

    private fun terminate() =
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

