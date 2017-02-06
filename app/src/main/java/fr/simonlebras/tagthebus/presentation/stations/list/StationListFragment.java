package fr.simonlebras.tagthebus.presentation.stations.list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.injection.components.ApplicationComponent;
import fr.simonlebras.tagthebus.injection.components.StationComponent;
import fr.simonlebras.tagthebus.injection.modules.StationModule;
import fr.simonlebras.tagthebus.models.StationModel;
import fr.simonlebras.tagthebus.presentation.base.BaseFragment;
import fr.simonlebras.tagthebus.presentation.base.PresenterManager;
import fr.simonlebras.tagthebus.presentation.navigator.Navigator;

public class StationListFragment extends BaseFragment<StationListPresenter, StationListPresenter.View> implements StationListPresenter.View {
    @Inject
    Navigator navigator;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.empty_view)
    LinearLayout emptyView;

    @BindView(R.id.button_retry)
    Button buttonRetry;

    private StationComponent component;

    private Unbinder unbinder;

    private StationListAdapter adapter;

    private Snackbar snackbar;

    public static StationListFragment newInstance() {
        final Bundle args = new Bundle();

        final StationListFragment fragment = new StationListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_station_list, container, false);

        unbinder = ButterKnife.bind(this, view);

        adapter = new StationListAdapter(this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        recyclerView.setHasFixedSize(true);

        buttonRetry.setOnClickListener(v -> {
            if (snackbar != null) {
                snackbar.dismiss();
            }

            showProgressBar();

            presenter.loadStationList();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.onAttachView(this);

        presenter.loadStationList();
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();

        super.onDestroyView();
    }

    @Override
    protected void createComponent() {
        component = ((ApplicationComponent) baseCallback.getComponent())
                .plus(new StationModule());
    }

    @Override
    protected void restorePresenter() {
        final PresenterManager presenterManager = baseCallback.getPresenterManager();

        presenter = (StationListPresenter) presenterManager.get(uuid);
        if (presenter == null) {
            presenter = component.stationListPresenter();
            presenterManager.put(uuid, presenter);
        }
    }

    @Override
    public void displayStationList(final List<StationModel> stations) {
        adapter.addAll(stations);
        adapter.notifyDataSetChanged();

        showRecyclerView();
    }

    @Override
    public void showLoadStationListError() {
        showEmptyView();

        showRetryAction();
    }

    public void onStationSelected(final String stationId) {
        navigator.navigateToPictureList(getContext(), stationId);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
    }

    private void showRecyclerView() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showEmptyView() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    private void showRetryAction() {
        snackbar = Snackbar.make(getView(), R.string.load_station_list_failed, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_retry, v -> {
                    showProgressBar();

                    presenter.loadStationList();
                });

        snackbar.show();
    }
}
