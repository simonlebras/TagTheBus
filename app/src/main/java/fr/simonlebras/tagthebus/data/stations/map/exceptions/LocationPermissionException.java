package fr.simonlebras.tagthebus.data.stations.map.exceptions;

public class LocationPermissionException extends Exception {
    public LocationPermissionException() {
        super("Location permission is required");
    }
}
