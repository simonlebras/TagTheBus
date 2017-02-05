package fr.simonlebras.tagthebus.data.stations.list;

import java.util.List;

import fr.simonlebras.tagthebus.models.StationModel;
import io.reactivex.Observable;

public interface StationRepository {
    Observable<List<StationModel>> loadStationList();

    void clear();
}
