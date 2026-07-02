package dedeadend.dterminal.ui.history

sealed interface HistoryUiEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : HistoryUiEffect
}