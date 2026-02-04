package dedeadend.dterminal.domin

sealed class TerminalState {
    object Success : TerminalState()
    object Error : TerminalState()
    object Running: TerminalState()
    object Idle: TerminalState()
}