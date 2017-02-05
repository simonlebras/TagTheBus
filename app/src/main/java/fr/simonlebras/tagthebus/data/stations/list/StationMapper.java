package fr.simonlebras.tagthebus.data.stations.list;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.stations.list.network.Station;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.StationModel;

@PerFragment
public class StationMapper {
    @Inject
    StationMapper() {
    }

    StationModel transform(Station station) {
        return StationModel.create(station.id(), station.streetName(), Double.parseDouble(station.latitude()), Double.parseDouble(station.longitude()));
    }
}
