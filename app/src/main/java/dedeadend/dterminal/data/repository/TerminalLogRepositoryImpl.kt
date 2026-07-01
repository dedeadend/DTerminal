package dedeadend.dterminal.data.repository

import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.data.local.TerminalLogDao
import dedeadend.dterminal.data.mapper.toDomain
import dedeadend.dterminal.data.mapper.toEntity
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TerminalLogRepositoryImpl @Inject constructor(
    private val terminalLogDao: TerminalLogDao,
    private val dispatchers: AppDispatchers
) : TerminalLogRepository{

    override fun getLogs(): Flow<List<TerminalLog>> = terminalLogDao.getLogs().map { entityList ->
        entityList.map { entity ->
            entity.toDomain()
        }
    }

    override suspend fun addLog(log: TerminalLog) = withContext(dispatchers.io) {
        terminalLogDao.insertLog(log.toEntity())
    }

    override suspend fun clearLogs() = withContext(dispatchers.io) {
        terminalLogDao.deleteLogs()
    }
}