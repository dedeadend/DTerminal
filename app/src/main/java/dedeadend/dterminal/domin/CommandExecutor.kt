package dedeadend.dterminal.domin

import kotlinx.coroutines.flow.Flow

interface CommandExecutor {
    fun execute(command: String, isroot: Boolean): Flow<TerminalMessage>
    fun cancel(): TerminalMessage
}