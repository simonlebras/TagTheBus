package fr.simonlebras.tagthebus.data.stations.list.network;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface StationService {
    @GET("bus/nearstation/latlon/41.3985182/2.1917991/1.json")
    Observable<StationListResponse> getStationList();
}
