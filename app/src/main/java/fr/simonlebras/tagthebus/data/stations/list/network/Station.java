package fr.simonlebras.tagthebus.data.stations.list.network;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class Station {
    public static JsonAdapter<Station> jsonAdapter(Moshi moshi) {
        return new AutoValue_Station.MoshiJsonAdapter(moshi);
    }

    @Json(name = "id")
    @Nullable
    public abstract String id();

    @Json(name = "street_name")
    @Nullable
    public abstract String streetName();

    @Json(name = "lat")
    @Nullable
    public abstract String latitude();

    @Json(name = "lon")
    @Nullable
    public abstract String longitude();

    public boolean isValid() {
        try {
            final double latitude = Double.parseDouble(latitude());
            final double longitude = Double.parseDouble(longitude());

            return id() != null && streetName() != null && latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
