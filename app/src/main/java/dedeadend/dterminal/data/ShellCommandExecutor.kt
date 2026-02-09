package dedeadend.dterminal.data

import dedeadend.dterminal.domin.CommandExecutor
import dedeadend.dterminal.domin.TerminalLog
import dedeadend.dterminal.domin.TerminalState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.BufferedReader

class ShellCommandExecutor : CommandExecutor {
    private var process: Process? = null
    private var reader: BufferedReader? = null

    override suspend fun execute(command: String, isRoot: Boolean): Flow<TerminalLog> =
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
                trySend(TerminalLog(TerminalState.Success, line!!))
            }
            process?.waitFor()
            reader?.close()
            process?.destroy()
            reader = null
            process = null
            close()
        }

    override suspend fun cancel(): TerminalLog {
        process?.let {
            reader?.close()
            it.destroy()
            reader = null
            process = null
            return TerminalLog(TerminalState.Error, "Process Terminated by User")
        }
        return TerminalLog(TerminalState.Error, "There is No Active Process")
    }
}