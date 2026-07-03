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
        withContext(dispatchers.default) {
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
        val tokens = command.trim().split("\\s+".toRegex())
        if (tokens.isEmpty() || tokens[0].isEmpty()) return false

        when (val baseCommand = tokens[0].lowercase()) {
            "help" -> {
                val helpText = """
                [ DTerminal Help ]


                ===== SYSTEM =====
                
                
                • help
                  Show this command list.

                • about
                  Display app and developer info.

                • clear / cls
                  Clear all terminal logs.

                • sysinfo
                  Advanced hardware & OS details.

                • uptime
                  Show system boot duration.

                • datetime
                  Display current date & time.

                • sudo [cmd]
                  Simulate root privilege command.
                  
                  
                ===== CUSTOMIZE =====
                
                
                • font [size]
                  Set terminal font size (5-25).

                • font def
                  Reset font size to default (11).

                • color1 [r g b]
                  Set Normal text color using RGB.

                • color1 def
                  Reset Normal text color to default.

                • color2 [r g b]
                  Set Error text color using RGB.

                • color2 def
                  Reset Error text color to default.

                • color3 [r g b]
                  Set Info text color using RGB.

                • color3 def
                  Reset Info text color to default.
                  
                  
                ===== TEXT =====
                
                
                • random [a] [b]
                  Generate number between a and b.

                • uuid
                  Generate a random UUID v4 string.

                • length [text]
                  Count characters in a string.

                • case [up/low] [text]
                  Convert text to upper/lowercase.

                • wordcount [text]
                  Count words in the given text.

                • sort [word1] [word2] ...
                  Sort list of words alphabetically.

                • shuffle [word1] [word2] ...
                  Randomly shuffle list of words.

                • reverse [text]
                  Reverse character order of text.

                • palindrome [text]
                  Check if text is a palindrome.

                • regex [pattern] [text]
                  Find regex matches within text.


                ===== CRYPTO & ENCODE =====
                
                
                • base64 [enc/dec] [text]
                  Encode or decode Base64 strings.

                • hash [algo] [text]
                  Generate md5, sha1, sha256.

                • url [enc/dec] [text]
                  Encode or decode URL components.

                • rot13 [text]
                  Apply ROT13 cipher to text.

                • morse [enc/dec] [text]
                  Encode or decode Morse code.

                • binary [enc/dec] [text]
                  Convert text to/from binary.

                • hex [enc/dec] [text]
                  Convert text to/from hex string.

                • ascii [single char]
                  Get decimal ASCII code of a char.


                ===== DEV TOOLS =====
                
                
                • pass [length]
                  Generate secure random password.

                • json [validate/format] [txt]
                  Validate or pretty-print JSON.


                ===== IMPORTANT NOTES =====
                
                
                • Shell Support:
                  Standard commands (ls, cd, etc.) 
                  are fully supported.

                • Multi-line Execution:
                  Each run is an isolated process. 
                  You can combine related commands.
                  e.g. Type:
                       cd /sdcard
                       ls
                  Then execute both lines at once.
            """.trimIndent()
                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, helpText))
            }

            "about" -> {
                val aboutText = """
                    
                 _____  _____                   _              _ 
                |  _  \|_   _|                 (_)            | |
                | |  | | | | ___ _ __ _ __ ___  _ _ __   __ _ | |
                | |  | | | |/ _ \ '__| '_ ` _ \| | '_ \ / _` || |
                | |__/ / | |  __/ |  | | | | | | | | | | (_| || |
                |_____/  \_/\___|_|  |_| |_| |_|_|_| |_|\__,_||_|
                                                  
                                                                
                                                                
                ♠️ DTerminal v2.0 - Developed by DeDeadend
                
                🌐 GitHub: github.com/dedeadend
                
                ☕ Coffee is not included!
                
                💚 Enjoy :)
                
            """.trimIndent()
                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, aboutText))
            }

            "color", "color1", "color2", "color3" -> {
                val type = if (baseCommand == "color") 1 else baseCommand.last().digitToInt()
                handleColorCommand(tokens, type, settingsRepository, terminalLogRepository)
            }

            "font" -> {
                if (tokens.size == 2) {
                    if (tokens[1] == "def") {
                        settingsRepository.setLogFontSize(11)
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Success,
                                "Font size set to default (11)."
                            )
                        )
                    } else {
                        val fontSize = tokens[1].toIntOrNull()
                        if (fontSize != null && fontSize in 5..25) {
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
                                    "Invalid size (5-25)."
                                )
                            )
                        }
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: font [size] or [def]"
                        )
                    )
                }
            }

            "random" -> {
                if (tokens.size == 3) {
                    val num1 = tokens[1].toIntOrNull()
                    val num2 = tokens[2].toIntOrNull()
                    if (num1 != null && num2 != null) {
                        val min = minOf(num1, num2)
                        val max = maxOf(num1, num2)
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Success,
                                (min..max).random().toString()
                            )
                        )
                    } else {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Enter valid numbers."
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: random [a] [b]"
                        )
                    )
                }
            }

            "clear", "cls" -> {
                terminalLogRepository.clearLogs()
            }

            "sysinfo" -> {
                val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
                val allAbis = Build.SUPPORTED_ABIS.joinToString(", ")
                val cores = Runtime.getRuntime().availableProcessors()
                val maxVmMem = Runtime.getRuntime().maxMemory() / (1024 * 1024)
                val buildDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(java.util.Date(Build.TIME))

                val info = """
                [ HARDWARE ]
                • Model: ${Build.BRAND} ${Build.MODEL}
                • Maker: ${Build.MANUFACTURER}
                • Board: ${Build.BOARD}
                • Device: ${Build.DEVICE}
                • Product: ${Build.PRODUCT}
                • Hardware: ${Build.HARDWARE}
                • Bootloader: ${Build.BOOTLOADER}
                
                [ CPU & MEMORY ]
                • CPU Cores: $cores cores
                • Architecture: ${System.getProperty("os.arch")}
                • Primary ABI: $primaryAbi
                • Supported ABIs: $allAbis
                • JVM Max Memory: ${maxVmMem}MB
                
                [ OS & BUILD ]
                • Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
                • Security Patch: ${Build.VERSION.SECURITY_PATCH}
                • Build ID: ${Build.ID}
                • Build Display: ${Build.DISPLAY}
                • Build Type/Tags: ${Build.TYPE} / ${Build.TAGS}
                • Kernel Version: ${System.getProperty("os.version")}
                • Build Date: $buildDate
                • Fingerprint:
                  ${Build.FINGERPRINT}
                
                [ STATUS ]
                • Uptime: ${getSystemUptime()}
            """.trimIndent()
                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, info))
            }

            "uptime" -> {
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Success,
                        "Uptime: ${getSystemUptime()}"
                    )
                )
            }

            "datetime" -> {
                val current = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(java.util.Date())
                terminalLogRepository.addLog(TerminalLog(TerminalState.Success, current))
            }

            "length" -> {
                if (tokens.size >= 2) {
                    val text = tokens.drop(1).joinToString(" ")
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            "Length: ${text.length}"
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: length [text]"
                        )
                    )
                }
            }

            "case" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    val result =
                        if (action == "up" || action == "upper") text.uppercase() else text.lowercase()
                    terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: case [up/low] [text]"
                        )
                    )
                }
            }

            "url" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    try {
                        val result = if (action == "enc" || action == "encode") {
                            java.net.URLEncoder.encode(text, "UTF-8")
                        } else {
                            java.net.URLDecoder.decode(text, "UTF-8")
                        }
                        terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                    } catch (_: Exception) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "URL operation failed."
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: url [enc/dec] [text]"
                        )
                    )
                }
            }

            "base64" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    when (action) {
                        "enc", "encode" -> {
                            val encoded =
                                java.util.Base64.getEncoder().encodeToString(text.toByteArray())
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    encoded
                                )
                            )
                        }

                        "dec", "decode" -> {
                            try {
                                val decoded = String(java.util.Base64.getDecoder().decode(text))
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Success,
                                        decoded
                                    )
                                )
                            } catch (_: Exception) {
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Error,
                                        "Invalid Base64."
                                    )
                                )
                            }
                        }

                        else -> terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: base64 [enc/dec] [text]"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: base64 [enc/dec] [text]"
                        )
                    )
                }
            }

            "hash" -> {
                if (tokens.size >= 3) {
                    val algorithm = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    val targetAlgo = when (algorithm) {
                        "md5" -> "MD5"
                        "sha1" -> "SHA-1"
                        "sha256" -> "SHA-256"
                        else -> null
                    }
                    if (targetAlgo != null) {
                        try {
                            val digest = java.security.MessageDigest.getInstance(targetAlgo)
                            val hashBytes = digest.digest(text.toByteArray())
                            val hashString = hashBytes.joinToString("") { "%02x".format(it) }
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    hashString
                                )
                            )
                        } catch (_: Exception) {
                            terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Error,
                                    "Hash failed."
                                )
                            )
                        }
                    } else {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Use: md5, sha1, sha256"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: hash [algo] [text]"
                        )
                    )
                }
            }

            "uuid" -> {
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Success,
                        java.util.UUID.randomUUID().toString()
                    )
                )
            }

            "sudo" -> {
                terminalLogRepository.addLog(
                    TerminalLog(
                        TerminalState.Success,
                        "Nice try! Use 'su' instead."
                    )
                )
            }

            "rot13" -> {
                if (tokens.size >= 2) {
                    val text = tokens.drop(1).joinToString(" ")
                    val result = text.map { c ->
                        when {
                            c.isUpperCase() -> 'A' + (c - 'A' + 13) % 26
                            c.isLowerCase() -> 'a' + (c - 'a' + 13) % 26
                            else -> c
                        }
                    }.joinToString("")
                    terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: rot13 [text]"
                        )
                    )
                }
            }

            "reverse" -> {
                if (tokens.size >= 2) {
                    val text = tokens.drop(1).joinToString(" ")
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            text.reversed()
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: reverse [text]"
                        )
                    )
                }
            }

            "palindrome" -> {
                if (tokens.size >= 2) {
                    val text = tokens.drop(1).joinToString("")
                    val normalized = text.lowercase().filter { it.isLetterOrDigit() }
                    val isPalindrome = normalized == normalized.reversed()
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            if (isPalindrome) "Yes, it's a palindrome." else "No, it's not a palindrome."
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: palindrome [text]"
                        )
                    )
                }
            }

            "morse" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    when (action) {
                        "enc", "encode" -> {
                            val result = text.uppercase().map { c -> morseMap[c] ?: c.toString() }
                                .joinToString(" ")
                            terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                        }

                        "dec", "decode" -> {
                            val result = text.trim().split(" ").joinToString("") { code ->
                                morseMap.entries.find { it.value == code }?.key?.toString() ?: code
                            }
                            terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                        }

                        else -> terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: morse [enc/dec] [text]"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: morse [enc/dec] [text]"
                        )
                    )
                }
            }

            "binary" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    when (action) {
                        "enc", "encode" -> {
                            val result = text.toByteArray().joinToString(" ") {
                                Integer.toBinaryString(it.toInt() and 0xFF).padStart(8, '0')
                            }
                            terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                        }

                        "dec", "decode" -> {
                            try {
                                val bytes = text.trim().split(" ").map { it.toInt(2).toByte() }
                                    .toByteArray()
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Success,
                                        String(bytes)
                                    )
                                )
                            } catch (_: Exception) {
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Error,
                                        "Invalid binary."
                                    )
                                )
                            }
                        }

                        else -> terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: binary [enc/dec] [text]"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: binary [enc/dec] [text]"
                        )
                    )
                }
            }

            "hex" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    when (action) {
                        "enc", "encode" -> {
                            val result = text.toByteArray().joinToString("") { "%02x".format(it) }
                            terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                        }

                        "dec", "decode" -> {
                            try {
                                val clean = text.trim().replace(" ", "")
                                val bytes =
                                    clean.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Success,
                                        String(bytes)
                                    )
                                )
                            } catch (_: Exception) {
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Error,
                                        "Invalid hex."
                                    )
                                )
                            }
                        }

                        else -> terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Usage: hex [enc/dec] [text]"
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: hex [enc/dec] [text]"
                        )
                    )
                }
            }

            "ascii" -> {
                if (tokens.size == 2 && tokens[1].length == 1) {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            tokens[1][0].code.toString()
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: ascii [single char]"
                        )
                    )
                }
            }

            "wordcount" -> {
                if (tokens.size >= 2) {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            "Words: ${tokens.drop(1).size}"
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: wordcount [text]"
                        )
                    )
                }
            }

            "sort" -> {
                if (tokens.size >= 2) {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            tokens.drop(1).sorted().joinToString(" ")
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: sort [word1] [word2] ..."
                        )
                    )
                }
            }

            "shuffle" -> {
                if (tokens.size >= 2) {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            tokens.drop(1).shuffled().joinToString(" ")
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: shuffle [word1] [word2] ..."
                        )
                    )
                }
            }

            "pass" -> {
                if (tokens.size == 2) {
                    val length = tokens[1].toIntOrNull()
                    if (length != null && length in 4..128) {
                        val chars =
                            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
                        val result = (1..length).map { chars.random() }.joinToString("")
                        terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                    } else {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Length must be 4-128."
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: pass [length]"
                        )
                    )
                }
            }

            "json" -> {
                if (tokens.size >= 3) {
                    val action = tokens[1].lowercase()
                    val text = tokens.drop(2).joinToString(" ")
                    try {
                        val parsed: Any = try {
                            org.json.JSONObject(text)
                        } catch (_: Exception) {
                            org.json.JSONArray(text)
                        }
                        when (action) {
                            "validate" -> terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Success,
                                    "Valid JSON."
                                )
                            )

                            "format" -> {
                                val formatted = when (parsed) {
                                    is org.json.JSONObject -> parsed.toString(2)
                                    is org.json.JSONArray -> parsed.toString(2)
                                    else -> text
                                }
                                terminalLogRepository.addLog(
                                    TerminalLog(
                                        TerminalState.Success,
                                        formatted
                                    )
                                )
                            }

                            else -> terminalLogRepository.addLog(
                                TerminalLog(
                                    TerminalState.Error,
                                    "Usage: json [validate/format] [text]"
                                )
                            )
                        }
                    } catch (_: Exception) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Invalid JSON."
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: json [validate/format] [text]"
                        )
                    )
                }
            }

            "regex" -> {
                if (tokens.size >= 3) {
                    val pattern = tokens[1]
                    val text = tokens.drop(2).joinToString(" ")
                    try {
                        val matches = Regex(pattern).findAll(text).map { it.value }.toList()
                        val result = if (matches.isEmpty()) "No match." else "Matches: ${
                            matches.joinToString(", ")
                        }"
                        terminalLogRepository.addLog(TerminalLog(TerminalState.Success, result))
                    } catch (_: Exception) {
                        terminalLogRepository.addLog(
                            TerminalLog(
                                TerminalState.Error,
                                "Invalid regex pattern."
                            )
                        )
                    }
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Usage: regex [pattern] [text]"
                        )
                    )
                }
            }

            else -> return false
        }
        return true
    }

    private fun getSystemUptime(): String {
        val upTime = SystemClock.elapsedRealtime()
        val hours = (upTime / 3600000) % 24
        val minutes = (upTime / 60000) % 60
        val seconds = (upTime / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private suspend fun handleColorCommand(
        tokens: List<String>,
        colorType: Int,
        settingsRepository: SettingsRepository,
        terminalLogRepository: TerminalLogRepository
    ) {
        val usage = "Usage: color$colorType [r g b] or [def]"
        when (tokens.size) {
            2 -> {
                if (tokens[1] == "def") {
                    updateRepositoryColor(settingsRepository, colorType, -1, -1, -1)
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            "Color $colorType reset to default."
                        )
                    )
                } else {
                    terminalLogRepository.addLog(TerminalLog(TerminalState.Error, usage))
                }
            }

            4 -> {
                val r = tokens[1].toIntOrNull()
                val g = tokens[2].toIntOrNull()
                val b = tokens[3].toIntOrNull()
                if (r != null && g != null && b != null && r in 0..255 && g in 0..255 && b in 0..255) {
                    updateRepositoryColor(settingsRepository, colorType, r, g, b)
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Success,
                            "Color $colorType applied."
                        )
                    )
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Values must be 0-255."
                        )
                    )
                }
            }

            else -> terminalLogRepository.addLog(TerminalLog(TerminalState.Error, usage))
        }
    }

    private suspend fun updateRepositoryColor(
        repo: SettingsRepository,
        type: Int,
        r: Int,
        g: Int,
        b: Int
    ) {
        when (type) {
            1 -> repo.setLogSuccessFontColor(r, g, b)
            2 -> repo.setLogErrorFontColor(r, g, b)
            3 -> repo.setLogInfoFontColor(r, g, b)
        }
    }

    private val morseMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".", 'F' to "..-.",
        'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.", 'Q' to "--.-", 'R' to ".-.",
        'S' to "...", 'T' to "-", 'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        ' ' to "/"
    )
}