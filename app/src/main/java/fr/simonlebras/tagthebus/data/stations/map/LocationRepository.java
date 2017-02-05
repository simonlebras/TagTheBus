package fr.simonlebras.tagthebus.data.stations.map;

import fr.simonlebras.tagthebus.models.LocationModel;
import io.reactivex.Observable;

public interface LocationRepository {
    Observable<LocationModel> getCurrentLocation();

    void clear();
}
