package dedeadend.dterminal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TerminalLogEntity::class, HistoryEntity::class, ScriptEntity::class, SettingsEntity::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun systemSettingsDao(): SettingsDao

    abstract fun terminalLogDao(): TerminalLogDao

    abstract fun historyDao(): HistoryDao

    abstract fun scriptDao(): ScriptDao

}