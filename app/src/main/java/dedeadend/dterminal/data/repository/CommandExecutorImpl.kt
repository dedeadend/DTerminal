package dedeadend.dterminal.data.repository

import dedeadend.dterminal.data.datasource.PythonManager
import dedeadend.dterminal.data.datasource.ShellManager
import dedeadend.dterminal.domain.model.History
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState
import dedeadend.dterminal.domain.repository.CommandExecutor
import dedeadend.dterminal.domain.repository.HistoryRepository
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import jakarta.inject.Inject
import kotlin.concurrent.Volatile

enum class ExecutorType { IDLE, SHELL, PYTHON }

class CommandExecutorImpl @Inject constructor(
    private val terminalLogRepository: TerminalLogRepository,
    private val historyRepository: HistoryRepository,
    private val shellManager: ShellManager,
    private val pythonManager: PythonManager
) : CommandExecutor {

    @Volatile
    private var currentExecutor = ExecutorType.IDLE

    override suspend fun execute(command: String, isRoot: Boolean) {
        historyRepository.addHistory(History(command))
        try {
            val lines = command.trim().lines()
            val firstLine = lines.firstOrNull()?.trim()?.lowercase() ?: ""
            if (firstLine == "py") {
                currentExecutor = ExecutorType.PYTHON
                val pythonCode = lines.drop(1).joinToString("\n").trim()
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Info,
                        "└─> py\n\n$pythonCode"
                    )
                )
                pythonManager.execute(pythonCode)
            } else {
                currentExecutor = ExecutorType.SHELL
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Info,
                        if (isRoot) "└─> #\n\n$command" else "└─> $\n\n$command"
                    )
                )
                shellManager.execute(command, isRoot)
            }
        } finally {
            currentExecutor = ExecutorType.IDLE
        }
    }

    override suspend fun cancel() {
        when (currentExecutor) {
            ExecutorType.SHELL -> shellManager.cancel()
            ExecutorType.PYTHON -> pythonManager.cancel()
            ExecutorType.IDLE -> terminalLogRepository.addLog(
                TerminalLog(
                    TerminalState.Error,
                    "There is no active process to terminate."
                )
            )
        }
    }
}
