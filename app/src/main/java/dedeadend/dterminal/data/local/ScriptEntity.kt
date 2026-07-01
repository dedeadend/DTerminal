package dedeadend.dterminal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script")
data class ScriptEntity(
    val name: String,
    val command: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)