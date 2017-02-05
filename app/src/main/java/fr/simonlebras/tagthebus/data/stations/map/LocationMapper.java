package fr.simonlebras.tagthebus.data.stations.map;

import android.location.Location;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.LocationModel;

@PerFragment
public class LocationMapper {
    @Inject
    LocationMapper() {
    }

    LocationModel transform(Location location) {
        return LocationModel.create(location.getLatitude(), location.getLongitude());
    }
}
