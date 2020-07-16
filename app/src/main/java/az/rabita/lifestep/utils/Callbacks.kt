package az.rabita.lifestep.utils

data class PaginationListeners(
    val onErrorListener: (message: String?) -> Unit,
    val onExpireTokenListener: () -> Unit
)