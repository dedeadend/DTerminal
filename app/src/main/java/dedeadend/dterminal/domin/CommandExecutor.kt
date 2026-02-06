package dedeadend.dterminal.domin

import kotlinx.coroutines.flow.Flow

interface CommandExecutor {
    suspend fun execute(command: String, isroot: Boolean): Flow<TerminalMessage>
    suspend fun cancel(): TerminalMessage
}