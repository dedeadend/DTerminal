package dedeadend.dterminal.domin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String
)

@Entity(tableName = "script")
data class Script(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val message: String
)