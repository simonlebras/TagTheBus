package fr.simonlebras.tagthebus.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StationModel {
    public static StationModel create(String id, String streetName, double latitude, double longitude) {
        return new AutoValue_StationModel(id, streetName, latitude, longitude);
    }

    public abstract String id();

    public abstract String streetName();

    public abstract double latitude();

    public abstract double longitude();
}
