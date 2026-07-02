package dedeadend.dterminal.ui.script

import androidx.compose.runtime.Immutable
import dedeadend.dterminal.domain.model.Script

@Immutable
data class ScriptUiState(
    val scripts: List<Script> = emptyList(),
    val scriptsBackup: List<Script> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val editingScriptName: String = "",
    val editingScriptCommand: String = "",
    val editingScriptId: Int = 0,
    val editingScriptNameError: String = "",
    val editingScriptCommandError: String = "",
) {
    val showEmptyState get() = scripts.isEmpty() && !isLoading
    val hasEditingScriptNameError get() = editingScriptNameError.isNotBlank()
    val hasEditingScriptCommandError get() = editingScriptCommandError.isNotBlank()
}