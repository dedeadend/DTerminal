package dedeadend.dterminal.domain.model

data class Script(
    val name: String,
    val command: String,
    val id: Int = 0
)