package dedeadend.dterminal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_settings")
data class SettingsEntity(
    val isFirstBoot: Boolean = true,
    val logSuccessFontColor: Int = -1,
    val logErrorFontColor: Int = -1,
    val logInfoFontColor: Int = -1,
    val logFontSize: Int = 12,
    @PrimaryKey val id: Int = 1
)