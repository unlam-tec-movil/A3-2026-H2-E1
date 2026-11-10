package ar.edu.unlam.scaffoldingandroid3.data.mapper

import android.location.Location
import ar.edu.unlam.scaffoldingandroid3.domain.model.Latitude
import ar.edu.unlam.scaffoldingandroid3.domain.model.LocationData
import ar.edu.unlam.scaffoldingandroid3.domain.model.Longitude

class LocationMapper {
    fun toDomain(location: Location): LocationData =
        LocationData(
            Latitude(location.latitude),
            Longitude(location.longitude),
        )
}
