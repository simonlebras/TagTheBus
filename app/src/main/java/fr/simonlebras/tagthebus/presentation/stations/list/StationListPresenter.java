package fr.simonlebras.tagthebus.presentation.stations.list;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.stations.list.StationRepository;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.StationModel;
import fr.simonlebras.tagthebus.presentation.base.BasePresenter;
import fr.simonlebras.tagthebus.presentation.base.BaseView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

@PerFragment
public class StationListPresenter extends BasePresenter<StationListPresenter.View> {
    private static final long TIMEOUT = 10;//in seconds

    private final StationRepository stationRepository;

    private Disposable loadStationListDisposable;

    @Inject
    StationListPresenter(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    public void onDetachView() {
        if (loadStationListDisposable != null && !loadStationListDisposable.isDisposed()) {
            loadStationListDisposable.dispose();
        }

        super.onDetachView();
    }

    @Override
    public void onDestroy() {
        stationRepository.clear();

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

    public interface View extends BaseView {
        void displayStationList(List<StationModel> stations);

        void showLoadStationListError();
    }
}
