package dedeadend.dterminal.data

import dedeadend.dterminal.domain.CommandExecutor
import dedeadend.dterminal.domain.TerminalLog
import dedeadend.dterminal.domain.TerminalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ShellCommandExecutor : CommandExecutor {
    private var process: Process? = null
    override suspend fun execute(command: String, isRoot: Boolean): Flow<TerminalLog> =
        callbackFlow {
            process = ProcessBuilder(if (isRoot) "su" else "sh")
                .redirectErrorStream(true)
                .start()
            launch(Dispatchers.IO) {
                process?.outputStream?.bufferedWriter()?.use { writer ->
                    command.lines().forEach { cmd ->
                        if (cmd.trim().isNotBlank()) {
                            writer.write(cmd + "\n")
                            writer.flush()
                        }
                    }
                    writer.write("exit\n")
                    writer.flush()
                }
            }
            process?.inputStream?.bufferedReader()?.use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    send(TerminalLog(TerminalState.Success, line!!))
                }
            }
            process?.waitFor()
            channel.close()
            awaitClose {
                process?.let { process ->
                    process.inputStream?.close()
                    process.outputStream?.close()
                    process.errorStream?.close()
                    process.destroyForcibly()
                }
                process = null
            }
        }.flowOn(Dispatchers.IO)

    override suspend fun cancel(): TerminalLog {
        process?.let { process ->
            val pid = getPid()
            if (pid != -1) {
                Runtime.getRuntime().exec("pkill -P $pid")
            }
            process.destroy()
            if (!process.waitFor(1000, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                process.waitFor()
            }
            process.inputStream.close()
            process.outputStream.close()
            process.errorStream.close()
            return TerminalLog(TerminalState.Error, "Process terminated by user")
        }
        return TerminalLog(TerminalState.Error, "There is no active process")
    }

    private fun getPid(): Int {
        return try {
            val field = process!!.javaClass.getDeclaredField("pid")
            field.isAccessible = true
            field.getInt(process)
        } catch (_: Exception) {
            -1
        }
    }
}