package fr.simonlebras.tagthebus.data.stations.map.exceptions;

public class GoogleApiClientConnectionSuspendedException extends Exception {
    public GoogleApiClientConnectionSuspendedException() {
        super("Connection to Google Play Services is suspended");
    }
}
