package dedeadend.dterminal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insertHistory(command: HistoryEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertHistory(commands: List<HistoryEntity>)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

}