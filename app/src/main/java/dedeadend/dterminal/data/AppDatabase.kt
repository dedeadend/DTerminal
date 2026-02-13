package dedeadend.dterminal.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dedeadend.dterminal.domain.CommandDao
import dedeadend.dterminal.domain.History
import dedeadend.dterminal.domain.Script
import dedeadend.dterminal.domain.TerminalLog
import dedeadend.dterminal.domain.TerminalLogDao

@Database(entities = [TerminalLog::class, History::class, Script::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao

    abstract fun terminalLogDao(): TerminalLogDao
}
