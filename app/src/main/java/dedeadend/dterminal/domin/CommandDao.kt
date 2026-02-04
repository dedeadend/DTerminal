package dedeadend.dterminal.domin

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM history_command")
    fun getAllHistoryCommands(): Flow<List<HistoryCommand>>

    @Query("SELECT * FROM saved_command")
    fun getAllSavedCommands(): Flow<List<SavedCommand>>


    @Insert
    suspend fun insertHistoryCommand(command: HistoryCommand)

    @Insert
    suspend fun insertSavedCommand(command: SavedCommand)


    @Query("DELETE FROM history_command")
    suspend fun deleteAllHistoryCommands()

    @Query("DELETE FROM saved_command")
    suspend fun deleteAllSavedCommands()


    @Query("DELETE FROM history_command WHERE id = :id")
    suspend fun deleteHistoryCommandById(id: Int)

    @Query("DELETE FROM saved_command WHERE id = :id")
    suspend fun deleteSavedCommandById(id: Int)
}

