package dedeadend.dterminal.ui.script

sealed interface ScriptUiEffect {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : ScriptUiEffect
}