package dedeadend.dterminal.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dedeadend.dterminal.domin.CommandDao
import dedeadend.dterminal.domin.HistoryCommand
import dedeadend.dterminal.domin.SavedCommand
import dedeadend.dterminal.domin.TerminalMessage

@Database(entities = [HistoryCommand::class, SavedCommand::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
