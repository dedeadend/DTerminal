package dedeadend.dterminal.domain.model

data class Settings(
    val isFirstBoot: Boolean = true,
    val logSuccessFontColor: Int = -1,
    val logErrorFontColor: Int = -1,
    val logInfoFontColor: Int = -1,
    val logFontSize: Int = 10,
    val id: Int = 1
)