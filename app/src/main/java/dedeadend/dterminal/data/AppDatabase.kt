package dedeadend.dterminal.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dedeadend.dterminal.domin.CommandDao
import dedeadend.dterminal.domin.History
import dedeadend.dterminal.domin.Script

@Database(entities = [History::class, Script::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
