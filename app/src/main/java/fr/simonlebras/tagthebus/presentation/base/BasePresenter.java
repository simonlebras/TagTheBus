package fr.simonlebras.tagthebus.presentation.base;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BasePresenter<V extends BaseView> {
    protected V view;

    protected CompositeDisposable compositeDisposable;

    public void onAttachView(V view) {
        compositeDisposable = new CompositeDisposable();

        this.view = view;
    }

    public void onDetachView() {
        compositeDisposable.clear();

        this.view = null;
    }

    public void onDestroy() {
    }

    protected boolean isViewAttached() {
        return view != null;
    }
}
