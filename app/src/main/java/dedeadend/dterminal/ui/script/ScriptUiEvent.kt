package dedeadend.dterminal.ui.script

import dedeadend.dterminal.domain.model.Script

sealed interface ScriptUiEvent {
    data class DeleteScript(val scriptCommand: Script) : ScriptUiEvent
    data class StartEdit(val script: Script) : ScriptUiEvent
    data class OnEditingScriptNameChange(val newName: String) : ScriptUiEvent
    data class OnEditingScriptCommandChange(val newCommand: String) : ScriptUiEvent
    object UndoDeleteScript : ScriptUiEvent
    object AddNewScript : ScriptUiEvent
    object SaveEdit : ScriptUiEvent
    object CancelEdit : ScriptUiEvent
}