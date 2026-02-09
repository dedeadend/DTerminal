package dedeadend.dterminal.domin

import kotlinx.coroutines.flow.Flow

interface CommandExecutor {
    suspend fun execute(command: String, isRoot: Boolean): Flow<TerminalLog>
    suspend fun cancel(): TerminalLog
}