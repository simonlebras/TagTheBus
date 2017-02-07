package fr.simonlebras.tagthebus.presentation.pictures;

import java.util.List;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.pictures.PictureRepository;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.PictureModel;
import fr.simonlebras.tagthebus.presentation.base.BasePresenter;
import fr.simonlebras.tagthebus.presentation.base.BaseView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

@PerFragment
public class PictureListPresenter extends BasePresenter<PictureListPresenter.View> {
    private final PictureRepository pictureRepository;

    private Disposable loadPictureListDisposable;

    @Inject
    PictureListPresenter(PictureRepository pictureRepository) {
        this.pictureRepository = pictureRepository;
    }

    @Override
    public void onDetachView() {
        if (loadPictureListDisposable != null && !loadPictureListDisposable.isDisposed()) {
            loadPictureListDisposable.dispose();
        }

        super.onDetachView();
    }

    void loadPictureList(String stationId) {
        if (loadPictureListDisposable != null && !loadPictureListDisposable.isDisposed()) {
            loadPictureListDisposable.dispose();
        }

        loadPictureListDisposable = pictureRepository.loadPictureList(stationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<PictureModel>>() {
                    @Override
                    public void onNext(final List<PictureModel> pictures) {
                        if (isViewAttached()) {
                            if (pictures.isEmpty()) {
                                view.showLoadPictureListError(false);

                                return;
                            }

                            view.displayPictureList(pictures);
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (isViewAttached()) {
                            view.showLoadPictureListError(true);
                        }
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    void addPicture(PictureModel picture) {
        compositeDisposable.add(pictureRepository.addPicture(picture)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (isViewAttached()) {
                            view.showAddError(picture);
                        }
                    }
                }));
    }

    void removePictures(List<PictureModel> pictures) {
        compositeDisposable.add(pictureRepository.removePictures(pictures)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (isViewAttached()) {
                            view.showRemoveError(pictures);
                        }
                    }
                }));
    }

    interface View extends BaseView {
        void displayPictureList(List<PictureModel> pictures);

        void showLoadPictureListError(boolean showRetry);

        void showAddError(PictureModel picture);

        void showRemoveError(List<PictureModel> pictures);
    }
}
