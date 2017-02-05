package fr.simonlebras.tagthebus.data.stations.list.network;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.List;

@AutoValue
public abstract class StationListWrapper {
    public static JsonAdapter<StationListWrapper> jsonAdapter(Moshi moshi) {
        return new AutoValue_StationListWrapper.MoshiJsonAdapter(moshi);
    }

    @Json(name = "nearstations")
    public abstract List<Station> stations();
}
