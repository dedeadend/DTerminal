package dedeadend.dterminal.ui.terminal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import dedeadend.dterminal.domain.model.Settings
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState

@Immutable
data class TerminalUiState(
    val settings: Settings = Settings(),
    val logs: List<TerminalLog> = emptyList(),
    val executionState: TerminalState = TerminalState.Idle,
    val isToolsMenuOpen: Boolean = false,
    val isRoot: Boolean = false,
    val command: String = "",
    val terminalLogText: AnnotatedString = AnnotatedString("")
)