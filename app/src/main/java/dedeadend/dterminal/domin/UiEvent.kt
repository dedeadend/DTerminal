package dedeadend.dterminal.domin

sealed class UiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
}
