package dedeadend.dterminal.domain

import kotlinx.coroutines.flow.Flow

interface CommandExecutor {
    suspend fun execute(command: String, isRoot: Boolean): Flow<TerminalLog>
    suspend fun cancel(): TerminalLog
}