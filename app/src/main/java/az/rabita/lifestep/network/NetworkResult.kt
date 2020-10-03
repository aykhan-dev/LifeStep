package az.rabita.lifestep.network

sealed class NetworkResult {

    data class Success<T>(val data: T) : NetworkResult()

    data class Failure(val type: NetworkResultFailureType, val message: String) : NetworkResult()

}

enum class NetworkResultFailureType {
    EXPIRED_TOKEN, ERROR
}