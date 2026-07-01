package dedeadend.dterminal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import dedeadend.dterminal.domain.model.TerminalLog
import kotlinx.coroutines.flow.Flow

@Dao
interface TerminalLogDao {

    @Query("SELECT * FROM terminal_log ORDER BY id DESC")
    fun getLogs(): Flow<List<TerminalLogEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insertLog(log: TerminalLogEntity)

    @Query("DELETE FROM terminal_log")
    suspend fun deleteLogs()
}