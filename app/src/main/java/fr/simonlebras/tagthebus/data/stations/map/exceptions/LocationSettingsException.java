package fr.simonlebras.tagthebus.data.stations.map.exceptions;

import com.google.android.gms.common.api.Status;

public class LocationSettingsException extends Exception {
    private Status status;

    public LocationSettingsException(Status status) {
        super("Location settings are not satisfied");

        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}