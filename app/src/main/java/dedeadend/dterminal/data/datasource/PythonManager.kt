package dedeadend.dterminal.data.datasource

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.RemoteException
import dagger.hilt.android.qualifiers.ApplicationContext
import dedeadend.dterminal.core.AppDispatchers
import dedeadend.dterminal.domain.model.TerminalLog
import dedeadend.dterminal.domain.model.TerminalState
import dedeadend.dterminal.domain.repository.TerminalLogRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

@Singleton
class PythonManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val terminalLogRepository: TerminalLogRepository,
    private val dispatchers: AppDispatchers
) {
    @Volatile
    private var mService: Messenger? = null

    @Volatile
    private var isBound = false

    @Volatile
    private var workerPid: Int = -1

    @Volatile
    private var warmup = true

    @Volatile
    private var streamChannel: Channel<StreamEvent>? = null

    private sealed interface StreamEvent {
        data class Chunk(val text: String, val state: TerminalState) : StreamEvent
        object Finished : StreamEvent
    }

    suspend fun execute(codeBlock: String) {
        withContext(dispatchers.default) {
            if (!isBound || mService == null) {
                bindService()
                if (warmup) {
                    warmup = false
                    terminalLogRepository.addLog(
                        TerminalLog(TerminalState.Error, "Starting Python Engine...\n")
                    )
                    delay(3000L)
                    execute(codeBlock)
                } else {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Python Engine is warming up...\nTry again in a second."
                        )
                    )
                }
            } else {
                val channel = Channel<StreamEvent>(Channel.UNLIMITED)
                streamChannel = channel

                try {
                    val msg = Message.obtain(null, PythonService.MSG_RUN_BLOCK).apply {
                        replyTo = clientMessenger
                        data = Bundle().apply {
                            putString(PythonService.KEY_CODE_BLOCK, codeBlock)
                        }
                    }
                    mService?.send(msg)
                } catch (e: RemoteException) {
                    terminalLogRepository.addLog(
                        TerminalLog(
                            TerminalState.Error,
                            "Failed to talk with Python Worker: ${e.localizedMessage}"
                        )
                    )
                    streamChannel = null
                    return@withContext
                }

                for (event in channel) {
                    when (event) {
                        is StreamEvent.Chunk -> {
                            terminalLogRepository.addLog(TerminalLog(event.state, event.text))
                        }

                        is StreamEvent.Finished -> {
                            break
                        }
                    }
                }
                streamChannel = null
            }
        }
    }

    suspend fun cancel() {
        withContext(dispatchers.default) {
            if (workerPid != -1) {
                Process.killProcess(workerPid)

                streamChannel?.trySend(
                    StreamEvent.Chunk(
                        "Process terminated by user.\n",
                        TerminalState.Error
                    )
                )
                streamChannel?.trySend(StreamEvent.Finished)

                unbindService()
                bindService()
            }
        }
    }

    private val clientMessenger = Messenger(Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            PythonService.MSG_HANDSHAKE -> {
                workerPid = msg.data.getInt(PythonService.KEY_SERVICE_PID, -1)
                true
            }

            PythonService.MSG_OUTPUT_RESULT -> {
                val result = msg.data.getString(PythonService.KEY_RESULT_TEXT) ?: ""

                val state =
                    if (result.contains("Python Runtime Error") || result.contains("Failed to talk")) {
                        TerminalState.Error
                    } else {
                        TerminalState.Success
                    }

                streamChannel?.trySend(StreamEvent.Chunk(result, state))
                true
            }


            PythonService.MSG_RUN_COMPLETED -> {
                streamChannel?.trySend(StreamEvent.Finished)
                true
            }

            else -> false
        }
    })

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            isBound = true
            try {
                val msg = Message.obtain(null, PythonService.MSG_HANDSHAKE).apply {
                    replyTo = clientMessenger
                }
                mService?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
            isBound = false
            workerPid = -1
            streamChannel?.trySend(StreamEvent.Finished)
        }
    }

    private fun bindService() {
        if (!isBound) {
            val intent = Intent(context, PythonService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            mService = null
            workerPid = -1
        }
    }
}