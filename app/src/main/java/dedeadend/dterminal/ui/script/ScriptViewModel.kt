package dedeadend.dterminal.ui.script

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.data.Repository
import dedeadend.dterminal.domain.Script
import dedeadend.dterminal.domain.UiEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScriptViewModel @Inject constructor(
    private val repository: Repository,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val scripts = repository.getScripts()
        .flowOn(ioDispatcher)
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
        viewModelScope.launch(ioDispatcher) {
            scriptsBackup = listOf(scriptCommand)
            repository.deleteScriptWithId(scriptCommand.id)
            _eventFlow.send(UiEvent.ShowSnackbar("Script Deleted", "Undo"))
        }
    }

    fun undoDeleteScript() {
        viewModelScope.launch(ioDispatcher) {
            scriptsBackup?.let {
                repository.insertToScripts(it.last())
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
        viewModelScope.launch(ioDispatcher) {
            repository.insertToScripts(
                Script(
                    editingScriptName,
                    editingScriptCommand,
                    editingScriptId
                )
            )
            editingScriptNameError = ""
            editingScriptCommandError = ""
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