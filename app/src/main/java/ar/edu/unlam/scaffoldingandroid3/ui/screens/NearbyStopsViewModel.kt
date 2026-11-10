package ar.edu.unlam.scaffoldingandroid3.ui.screens

import androidx.lifecycle.ViewModel
import ar.edu.unlam.scaffoldingandroid3.domain.BusStopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NearbyStopsViewModel
    @Inject
    constructor(
        val busStopRepository: BusStopRepository,
    ) : ViewModel()
