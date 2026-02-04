package dedeadend.dterminal.domin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_command")
data class HistoryCommand(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String
)

@Entity(tableName = "saved_command")
data class SavedCommand(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String
)