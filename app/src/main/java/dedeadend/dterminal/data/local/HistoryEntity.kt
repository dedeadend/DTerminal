package dedeadend.dterminal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    val command: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)