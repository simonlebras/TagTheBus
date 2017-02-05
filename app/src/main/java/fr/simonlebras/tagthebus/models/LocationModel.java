package fr.simonlebras.tagthebus.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LocationModel {
    public static LocationModel create(double latitude, double longitude) {
        return new AutoValue_LocationModel(latitude, longitude);
    }

    public abstract double latitude();

    public abstract double longitude();
}
