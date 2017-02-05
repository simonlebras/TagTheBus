package fr.simonlebras.tagthebus.data.stations.map.exceptions;

public class LocationUnavailableException extends Exception {
    public LocationUnavailableException() {
        super("Current location is unavailable");
    }
}
