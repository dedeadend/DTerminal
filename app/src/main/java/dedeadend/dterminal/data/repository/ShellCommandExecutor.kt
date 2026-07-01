package dedeadend.dterminal.data.repository

import android.os.Build
import android.os.SystemClock
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.model.History
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState
import dedeadend.dterminal.domain.repository.CommandExecutor
import dedeadend.dterminal.domain.repository.HistoryRepository
import dedeadend.dterminal.domain.repository.SettingsRepository
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

class ShellCommandExecutor @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val terminalLogRepository: TerminalLogRepository,
    private val dispatchers: AppDispatchers
) : CommandExecutor {
    private var process: Process? = null
    override suspend fun execute(command: String, isRoot: Boolean) {
        withContext(dispatchers.io) {
            historyRepository.addHistory(History(command))
            terminalLogRepository.addLog(
                TerminalLog(
                    TerminalState.Info,
                    (if (isRoot) "#: " else "$: ") + command
                )
            )
            try {
                process = ProcessBuilder(if (isRoot) "su" else "sh")
                    .redirectErrorStream(true)
                    .start()
                launch {
                    process?.outputStream?.bufferedWriter()?.use { writer ->
                        command.lines().forEach { cmd ->
                            if (cmd.trim().isNotBlank()) {
                                if (!executedAsCustomCommand(
                                        settingsRepository,
                                        terminalLogRepository,
                                        cmd
                                    )
                                ) {
                                    writer.write(cmd + "\n")
                                    writer.flush()
                                }
                            }
                        }
                        writer.write("exit\n")
                        writer.flush()
                    }
                }
                process?.inputStream?.bufferedReader()?.use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        terminalLogRepository.addLog(TerminalLog(TerminalState.Success, line!!))
                    }
                }
                process?.waitFor()
            } catch (e: Exception) {
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Error,
                        e.message ?: "Unknown Error"
                    )
                )
            } finally {
                process?.let { process ->
                    process.inputStream?.close()
                    process.outputStream?.close()
                    process.errorStream?.close()
                    process.destroyForcibly()
                }
                process = null
            }
        }
    }

    override suspend fun cancel() {
        withContext(dispatchers.io) {
            if (process == null) {
                terminalLogRepository.addLog(
                    TerminalLog(TerminalState.Error, "There is no active process")
                )
            }
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
                terminalLogRepository.addLog(
                    TerminalLog(TerminalState.Error, "Process terminated by user")
                )
            }
            process = null
        }
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

    private suspend fun executedAsCustomCommand(
        settingsRepository: SettingsRepository,
        terminalLogRepository: TerminalLogRepository,
        command: String
    ): Boolean {
        when (command.split(" ")[0].lowercase().trim()) {

            "help" -> {
                val helpText = """
                    [ DTerminal Help Menu ]
                    -----------------------
                    
                    System Commands
                    ---------------
                    help             [Show this list of commands]
                    about            [Display app information]
                    clear/cls        [Clear all terminal logs]
                    sysinfo          [Display device and OS details]
                    sudo [cmd]       [Run a command with root privileges]
                    random [a] [b]   [Generate a random number between a and b]
                
                    UI Customization
                    ----------------
                    font [size]      [Set terminal font size]
                    color1 [r g b]   [Set Normal text color (RGB)]
                    color2 [r g b]   [Set Error text color (RGB)]
                    color3 [r g b]   [Set Info text color (RGB)]
                    
                    Note: You can use [def] to set default value.
                    e.g. color1 def
                    
                    
                    -----------------------
                    Hint: Standard shell commands (ls, cd, ping, etc.) are supported.
                    
                    Note: Each execution runs in an isolated process. So you must combine related commands in a single execution.
                    e.g. Type:
                           cd /sdcard
                           ls
                         Then execute both lines at once.
                """.trimIndent()
                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, helpText))
                return true
            }

            "about" -> {
                val aboutText = """
                        
                     _____  _____                   _              _ 
                    |  _  \|_   _|                 (_)            | |
                    | |  | | | | ___ _ __ _ __ ___  _ _ __   __ _ | |
                    | |  | | | |/ _ \ '__| '_ ` _ \| | '_ \ / _` || |
                    | |__/ / | |  __/ |  | | | | | | | | | | (_| || |
                    |_____/  \_/\___|_|  |_| |_| |_|_|_| |_|\__,_||_|
                                                    
                                                          
                                                                      
                    ♠️ DTerminal v1.2 - Created by Ehsan Nasiri. 
                    
                    🌐 Open Source on GitHub: github.com/dedeadend
                    
                    ☕ Coffee is not included!
                    
                    💚 Enjoy :)
                    
                    
                    -------------------------------------------------
                """.trimIndent()
                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, aboutText))
                return true
            }

            "color", "color1" -> {
                val parts = command.trim().split("\\s+".toRegex())
                if (parts.size == 2) {
                    if (parts[1] == "def") {
                        settingsRepository.setLogSuccessFontColor(-1, -1, -1)
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Success,
                                "Color set successfully."
                            )
                        )
                    } else {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: color1 [red] [green] [blue]\n" +
                                        "Example: color1 250 140 0\n" +
                                        "Default: color1 def"
                            )
                        )
                    }
                } else if (parts.size == 4) {
                    try {
                        val red = parts[1].toInt()
                        val green = parts[2].toInt()
                        val blue = parts[3].toInt()
                        if (red in 0..255 && green in 0..255 && blue in 0..255) {
                            settingsRepository.setLogSuccessFontColor(red, green, blue)
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    "Color set successfully."
                                )
                            )
                        } else {
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Error,
                                    "Invalid color values.\n" +
                                            "Note: Values must be between 0 and 255."
                                )
                            )
                        }
                    } catch (_: NumberFormatException) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: color1 [red] [green] [blue]\n" +
                                        "Example: color1 250 140 0\n" +
                                        "Default: color1 def"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: color1 [red] [green] [blue]\n" +
                                    "Example: color1 250 140 0\n" +
                                    "Default: color1 def"
                        )
                    )
                }
                return true
            }

            "color2" -> {
                val parts = command.trim().split("\\s+".toRegex())
                if (parts.size == 2) {
                    if (parts[1] == "def") {
                        settingsRepository.setLogErrorFontColor(-1, -1, -1)
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Success,
                                "Color set successfully."
                            )
                        )
                    } else {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: color2 [red] [green] [blue]\n" +
                                        "Example: color2 250 140 0\n" +
                                        "Default: color2 def"
                            )
                        )
                    }
                } else if (parts.size == 4) {
                    try {
                        val red = parts[1].toInt()
                        val green = parts[2].toInt()
                        val blue = parts[3].toInt()
                        if (red in 0..255 && green in 0..255 && blue in 0..255) {
                            settingsRepository.setLogErrorFontColor(red, green, blue)
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    "Color set successfully."
                                )
                            )
                        } else {
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Error,
                                    "Invalid color values.\n" +
                                            "Note: Values must be between 0 and 255."
                                )
                            )
                        }
                    } catch (_: NumberFormatException) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: color2 [red] [green] [blue]\n" +
                                        "Example: color2 250 140 0\n" +
                                        "Default: color2 def"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: color2 [red] [green] [blue]\n" +
                                    "Example: color2 250 140 0\n" +
                                    "Default: color2 def"
                        )
                    )
                }
                return true
            }

            "color3" -> {
                val parts = command.trim().split("\\s+".toRegex())
                if (parts.size == 2) {
                    if (parts[1] == "def") {
                        settingsRepository.setLogInfoFontColor(-1, -1, -1)
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Success,
                                "Color set successfully."
                            )
                        )
                    } else {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: color3 [red] [green] [blue]\n" +
                                        "Example: color3 250 140 0\n" +
                                        "Default: color3 def"
                            )
                        )
                    }
                } else if (parts.size == 4) {
                    try {
                        val red = parts[1].toInt()
                        val green = parts[2].toInt()
                        val blue = parts[3].toInt()
                        if (red in 0..255 && green in 0..255 && blue in 0..255) {
                            settingsRepository.setLogInfoFontColor(red, green, blue)
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    "Color set successfully."
                                )
                            )
                        } else {
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Error,
                                    "Invalid color values.\n" +
                                            "Note: Values must be between 0 and 255."
                                )
                            )
                        }
                    } catch (_: NumberFormatException) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: color3 [red] [green] [blue]\n" +
                                        "Example: color3 250 140 0\n" +
                                        "Default: color3 def"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: color3 [red] [green] [blue]\n" +
                                    "Example: color3 250 140 0\n" +
                                    "Default: color3 def"
                        )
                    )
                }
                return true
            }

            "font" -> {
                val parts = command.trim().split("\\s+".toRegex())
                if (parts.size == 2) {
                    if (parts[1] == "def") {
                        settingsRepository.setLogFontSize(11)
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Success,
                                "Font size set successfully."
                            )
                        )
                    } else {
                        try {
                            val fontSize = parts[1].toInt()
                            if (fontSize in 5..25) {
                                settingsRepository.setLogFontSize(fontSize)
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Success,
                                        "Font size set successfully."
                                    )
                                )
                            } else {
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Error,
                                        "Invalid font size value.\n" +
                                                "Note: Value must be between 5 and 25."
                                    )
                                )
                            }
                        } catch (_: NumberFormatException) {
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Error,
                                    "Invalid font value.\n" +
                                            "Note: Value must be between 5 and 25."
                                )
                            )
                        }
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: font [size]\n" +
                                    "Example: font 13\n" +
                                    "Default: font def"

                        )
                    )
                }
                return true
            }

            "random" -> {
                val parts = command.trim().split("\\s+".toRegex())
                if (parts.size == 3) {
                    try {
                        val num1 = parts[1].toInt()
                        val num2 = parts[2].toInt()
                        if (num1 > num2) {
                            val randomNum = (num2..num1).random()
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    ("$randomNum")
                                )
                            )

                        } else {
                            val randomNum = (num1..num2).random()
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    ("$randomNum")
                                )
                            )
                        }
                    } catch (_: NumberFormatException) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Invalid input. Please enter valid numbers."
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: random [a] [b]\n" +
                                    "Example: random 1 100"
                        )
                    )
                }
                return true
            }

            "clear", "cls" -> {
                terminalLogRepository.clearLogs()
                return true
            }

            "sysinfo" -> {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                val androidVersion = Build.VERSION.RELEASE
                val sdkVersion = Build.VERSION.SDK_INT
                val board = Build.BOARD
                val hardware = Build.HARDWARE
                val upTime = SystemClock.elapsedRealtime()
                val hours = (upTime / (1000 * 60 * 60)) % 24
                val minutes = (upTime / (1000 * 60)) % 60
                val seconds = (upTime / 1000) % 60
                val uptimeString = String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d", hours, minutes, seconds
                )
                val info = """
                    [ DEVICE INFORMATION ]
                    ----------------------
                    Manufacturer : ${manufacturer.uppercase()}
                    Model        : $model
                    Board        : $board
                    Hardware     : $hardware
                    
                    
                    [ SOFTWARE SYSTEM ]
                    -------------------
                    Android Ver  : $androidVersion (API $sdkVersion)
                    Kernel       : ${System.getProperty("os.version")}
                    
                    
                    [ SYSTEM ]
                    ----------
                    Uptime: $uptimeString
                """.trimIndent()

                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, info))
                return true
            }

            "sudo" -> {
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Success,
                        "Nice try! But you have no power here :)\n[Use su or switch to root mode]"
                    )
                )
                return true
            }

            else -> {
                return false
            }
        }
    }
}