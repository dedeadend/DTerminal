package dedeadend.dterminal.domin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    val command: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = "script")
data class Script(
    val name: String,
    val command: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = "log")
data class Log(
    val log: String,
    @PrimaryKey val date: Long = System.currentTimeMillis()
)