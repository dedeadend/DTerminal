package dedeadend.dterminal.domain

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<History>>

    @Query("SELECT * FROM script")
    fun getAllScripts(): Flow<List<Script>>


    @Insert(onConflict = REPLACE)
    suspend fun insertHistory(command: History)

    @Insert(onConflict = REPLACE)
    suspend fun insertScript(command: Script)


    @Insert(onConflict = REPLACE)
    suspend fun insertHistory(commands: List<History>)

    @Insert(onConflict = REPLACE)
    suspend fun insertScript(commands: List<Script>)


    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()

    @Query("DELETE FROM script")
    suspend fun deleteAllScripts()


    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM script WHERE id = :id")
    suspend fun deleteScriptById(id: Int)

}

