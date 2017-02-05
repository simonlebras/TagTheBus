package fr.simonlebras.tagthebus.data.stations.list.network;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class StationListResponse {
    @Json(name = "data")
    public abstract StationListWrapper wrapper();

    public static JsonAdapter<StationListResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_StationListResponse.MoshiJsonAdapter(moshi);
    }
}
