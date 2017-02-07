package fr.simonlebras.tagthebus.presentation.stations.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.GoogleApiClientConnectionFailedException;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.LocationSettingsException;
import fr.simonlebras.tagthebus.injection.components.ApplicationComponent;
import fr.simonlebras.tagthebus.injection.components.StationComponent;
import fr.simonlebras.tagthebus.injection.modules.StationModule;
import fr.simonlebras.tagthebus.models.LocationModel;
import fr.simonlebras.tagthebus.models.StationModel;
import fr.simonlebras.tagthebus.presentation.base.BaseFragment;
import fr.simonlebras.tagthebus.presentation.base.PresenterManager;
import fr.simonlebras.tagthebus.presentation.navigator.Navigator;

public class StationMapFragment extends BaseFragment<StationMapPresenter, StationMapPresenter.View> implements StationMapPresenter.View, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    public static final int RESOLVE_ERROR_REQUEST_CODE = 100;
    public static final int CHECK_SETTINGS_REQUEST_CODE = 101;

    private static final int LOCATION_REQUEST_CODE = 102;

    private static final String BUNDLE_RESOLVING_ERROR = "BUNDLE_RESOLVING_ERROR";

    private static final String BUNDLE_CURRENT_LATITUDE = "BUNDLE_CURRENT_LATITUDE";
    private static final String BUNDLE_CURRENT_LONGITUDE = "BUNDLE_CURRENT_LONGITUDE";
    private static final double INVALID_COORDINATE = 1000;

    @Inject
    Navigator navigator;

    @BindView(R.id.map_view)
    MapView mapView;

    private StationComponent component;

    private Unbinder unbinder;

    private GoogleMap googleMap;

    private Callback callback;

    private LocationModel currentLocation;
    private Marker currentLocationMarker;

    private int mapZoom;

    private boolean isResolvingError;

    private Snackbar snackbar;

    public static StationMapFragment newInstance() {
        final Bundle args = new Bundle();

        final StationMapFragment fragment = new StationMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            callback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement Callback");
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mapZoom = getResources().getInteger(R.integer.map_zoom);

        //restore previous location
        if (savedInstanceState != null) {
            final double latitude = savedInstanceState.getDouble(BUNDLE_CURRENT_LATITUDE, INVALID_COORDINATE);
            final double longitude = savedInstanceState.getDouble(BUNDLE_CURRENT_LONGITUDE, INVALID_COORDINATE);

            if (latitude != INVALID_COORDINATE && longitude != INVALID_COORDINATE) {
                currentLocation = LocationModel.create(latitude, longitude);
            }

            isResolvingError = savedInstanceState.getBoolean(BUNDLE_RESOLVING_ERROR, false);
        }

        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_station_map, container, false);

        unbinder = ButterKnife.bind(this, view);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.onAttachView(this);

        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        mapView.onSaveInstanceState(outState);

        outState.putBoolean(BUNDLE_RESOLVING_ERROR, isResolvingError);

        if (currentLocation != null) {
            outState.putDouble(BUNDLE_CURRENT_LATITUDE, currentLocation.latitude());
            outState.putDouble(BUNDLE_CURRENT_LONGITUDE, currentLocation.longitude());
        }
    }

    @Override
    public void onPause() {
        mapView.onPause();

        super.onPause();
    }

    @Override
    public void onStop() {
        mapView.onStop();

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mapView.onDestroy();

        unbinder.unbind();

        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        callback = null;

        super.onDetach();
    }

    @Override
    public void onLowMemory() {
        mapView.onLowMemory();

        super.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.requestCurrentLocation();
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showLocationPermissionRationale();
                } else {
                    snackbar = Snackbar.make(getView(), getString(R.string.location_permission_denied), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }
        }
    }

    @Override
    protected void createComponent() {
        component = ((ApplicationComponent) baseCallback.getComponent())
                .plus(new StationModule());
    }

    @Override
    protected void restorePresenter() {
        final PresenterManager presenterManager = baseCallback.getPresenterManager();

        presenter = (StationMapPresenter) presenterManager.get(uuid);
        if (presenter == null) {
            presenter = component.stationMapPresenter();
            presenterManager.put(uuid, presenter);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setInfoWindowAdapter(new StationWindowAdapter(LayoutInflater.from(getContext())));
        this.googleMap.setOnInfoWindowClickListener(this);

        //add current location if available
        if (currentLocation != null) {
            displayCurrentLocation(currentLocation, false);
        }

        callback.onMapReady();

        presenter.loadStationList();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        final String tag = (String) marker.getTag();
        //check if the marker is not the current location marker
        if (tag != null) {
            navigator.navigateToPictureList(getContext(), tag, marker.getTitle());
        }
    }

    @Override
    public void displayStationList(final List<StationModel> stations) {
        googleMap.clear();

        if (currentLocation != null) {
            displayCurrentLocation(currentLocation, false);
        }

        final BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_place_red_48dp);

        for (int i = 0, size = stations.size(); i < size; i++) {
            final StationModel station = stations.get(i);
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(station.latitude(), station.longitude()))
                    .title(station.streetName())
                    .icon(bitmapDescriptor))
                    .setTag(station.id());
        }
    }

    @Override
    public void showLoadStationListError() {
        Snackbar.make(getView(), R.string.error_occured, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, v -> presenter.loadStationList())
                .show();
    }

    @Override
    public void displayCurrentLocation(LocationModel location, boolean moveCamera) {
        currentLocation = location;

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        currentLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.latitude(), location.longitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_person_pin_circle_blue_48dp)));

        if (moveCamera) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude(), location.longitude()), mapZoom));
        }
    }

    @Override
    public void handleConnectionFailed(final GoogleApiClientConnectionFailedException exception) {
        if (isResolvingError) {
            // Already attempting to resolve an error
            return;
        }

        final ConnectionResult result = exception.getConnectionResult();
        if (result.hasResolution()) {
            try {
                isResolvingError = true;
                result.startResolutionForResult(getActivity(), RESOLVE_ERROR_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                isResolvingError = false;
                showRequestCurrentLocationError();
            }
        } else {
            showErrorDialog(result.getErrorCode());
            isResolvingError = true;
        }
    }

    @Override
    public void handleSettingsInvalid(final LocationSettingsException exception) {
        try {
            exception.getStatus().startResolutionForResult(getActivity(), CHECK_SETTINGS_REQUEST_CODE);
        } catch (IntentSender.SendIntentException ignored) {
        }
    }

    @Override
    public void showRequestCurrentLocationError() {
        snackbar = Snackbar.make(getView(), R.string.request_current_location_failed, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry, v -> requestCurrentLocation());
        snackbar.show();
    }

    public void requestCurrentLocation() {
        if (snackbar != null) {
            snackbar.dismiss();
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showLocationPermissionRationale();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }

            return;
        }

        presenter.requestCurrentLocation();
    }

    public void onErrorResolved(final int resultCode) {
        isResolvingError = false;

        if (resultCode == Activity.RESULT_OK) {
            requestCurrentLocation();
        }
    }

    public void onSettingsChanged(final int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            requestCurrentLocation();
        }
    }

    public void onDialogDismissed() {
        isResolvingError = false;
    }

    private void showLocationPermissionRationale() {
        snackbar = Snackbar.make(getView(), R.string.location_permission_rationale, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_allow, v -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE));
        snackbar.show();
    }

    private void showErrorDialog(int errorCode) {
        final ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(errorCode);
        errorDialogFragment.setTargetFragment(this, 0);
        errorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.TAG_ERROR_DIALOG_FRAGMENT);
    }

    public interface Callback {
        void onMapReady();
    }
}
