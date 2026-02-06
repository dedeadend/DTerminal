package dedeadend.dterminal.data

import dedeadend.dterminal.domin.CommandDao
import dedeadend.dterminal.domin.History
import dedeadend.dterminal.domin.Script
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    private val commandDao: CommandDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun getHistory(): Flow<List<History>> = commandDao.getAllHistory()
    fun getScripts(): Flow<List<Script>> = commandDao.getAllScripts()

    suspend fun insertToHistory(command: History) = withContext(ioDispatcher) {
        commandDao.insertHistory(command)
    }

    suspend fun insertToScripts(command: Script) = withContext(ioDispatcher) {
        commandDao.insertScript(command)
    }

    suspend fun restoreHistory(commands: List<History>) = withContext(ioDispatcher) {
        commandDao.insertHistory(commands)
    }

    suspend fun restoreScripts(commands: List<History>) = withContext(ioDispatcher) {
        commandDao.insertHistory(commands)
    }

    suspend fun deleteHistoryWithId(id: Int) = withContext(ioDispatcher) {
        commandDao.deleteHistoryById(id)
    }

    suspend fun deleteScriptWithId(id: Int) = withContext(ioDispatcher) {
        commandDao.deleteScriptById(id)
    }

    suspend fun deleteAllHistory() = withContext(ioDispatcher) {
        commandDao.deleteAllHistory()
    }

    suspend fun deleteAllScripts() = withContext(ioDispatcher) {
        commandDao.deleteAllScripts()
    }
}