package dedeadend.dterminal.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log")
data class TerminalLog(
    val state: TerminalState,
    val message: String,
    val date: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)