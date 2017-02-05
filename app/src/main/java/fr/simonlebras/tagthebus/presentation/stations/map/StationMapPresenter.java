package fr.simonlebras.tagthebus.presentation.stations.map;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.stations.list.StationRepository;
import fr.simonlebras.tagthebus.data.stations.map.LocationRepository;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.GoogleApiClientConnectionFailedException;
import fr.simonlebras.tagthebus.data.stations.map.exceptions.LocationSettingsException;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.LocationModel;
import fr.simonlebras.tagthebus.models.StationModel;
import fr.simonlebras.tagthebus.presentation.base.BasePresenter;
import fr.simonlebras.tagthebus.presentation.base.BaseView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

@PerFragment
public class StationMapPresenter extends BasePresenter<StationMapPresenter.View> {
    private static final long TIMEOUT = 10;//in seconds

    private final StationRepository stationRepository;
    private final LocationRepository locationRepository;

    private Disposable loadStationListDisposable;
    private Disposable requestLocationDisposable;

    @Inject
    StationMapPresenter(StationRepository stationRepository, LocationRepository locationRepository) {
        this.stationRepository = stationRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public void onDetachView() {
        if (loadStationListDisposable != null && !loadStationListDisposable.isDisposed()) {
            loadStationListDisposable.dispose();
        }

        if (requestLocationDisposable != null && !requestLocationDisposable.isDisposed()) {
            requestLocationDisposable.dispose();
        }

        super.onDetachView();
    }

    @Override
    public void onDestroy() {
        stationRepository.clear();
        locationRepository.clear();

        super.onDestroy();
    }

    void loadStationList() {
        if (loadStationListDisposable != null && !loadStationListDisposable.isDisposed()) {
            loadStationListDisposable.dispose();
        }

        loadStationListDisposable = stationRepository.loadStationList()
                .subscribeOn(Schedulers.io())
                .timeout(TIMEOUT, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<StationModel>>() {
                    @Override
                    public void onNext(final List<StationModel> stations) {
                        if (isViewAttached()) {
                            if (stations.isEmpty()) {
                                view.showLoadStationListError();
                                return;
                            }

                            view.displayStationList(stations);
                        }
                    }

                    @Override
                    public void onError(final Throwable t) {
                        if (isViewAttached()) {
                            view.showLoadStationListError();
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    void requestCurrentLocation() {
        if (requestLocationDisposable != null && !requestLocationDisposable.isDisposed()) {
            requestLocationDisposable.dispose();
        }

        requestLocationDisposable = locationRepository.getCurrentLocation()
                .subscribeOn(Schedulers.io())
                .take(1)
                .timeout(TIMEOUT, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<LocationModel>() {
                    @Override
                    public void onNext(final LocationModel location) {
                        if (isViewAttached()) {
                            view.displayCurrentLocation(location, true);
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (isViewAttached()) {
                            if (e instanceof GoogleApiClientConnectionFailedException) {
                                view.handleConnectionFailed((GoogleApiClientConnectionFailedException) e);
                            } else if (e instanceof LocationSettingsException) {
                                view.handleSettingsInvalid((LocationSettingsException) e);
                            } else {
                                view.showRequestCurrentLocationError();
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public interface View extends BaseView {
        void displayStationList(List<StationModel> stations);

        void showLoadStationListError();

        void displayCurrentLocation(LocationModel location, boolean moveCamera);

        void handleConnectionFailed(GoogleApiClientConnectionFailedException exception);

        void handleSettingsInvalid(LocationSettingsException exception);

        void showRequestCurrentLocationError();
    }
}
