package dedeadend.dterminal.data.repository

import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.data.local.HistoryDao
import dedeadend.dterminal.data.mapper.toDomain
import dedeadend.dterminal.data.mapper.toEntity
import dedeadend.dterminal.domain.model.History
import dedeadend.dterminal.domain.repository.HistoryRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao,
    private val dispatchers: AppDispatchers
) : HistoryRepository {

    override fun getHistory(): Flow<List<History>> = historyDao.getAllHistory().map { entityList ->
        entityList.map { entity ->
            entity.toDomain()
        }
    }

    override suspend fun addHistory(command: History) = withContext(dispatchers.io) {
        historyDao.insertHistory(command.toEntity())
    }

    override suspend fun restoreHistory(commands: List<History>) = withContext(dispatchers.io) {
        historyDao.insertHistory(commands.map { it.toEntity() })
    }

    override suspend fun deleteHistoryWithId(id: Int) = withContext(dispatchers.io) {
        historyDao.deleteHistoryById(id)
    }

    override suspend fun clearHistory() = withContext(dispatchers.io) {
        historyDao.deleteAllHistory()
    }
}