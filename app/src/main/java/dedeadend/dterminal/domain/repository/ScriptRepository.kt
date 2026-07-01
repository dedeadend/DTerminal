package dedeadend.dterminal.domain.repository

import dedeadend.dterminal.domain.model.Script
import kotlinx.coroutines.flow.Flow

interface ScriptRepository {

    fun getScripts(): Flow<List<Script>>

    suspend fun addScript(command: Script)

    suspend fun deleteScriptWithId(id: Int)
}