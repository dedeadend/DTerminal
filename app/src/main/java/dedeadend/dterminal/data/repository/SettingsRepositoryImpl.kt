package dedeadend.dterminal.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.data.local.SettingsDao
import dedeadend.dterminal.data.mapper.toDomain
import dedeadend.dterminal.data.mapper.toEntity
import dedeadend.dterminal.domain.model.Settings
import dedeadend.dterminal.domain.repository.SettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDao: SettingsDao,
    private val dispatchers: AppDispatchers
) : SettingsRepository {
    override fun getSystemSettings(): Flow<Settings> =
        settingsDao.getSettings().map { it.toDomain() }

    override suspend fun setFirstBootCompleted() = withContext(dispatchers.io) {
        val currentSettings = getSystemSettings().first().toEntity()
        settingsDao.updateSettings(currentSettings.copy(isFirstBoot = false))
    }

    override suspend fun setLogSuccessFontColor(r: Int, g: Int, b: Int) =
        withContext(dispatchers.io) {
            val currentSettings = getSystemSettings().first().toEntity()
            settingsDao.updateSettings(
                currentSettings.copy(logSuccessFontColor = Color(r, g, b).toArgb())
            )
        }

    override suspend fun setLogErrorFontColor(r: Int, g: Int, b: Int) =
        withContext(dispatchers.io) {
            val currentSettings = getSystemSettings().first().toEntity()
            settingsDao.updateSettings(
                currentSettings.copy(logErrorFontColor = Color(r, g, b).toArgb())
            )
        }

    override suspend fun setLogInfoFontColor(r: Int, g: Int, b: Int) = withContext(dispatchers.io) {
        val currentSettings = getSystemSettings().first().toEntity()
        settingsDao.updateSettings(
            currentSettings.copy(logInfoFontColor = Color(r, g, b).toArgb())
        )
    }

    override suspend fun setLogFontSize(size: Int) = withContext(dispatchers.io) {
        val currentSettings = getSystemSettings().first().toEntity()
        settingsDao.updateSettings(currentSettings.copy(logFontSize = size))
    }
}