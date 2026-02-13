package dedeadend.dterminal.data

import dedeadend.dterminal.domain.CommandDao
import dedeadend.dterminal.domain.History
import dedeadend.dterminal.domain.Script
import dedeadend.dterminal.domain.TerminalLog
import dedeadend.dterminal.domain.TerminalLogDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    private val commandDao: CommandDao,
    private val terminalLogDao: TerminalLogDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun getHistory(): Flow<List<History>> = commandDao.getAllHistory()
    fun getScripts(): Flow<List<Script>> = commandDao.getAllScripts()

    fun getLogs(): Flow<List<TerminalLog>> = terminalLogDao.getLogs()


    suspend fun insertToHistory(command: History) = withContext(ioDispatcher) {
        commandDao.insertHistory(command)
    }

    suspend fun insertToScripts(command: Script) = withContext(ioDispatcher) {
        commandDao.insertScript(command)
    }

    suspend fun insertToLogs(log: TerminalLog) = withContext(ioDispatcher) {
        terminalLogDao.insertLog(log)
    }

    suspend fun restoreHistory(commands: List<History>) = withContext(ioDispatcher) {
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

    suspend fun deleteLogs() = withContext(ioDispatcher) {
        terminalLogDao.deleteLogs()
    }

}