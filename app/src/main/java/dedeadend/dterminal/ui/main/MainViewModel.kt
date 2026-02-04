package dedeadend.dterminal.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dedeadend.dterminal.navigation.AppDestinations
import jakarta.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    var currentScreen by mutableStateOf(AppDestinations.TERMINAL)
        private set

    fun navigateTO(screen: AppDestinations) {
        currentScreen = screen
    }
}