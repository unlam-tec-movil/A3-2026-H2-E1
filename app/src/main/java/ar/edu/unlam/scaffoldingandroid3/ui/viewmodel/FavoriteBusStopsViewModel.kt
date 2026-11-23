
package ar.edu.unlam.scaffoldingandroid3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.scaffoldingandroid3.domain.model.BusStop
import ar.edu.unlam.scaffoldingandroid3.domain.model.FavoriteStopSnapshot
import ar.edu.unlam.scaffoldingandroid3.domain.model.toFavoriteSnapshot
import ar.edu.unlam.scaffoldingandroid3.domain.repository.FavoriteBusStopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteBusStopsViewModel
    @Inject
    constructor(
        private val repository: FavoriteBusStopRepository,
    ) : ViewModel() {
        val favoriteStopIds =
            repository.observeFavoriteStopIds().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet(),
            )

        val favoriteStops =
            repository.observeFavoriteStops().stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
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

        fun restoreFavorite(stop: FavoriteStopSnapshot) {
            viewModelScope.launch {
                repository.addFavorite(stop)
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

        fun toggleFavorite(stop: BusStop) {
            viewModelScope.launch {
                if (repository.isFavorite(stop.stopId)) {
                    repository.removeFavorite(stop.stopId)
                } else {
                    repository.addFavorite(stop.toFavoriteSnapshot())
                }
            }
        }
    }
