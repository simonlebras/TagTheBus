package fr.simonlebras.tagthebus.presentation.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import fr.simonlebras.tagthebus.injection.components.BaseComponent;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment<P extends BasePresenter<V>, V extends BaseView> extends Fragment {
    private static final String BUNDLE_UUID = "BUNDLE_UUID";

    protected BaseCallback baseCallback;

    protected P presenter;
    protected UUID uuid;

    protected CompositeDisposable compositeDisposable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            baseCallback = (BaseCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BaseCallback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            uuid = (UUID) savedInstanceState.getSerializable(BUNDLE_UUID);
        }

        if (uuid == null) {
            uuid = UUID.randomUUID();
        }

        createComponent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        compositeDisposable = new CompositeDisposable();

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        restorePresenter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(BUNDLE_UUID, uuid);
    }

    @Override
    public void onDestroyView() {
        compositeDisposable.clear();

        presenter.onDetachView();

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (!getActivity().isChangingConfigurations()) {
            presenter.onDestroy();
            baseCallback.getPresenterManager().remove(uuid);
        }

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        baseCallback = null;

        super.onDetach();
    }

    protected abstract void createComponent();

    protected abstract void restorePresenter();

    public interface BaseCallback {
        PresenterManager getPresenterManager();

        BaseComponent getComponent();
    }
}
