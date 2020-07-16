package az.rabita.lifestep.network

sealed class NetworkState {
    data class Success<T>(val data: T) : NetworkState()

    object InvalidData : NetworkState()
    object ExpiredToken : NetworkState()

    data class HandledHttpError(val error: String?) : NetworkState()
    data class UnhandledHttpError(val error: String?) : NetworkState()

    data class NetworkException(val exception: String?) : NetworkState()
}