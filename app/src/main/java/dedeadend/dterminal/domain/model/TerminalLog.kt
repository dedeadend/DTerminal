package dedeadend.dterminal.domain.model

data class TerminalLog(
    val state: TerminalState,
    val message: String,
    val date: String = "",
    val id: Int = 0
)