package dedeadend.dterminal.core

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


@Singleton
class AppDispatchers @Inject constructor() {
    val io: CoroutineDispatcher = Dispatchers.IO
    val main: CoroutineDispatcher = Dispatchers.Main
    val default: CoroutineDispatcher = Dispatchers.Default
}
