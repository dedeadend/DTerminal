package dedeadend.dterminal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import dedeadend.dterminal.domain.model.TerminalState

@Entity(tableName = "terminal_log")
data class TerminalLogEntity(
    val state: TerminalState,
    val message: String,
    val date: Long,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)