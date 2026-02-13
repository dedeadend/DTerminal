package dedeadend.dterminal.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dedeadend.dterminal.data.ShellCommandExecutor
import dedeadend.dterminal.domain.CommandExecutor
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TerminalModule {

    @Provides
    fun provideIoDispatcher() = Dispatchers.IO

    @Provides
    @Singleton
    fun provideCommandExecutor(): CommandExecutor {
        return ShellCommandExecutor()
    }
}