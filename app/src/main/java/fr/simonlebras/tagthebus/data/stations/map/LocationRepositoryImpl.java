package fr.simonlebras.tagthebus.data.stations.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.stations.map.exceptions.GoogleApiClientConnectionFailedException;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.GoogleApiClientConnectionSuspendedException;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.LocationPermissionException;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.LocationSettingsException;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.LocationUnavailableException;
import fr.simonlebras.tagthebus.data.stations.utils.OnErrorRetryCache;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.LocationModel;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

@PerFragment
public class LocationRepositoryImpl implements LocationRepository {
    private final Context context;

    private final LocationMapper locationMapper;

    private OnErrorRetryCache<LocationModel> locationCache;
    private OnErrorRetryCache<GoogleApiClient> googleApiClientCache;

    @Inject
    LocationRepositoryImpl(Context context, LocationMapper locationMapper) {
        this.context = context;

        this.locationMapper = locationMapper;
    }

    @Override
    public Observable<LocationModel> getCurrentLocation() {
        if (locationCache == null) {
            final Observable<LocationModel> source = getGoogleApiClient()
                    .flatMap(googleApiClient -> {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            return Observable.<Location>create(subscriber -> {
                                final LocationRequest locationRequest = createLocationRequest();

                                final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                                        .addLocationRequest(locationRequest);

                                final PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                                        .checkLocationSettings(googleApiClient, builder.build());

                                final LocationListener locationListener = location -> {
                                    if (!subscriber.isDisposed() && location != null) {
                                        subscriber.onNext(location);
                                    }
                                };

                                result.setResultCallback(locationSettingsResult -> {
                                    final Status status = locationSettingsResult.getStatus();
                                    switch (status.getStatusCode()) {
                                        case LocationSettingsStatusCodes.SUCCESS:
                                            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);

                                            final Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                                            if (!subscriber.isDisposed() && location != null) {
                                                subscriber.onNext(location);
                                            }
                                            break;
                                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                            if (!subscriber.isDisposed()) {
                                                subscriber.onError(new LocationSettingsException(status));
                                            }
                                            break;
                                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                            if (!subscriber.isDisposed()) {
                                                subscriber.onError(new LocationUnavailableException());
                                            }
                                            break;
                                    }
                                });

                                subscriber.setCancellable(() -> {
                                    LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);

                                    result.cancel();
                                });
                            });
                        }

                        return Observable.error(new LocationPermissionException());
                    })
                    .map(locationMapper::transform);


            locationCache = new OnErrorRetryCache<>(source);
        }

        return locationCache.get();
    }

    private Observable<GoogleApiClient> getGoogleApiClient() {
        if (googleApiClientCache == null) {
            final Observable<GoogleApiClient> source = Observable.create(subscriber -> {
                final LocationConnectionCallbacks locationConnectionCallbacks = new LocationConnectionCallbacks(subscriber);

                final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(locationConnectionCallbacks)
                        .addOnConnectionFailedListener(locationConnectionCallbacks)
                        .build();

                locationConnectionCallbacks.setGoogleApiClient(googleApiClient);

                subscriber.setCancellable(() -> {
                    googleApiClient.unregisterConnectionCallbacks(locationConnectionCallbacks);

                    googleApiClient.unregisterConnectionFailedListener(locationConnectionCallbacks);

                    if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                        googleApiClient.disconnect();
                    }
                });

                googleApiClient.connect();
            });

            googleApiClientCache = new OnErrorRetryCache<>(source);
        }

        return googleApiClientCache.get();
    }

    private LocationRequest createLocationRequest() {
        return new LocationRequest()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void clear() {
        if (locationCache != null) {
            locationCache.clear();
        }

        if (googleApiClientCache != null) {
            googleApiClientCache.clear();
        }
    }

    private static class LocationConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private final ObservableEmitter<GoogleApiClient> subscriber;

        private GoogleApiClient googleApiClient;

        LocationConnectionCallbacks(final ObservableEmitter<GoogleApiClient> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            if (!subscriber.isDisposed()) {
                subscriber.onNext(googleApiClient);
            }
        }

        @Override
        public void onConnectionSuspended(final int i) {
            if (!subscriber.isDisposed()) {
                subscriber.onError(new GoogleApiClientConnectionSuspendedException());
            }
        }

        @Override
        public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
            if (!subscriber.isDisposed()) {
                subscriber.onError(new GoogleApiClientConnectionFailedException(connectionResult));
            }
        }

        void setGoogleApiClient(final GoogleApiClient googleApiClient) {
            this.googleApiClient = googleApiClient;
        }
    }
}
