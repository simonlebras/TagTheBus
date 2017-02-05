package fr.simonlebras.tagthebus.data.stations.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.stations.list.network.Station;
import fr.simonlebras.tagthebus.data.stations.list.network.StationService;
import fr.simonlebras.tagthebus.data.stations.utils.OnErrorRetryCache;
import fr.simonlebras.tagthebus.data.stations.utils.RetryPolicy;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.StationModel;
import io.reactivex.Observable;

@PerFragment
public class StationRepositoryImpl implements StationRepository {
    private final StationService stationService;

    private final StationMapper stationMapper;

    private OnErrorRetryCache<List<StationModel>> stationCache;

    @Inject
    StationRepositoryImpl(StationService stationService, StationMapper stationMapper) {
        this.stationService = stationService;

        this.stationMapper = stationMapper;
    }

    @Override
    public Observable<List<StationModel>> loadStationList() {
        if (stationCache == null) {
            final List<Class<? extends Throwable>> exceptions = new ArrayList<>();
            exceptions.add(IOException.class);

            final Observable<List<StationModel>> source = stationService.getStationList()
                    .retryWhen(new RetryPolicy(2, TimeUnit.SECONDS, 3, exceptions))
                    .flatMap(response -> Observable.fromIterable(response.wrapper().stations())
                            .filter(Station::isValid)
                            .map(stationMapper::transform)
                            .toList()
                            .toObservable());

            stationCache = new OnErrorRetryCache<>(source);
        }

        return stationCache.get();
    }

    @Override
    public void clear() {
        if (stationCache != null) {
            stationCache.clear();
        }
    }
}
