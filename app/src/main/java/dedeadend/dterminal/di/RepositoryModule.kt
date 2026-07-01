package dedeadend.dterminal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dedeadend.dterminal.data.repository.HistoryRepositoryImpl
import dedeadend.dterminal.data.repository.ScriptRepositoryImpl
import dedeadend.dterminal.data.repository.SettingsRepositoryImpl
import dedeadend.dterminal.data.repository.ShellCommandExecutor
import dedeadend.dterminal.data.repository.TerminalLogRepositoryImpl
import dedeadend.dterminal.domain.repository.CommandExecutor
import dedeadend.dterminal.domain.repository.HistoryRepository
import dedeadend.dterminal.domain.repository.ScriptRepository
import dedeadend.dterminal.domain.repository.SettingsRepository
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTerminalLogRepository(impl: TerminalLogRepositoryImpl): TerminalLogRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindScriptRepository(impl: ScriptRepositoryImpl): ScriptRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindCommandExecutor(impl: ShellCommandExecutor): CommandExecutor

}