package dedeadend.dterminal.ui.script

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.UiEvent
import dedeadend.dterminal.domain.model.Script
import dedeadend.dterminal.domain.repository.ScriptRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val scriptRepository: ScriptRepository,
    private val dispatchers: AppDispatchers
) : ViewModel() {

    val scripts = scriptRepository.getScripts()
        .flowOn(dispatchers.io)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var scriptsBackup: List<Script>? = null

    var isEditing by mutableStateOf(false)
        private set
    var editingScriptName by mutableStateOf("")
        private set
    var editingScriptCommand by mutableStateOf("")
        private set
    private var editingScriptId = -1

    var editingScriptNameError by mutableStateOf("")
        private set

    var editingScriptCommandError by mutableStateOf("")
        private set

    private var _eventFlow = Channel<UiEvent>(Channel.RENDEZVOUS)
    val eventFlow = _eventFlow.receiveAsFlow()

    fun deleteScript(scriptCommand: Script) {
        viewModelScope.launch(dispatchers.io) {
            scriptsBackup = listOf(scriptCommand)
            scriptRepository.deleteScriptWithId(scriptCommand.id)
            _eventFlow.send(UiEvent.ShowSnackbar("Script Deleted", "Undo"))
        }
    }

    fun undoDeleteScript() {
        viewModelScope.launch(dispatchers.io) {
            scriptsBackup?.let {
                scriptRepository.addScript(it.last())
                scriptsBackup = null
            }
        }
    }

    fun addNewScript() {
        startEdit(Script("", ""))
    }

    fun startEdit(script: Script) {
        editingScriptName = script.name
        editingScriptCommand = script.command
        editingScriptId = script.id
        isEditing = true
    }

    fun saveEdit() {
        if (editingScriptName.trim().isEmpty()) {
            editingScriptNameError = "Name cannot be empty"
            return
        }
        if (editingScriptCommand.trim().isEmpty()) {
            editingScriptCommandError = "Command cannot be empty"
            return
        }
        viewModelScope.launch(dispatchers.io) {
            scriptRepository.addScript(
                Script(
                    editingScriptName,
                    editingScriptCommand,
                    editingScriptId
                )
            )
            editingScriptNameError = ""
            editingScriptCommandError = ""
            editingScriptId = -1
            isEditing = false
        }
    }

    fun cancelEdit() {
        editingScriptNameError = ""
        editingScriptCommandError = ""
        editingScriptName = ""
        editingScriptCommand = ""
        editingScriptId = -1
        isEditing = false
    }

    fun onEditingScriptNameChange(newName: String) {
        editingScriptName = newName
        editingScriptNameError = ""
    }

    fun onEditingScriptCommandChange(newCommand: String) {
        editingScriptCommand = newCommand
        editingScriptCommandError = ""
    }
}