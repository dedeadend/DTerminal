package dedeadend.dterminal.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dedeadend.dterminal.domin.CommandDao
import dedeadend.dterminal.domin.History
import dedeadend.dterminal.domin.Script
import dedeadend.dterminal.domin.TerminalLog
import dedeadend.dterminal.domin.TerminalLogDao

@Database(entities = [TerminalLog::class, History::class, Script::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao

    abstract fun terminalLogDao(): TerminalLogDao
}
