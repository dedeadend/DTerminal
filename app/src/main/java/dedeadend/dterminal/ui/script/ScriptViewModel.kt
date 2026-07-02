package dedeadend.dterminal.ui.script

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.model.Script
import dedeadend.dterminal.domain.repository.ScriptRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val scriptRepository: ScriptRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {
    private val _uiState = MutableStateFlow(ScriptUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<ScriptUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            scriptRepository.getScripts().flowOn(dispatchers.io).collect { scripts ->
                _uiState.update { it.copy(scripts = scripts, isLoading = false) }
            }
        }
    }

    fun onEvent(event: ScriptUiEvent) {
        when (event) {
            is ScriptUiEvent.DeleteScript -> deleteScript(event.scriptCommand)
            is ScriptUiEvent.AddNewScript -> addNewScript()
            is ScriptUiEvent.UndoDeleteScript -> undoDeleteScript()
            is ScriptUiEvent.StartEdit -> startEdit(event.script)
            is ScriptUiEvent.CancelEdit -> cancelEdit()
            is ScriptUiEvent.OnEditingScriptCommandChange -> onEditingScriptCommandChange(event.newCommand)
            is ScriptUiEvent.OnEditingScriptNameChange -> onEditingScriptNameChange(event.newName)
            is ScriptUiEvent.SaveEdit -> saveEdit()
        }
    }

    private fun deleteScript(scriptCommand: Script) {
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(scriptsBackup = listOf(scriptCommand)) }
            scriptRepository.deleteScriptWithId(scriptCommand.id)
            _uiEffect.send(ScriptUiEffect.ShowSnackbar("Script Deleted", "Undo"))
        }
    }

    private fun undoDeleteScript() {
        viewModelScope.launch(dispatchers.io) {
            if (_uiState.value.scriptsBackup.isNotEmpty()) {
                scriptRepository.addScript(_uiState.value.scriptsBackup.last())
                _uiState.update { it.copy(scriptsBackup = emptyList()) }
            }
        }
    }

    private fun addNewScript() {
        startEdit(Script("", ""))
    }

    private fun startEdit(script: Script) {
        _uiState.update {
            it.copy(
                editingScriptName = script.name,
                editingScriptCommand = script.command,
                editingScriptId = script.id,
                isEditing = true
            )
        }
    }

    private fun saveEdit() {
        var isValid = true
        if (_uiState.value.editingScriptName.isBlank()) {
            _uiState.update { it.copy(editingScriptNameError = "Name cannot be empty") }
            isValid = false
        }
        if (_uiState.value.editingScriptCommand.isBlank()) {
            _uiState.update { it.copy(editingScriptCommandError = "Command cannot be empty") }
            isValid = false
        }
        if (!isValid)
            return
        viewModelScope.launch(dispatchers.io) {
            scriptRepository.addScript(
                Script(
                    _uiState.value.editingScriptName,
                    _uiState.value.editingScriptCommand,
                    _uiState.value.editingScriptId
                )
            )
            _uiState.update {
                it.copy(
                    editingScriptNameError = "",
                    editingScriptCommandError = "",
                    editingScriptId = 0,
                    isEditing = false
                )
            }
            _uiEffect.send(ScriptUiEffect.ShowSnackbar("Script Saved Successfully"))
        }
    }

    private fun cancelEdit() {
        _uiState.update {
            it.copy(
                editingScriptNameError = "",
                editingScriptCommandError = "",
                editingScriptName = "",
                editingScriptCommand = "",
                editingScriptId = 0,
                isEditing = false
            )
        }

    }

    private fun onEditingScriptNameChange(newName: String) {
        _uiState.update {
            it.copy(
                editingScriptName = newName,
                editingScriptNameError = ""
            )
        }
    }

    private fun onEditingScriptCommandChange(newCommand: String) {
        _uiState.update {
            it.copy(
                editingScriptCommand = newCommand,
                editingScriptCommandError = ""
            )
        }
    }
}