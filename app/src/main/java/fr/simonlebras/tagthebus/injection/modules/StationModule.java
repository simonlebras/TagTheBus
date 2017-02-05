package fr.simonlebras.tagthebus.injection.modules;

import dagger.Module;
import dagger.Provides;
import fr.simonlebras.tagthebus.data.stations.list.StationRepository;
import fr.simonlebras.tagthebus.data.stations.list.StationRepositoryImpl;
import fr.simonlebras.tagthebus.data.stations.map.LocationRepository;
import fr.simonlebras.tagthebus.data.stations.map.LocationRepositoryImpl;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;

@Module
public class StationModule {
    @Provides
    @PerFragment
    StationRepository providesStationRepository(StationRepositoryImpl repository) {
        return repository;
    }

    @Provides
    @PerFragment
    LocationRepository providesLocationRepository(LocationRepositoryImpl repository) {
        return repository;
    }
}
