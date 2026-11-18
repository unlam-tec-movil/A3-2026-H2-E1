
package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.repository.FavoriteBusStopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavoriteBusStopsViewModel @Inject constructor(
    private val repository: FavoriteBusStopRepository,
) : ViewModel() {

    val favoriteStopIds = repository.observeFavoriteStopIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet(),
        )

    fun addFavorite(stopId: Long) {
        viewModelScope.launch {
            repository.addFavorite(stopId)
        }
    }

    fun removeFavorite(stopId: Long) {
        viewModelScope.launch {
            repository.removeFavorite(stopId)
        }
    }

    fun toggleFavorite(stopId: Long) {
        viewModelScope.launch {
            if (repository.isFavorite(stopId)) {
                repository.removeFavorite(stopId)
            } else {
                repository.addFavorite(stopId)
            }
        }
    }
}
