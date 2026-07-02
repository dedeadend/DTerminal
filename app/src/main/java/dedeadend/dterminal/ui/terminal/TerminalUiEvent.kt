package dedeadend.dterminal.ui.terminal

sealed interface TerminalUiEvent {
    data class ToggleToolsMenu(val show: Boolean) : TerminalUiEvent
    data class OnCommandChange(val newCommand: String) : TerminalUiEvent
    object ToggleRoot : TerminalUiEvent
    object ClearOutput : TerminalUiEvent
    object Execute : TerminalUiEvent
    object Terminate : TerminalUiEvent


}