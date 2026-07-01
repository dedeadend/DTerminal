package dedeadend.dterminal.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import dedeadend.dterminal.domain.model.Script
import kotlinx.coroutines.flow.Flow


@Dao
interface ScriptDao {

    @Query("SELECT * FROM script")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insertScript(command: ScriptEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertScript(commands: List<ScriptEntity>)

    @Query("DELETE FROM script")
    suspend fun deleteAllScripts()

    @Query("DELETE FROM script WHERE id = :id")
    suspend fun deleteScriptById(id: Int)

}