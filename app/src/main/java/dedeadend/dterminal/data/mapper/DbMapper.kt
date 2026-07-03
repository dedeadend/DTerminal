package dedeadend.dterminal.data.mapper

import dedeadend.dterminal.data.local.HistoryEntity
import dedeadend.dterminal.data.local.ScriptEntity
import dedeadend.dterminal.data.local.SettingsEntity
import dedeadend.dterminal.data.local.TerminalLogEntity
import dedeadend.dterminal.domain.model.History
import dedeadend.dterminal.domain.model.Script
import dedeadend.dterminal.domain.model.Settings
import dedeadend.dterminal.domain.model.TerminalLog
import java.text.SimpleDateFormat

fun SettingsEntity.toDomain(): Settings {
    return Settings(
        isFirstBoot = this.isFirstBoot,
        logSuccessFontColor = this.logSuccessFontColor,
        logErrorFontColor = this.logErrorFontColor,
        logInfoFontColor = this.logInfoFontColor,
        logFontSize = this.logFontSize,
        id = this.id
    )
}

fun Settings.toEntity(): SettingsEntity {
    return SettingsEntity(
        isFirstBoot = this.isFirstBoot,
        logSuccessFontColor = this.logSuccessFontColor,
        logErrorFontColor = this.logErrorFontColor,
        logInfoFontColor = this.logInfoFontColor,
        logFontSize = this.logFontSize,
        id = this.id
    )
}


fun TerminalLogEntity.toDomain(): TerminalLog {
    return TerminalLog(
        state = this.state,
        message = this.message,
        date = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            java.util.Locale.getDefault()
        ).format(this.date),
        id = this.id
    )
}

fun TerminalLog.toEntity(): TerminalLogEntity {
    val time = if (this.date.isBlank()) {
        System.currentTimeMillis()
    } else {
        try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .parse(this.date)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    return TerminalLogEntity(
        state = this.state,
        message = this.message,
        date = time,
        id = this.id
    )
}


fun HistoryEntity.toDomain(): History {
    return History(
        command = this.command,
        id = this.id
    )
}

fun History.toEntity(): HistoryEntity {
    return HistoryEntity(
        command = this.command,
        id = this.id
    )
}


fun ScriptEntity.toDomain(): Script {
    return Script(
        name = this.name,
        command = this.command,
        id = this.id
    )
}

fun Script.toEntity(): ScriptEntity {
    return ScriptEntity(
        name = this.name,
        command = this.command,
        id = this.id
    )
}