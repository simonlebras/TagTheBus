package fr.simonlebras.tagthebus.presentation.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import fr.simonlebras.tagthebus.TagTheBusApplication;
import fr.simonlebras.tagthebus.injection.components.ApplicationComponent;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends AppCompatActivity implements BaseFragment.BaseCallback {
    protected CompositeDisposable compositeDisposable;

    private PresenterManager presenterManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        compositeDisposable = new CompositeDisposable();

        presenterManager = getLastCustomNonConfigurationInstance();
        if (presenterManager == null) {
            presenterManager = new PresenterManager();
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();

        super.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenterManager;
    }

    @Override
    public PresenterManager getLastCustomNonConfigurationInstance() {
        return (PresenterManager) super.getLastCustomNonConfigurationInstance();
    }

    @Override
    public PresenterManager getPresenterManager() {
        return presenterManager;
    }

    @Override
    public ApplicationComponent getComponent() {
        return ((TagTheBusApplication) getApplication()).getApplicationComponent();
    }
}
