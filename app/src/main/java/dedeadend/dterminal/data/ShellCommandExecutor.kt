package dedeadend.dterminal.data

import dedeadend.dterminal.domin.CommandExecutor
import dedeadend.dterminal.domin.TerminalMessage
import dedeadend.dterminal.domin.TerminalState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader

class ShellCommandExecutor : CommandExecutor {
    private var process: Process? = null
    private var reader: BufferedReader? = null

    override suspend fun execute(command: String, isRoot: Boolean): Flow<TerminalMessage> =
        callbackFlow {
            val finalCommand = command.lines().joinToString(" && ")
            if (isRoot)
                process = ProcessBuilder("su", "-c", finalCommand)
                    .redirectErrorStream(true)
                    .start()
            else
                process = ProcessBuilder("/system/bin/sh", "-c", finalCommand)
                    .redirectErrorStream(true)
                    .start()
            reader = process?.inputStream?.bufferedReader()
            var line: String?
            while (reader?.readLine().also { line = it } != null) {
                trySend(TerminalMessage(TerminalState.Success, line!!))
            }
            process?.waitFor()
            reader?.close()
            process?.destroy()
            reader = null
            process = null
            close()
        }

    override suspend fun cancel(): TerminalMessage {
        process?.let {
            reader?.close()
            it.destroy()
            reader = null
            process = null
            return TerminalMessage(TerminalState.Error, "Process Terminated by User")
        }
        return TerminalMessage(TerminalState.Error, "There is No Active Process")
    }
}