package dedeadend.dterminal.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dedeadend.dterminal.data.AppDatabase
import dedeadend.dterminal.data.Repository
import dedeadend.dterminal.domin.CommandDao
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCommandDao(database: AppDatabase) = database.commandDao()

    @Provides
    @Singleton
    fun provideRepository(commandDao: CommandDao, ioDispatcher: CoroutineDispatcher) =
        Repository(commandDao, ioDispatcher)

}