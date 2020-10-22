package az.rabita.lifestep.network

sealed class NetworkResult {

    data class Success<T>(val data: T) : NetworkResult()

    sealed class Exceptions(code: Int, message: String) : IllegalStateException(message) {
        class Failure(code: Int, message: String) : Exceptions(code, message)
        class RepeatedAction(code: Int, message: String) : Exceptions(code, message)
        class ExpiredToken(code: Int, message: String) : Exceptions(code, message)
        class ServerError(code: Int, message: String) : Exceptions(code, message)
    }

}