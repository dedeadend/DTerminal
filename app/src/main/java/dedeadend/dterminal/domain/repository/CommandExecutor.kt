package dedeadend.dterminal.domain.repository

interface CommandExecutor {
    suspend fun execute(command: String, isRoot: Boolean)
    suspend fun cancel()
}