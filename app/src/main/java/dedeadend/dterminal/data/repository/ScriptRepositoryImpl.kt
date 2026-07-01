package dedeadend.dterminal.data.repository

import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.data.local.ScriptDao
import dedeadend.dterminal.data.mapper.toDomain
import dedeadend.dterminal.data.mapper.toEntity
import dedeadend.dterminal.domain.model.Script
import dedeadend.dterminal.domain.repository.ScriptRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ScriptRepositoryImpl @Inject constructor(
    private val scriptDao: ScriptDao,
    private val dispatchers: AppDispatchers
) : ScriptRepository {
    override fun getScripts(): Flow<List<Script>> = scriptDao.getAllScripts().map { entityList ->
        entityList.map { entity ->
            entity.toDomain()
        }
    }

    override suspend fun addScript(command: Script) = withContext(dispatchers.io) {
        scriptDao.insertScript(command.toEntity())
    }

    override suspend fun deleteScriptWithId(id: Int) = withContext(dispatchers.io) {
        scriptDao.deleteScriptById(id)
    }
}