package fr.simonlebras.tagthebus.data.stations.map.exceptions;

import com.google.android.gms.common.ConnectionResult;

public class GoogleApiClientConnectionFailedException extends Exception {
    private final ConnectionResult connectionResult;

    public GoogleApiClientConnectionFailedException(ConnectionResult connectionResult) {
        super("Failed to connect to Google Play Services");

        this.connectionResult = connectionResult;
    }

    public ConnectionResult getConnectionResult() {
        return connectionResult;
    }
}
