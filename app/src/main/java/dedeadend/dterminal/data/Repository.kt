package dedeadend.dterminal.data

import dedeadend.dterminal.domin.CommandDao
import dedeadend.dterminal.domin.HistoryCommand
import dedeadend.dterminal.domin.SavedCommand
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Repository @Inject constructor(
    private val commandDao: CommandDao,
    private val ioDispatcher: CoroutineDispatcher
) {
    fun getHistory(): Flow<List<HistoryCommand>> = commandDao.getAllHistoryCommands()
    fun getSaved(): Flow<List<SavedCommand>> = commandDao.getAllSavedCommands()

    suspend fun insertToHistory(command: HistoryCommand) = withContext(ioDispatcher) {
        commandDao.insertHistoryCommand(HistoryCommand(message = command.message))
    }

    suspend fun insertToSaved(command: SavedCommand) = withContext(ioDispatcher) {
        commandDao.insertSavedCommand(SavedCommand(message = command.message))
    }

    suspend fun deleteHistory(id: Int) = withContext(ioDispatcher) {
        commandDao.deleteHistoryCommandById(id)
    }

    suspend fun deleteSaved(id: Int) = withContext(ioDispatcher) {
        commandDao.deleteSavedCommandById(id)
    }

    suspend fun deleteAllHistory() = withContext(ioDispatcher) {
        commandDao.deleteAllHistoryCommands()
    }

    suspend fun deleteAllSaved() = withContext(ioDispatcher) {
        commandDao.deleteAllSavedCommands()
    }
}