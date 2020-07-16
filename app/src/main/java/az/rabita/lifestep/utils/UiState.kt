package az.rabita.lifestep.utils

sealed class UiState {
    object Loading : UiState()
    object LoadingFinished : UiState()
}