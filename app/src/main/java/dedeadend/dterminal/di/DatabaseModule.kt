package dedeadend.dterminal.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dedeadend.dterminal.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        ).addCallback(dbCallback).build()
    }

    @Provides
    @Singleton
    fun provideSystemSettingsDao(database: AppDatabase) = database.systemSettingsDao()

    @Provides
    @Singleton
    fun provideTerminalLogDao(database: AppDatabase) = database.terminalLogDao()

    @Provides
    @Singleton
    fun provideHistoryDao(database: AppDatabase) = database.historyDao()

    @Provides
    @Singleton
    fun provideScriptDao(database: AppDatabase) = database.scriptDao()


    private val dbCallback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            CoroutineScope(Dispatchers.IO).launch {
                db.execSQL("DELETE FROM terminal_log WHERE id NOT IN (SELECT id FROM terminal_log ORDER BY id DESC LIMIT 1000)")
                db.execSQL("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY id DESC LIMIT 100)")
                db.execSQL("VACUUM")
            }
        }

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.execSQL("INSERT INTO system_settings (id, isFirstBoot, logSuccessFontColor, logErrorFontColor, logInfoFontColor, logFontSize) VALUES (1, 1, -1, -1, -1, 10)")
        }
    }
}