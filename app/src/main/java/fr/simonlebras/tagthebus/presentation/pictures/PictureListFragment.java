package fr.simonlebras.tagthebus.presentation.pictures;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.injection.components.ApplicationComponent;
import fr.simonlebras.tagthebus.injection.components.PictureComponent;
import fr.simonlebras.tagthebus.injection.modules.PictureModule;
import fr.simonlebras.tagthebus.models.PictureModel;
import fr.simonlebras.tagthebus.presentation.base.BaseFragment;
import fr.simonlebras.tagthebus.presentation.base.PresenterManager;
import fr.simonlebras.tagthebus.presentation.navigator.Navigator;

public class PictureListFragment extends BaseFragment<PictureListPresenter, PictureListPresenter.View> implements PictureListPresenter.View, ActionMode.Callback {
    public static final String ARGUMENT_STATION_ID = "ARGUMENT_STATION_ID";
    public static final String ARGUMENT_STATION_NAME = "ARGUMENT_STATION_NAME";

    public static final int NEW_PICTURE_REQUEST_CODE = 100;

    public static final String EXTRA_NEW_PICTURE = "EXTRA_NEW_PICTURE";

    @Inject
    Navigator navigator;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.progress_bar)
    FrameLayout progressBar;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.empty_view)
    FrameLayout emptyView;

    @BindView(R.id.button_retry)
    Button buttonRetry;

    @BindView(R.id.button_add_picture)
    FloatingActionButton addPicture;

    private PictureComponent component;

    private Unbinder unbinder;

    private String stationId;
    private String stationName;

    private PictureListAdapter adapter;

    private Snackbar snackbar;

    private ActionMode actionMode;

    public static PictureListFragment newInstance(String stationId, String stationName) {
        final Bundle args = new Bundle();
        args.putString(ARGUMENT_STATION_ID, stationId);
        args.putString(ARGUMENT_STATION_NAME, stationName);

        final PictureListFragment fragment = new PictureListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        stationId = arguments.getString(ARGUMENT_STATION_ID);
        stationName = arguments.getString(ARGUMENT_STATION_NAME);

        component.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_picture_list, container, false);

        unbinder = ButterKnife.bind(this, view);

        toolbar.setTitle(stationName);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new PictureListAdapter(this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final float width = getResources().getDimensionPixelSize(R.dimen.list_divider_width);
        final DividerItemDecoration decoration = new DividerItemDecoration(ContextCompat.getColor(getContext(), R.color.colorDivider), width);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setHasFixedSize(true);

        buttonRetry.setOnClickListener(v -> {
            if (snackbar != null) {
                snackbar.dismiss();
            }

            showProgressBar();

            presenter.loadPictureList(stationId);
        });

        addPicture.setOnClickListener(v -> {
            navigator.navigateToPictureCreation(this, stationId);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        presenter.onAttachView(this);

        presenter.loadPictureList(stationId);
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();

        super.onDestroyView();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == NEW_PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final PictureModel picture = data.getParcelableExtra(PictureListFragment.EXTRA_NEW_PICTURE);

                presenter.addPicture(picture);
            }
        }
    }

    @Override
    protected void createComponent() {
        component = ((ApplicationComponent) baseCallback.getComponent())
                .plus(new PictureModule());
    }

    @Override
    protected void restorePresenter() {
        final PresenterManager presenterManager = baseCallback.getPresenterManager();

        presenter = (PictureListPresenter) presenterManager.get(uuid);
        if (presenter == null) {
            presenter = component.pictureListPresenter();
            presenterManager.put(uuid, presenter);
        }
    }

    @Override
    public void displayPictureList(final List<PictureModel> pictures) {
        showRecyclerView();

        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new PictureListDiffCallback(adapter.getPictures(), pictures));

        adapter.addAll(pictures);
        result.dispatchUpdatesTo(adapter);
    }

    @Override
    public void showLoadPictureListError(final boolean showRetry) {
        showEmptyView(showRetry);

        adapter.removeAll();
        adapter.notifyDataSetChanged();

        if (showRetry) {
            snackbar = Snackbar.make(getView(), R.string.error_occured, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry, v -> {
                        showProgressBar();

                        presenter.loadPictureList(stationId);
                    });
            snackbar.show();
        }
    }

    @Override
    public void showAddError(final PictureModel picture) {
        snackbar = Snackbar.make(getView(), R.string.error_occured, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, v -> presenter.addPicture(picture));
        snackbar.show();
    }

    @Override
    public void showRemoveError(final List<PictureModel> pictures) {
        snackbar = Snackbar.make(getView(), R.string.error_occured, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, v -> presenter.removePictures(pictures));
        snackbar.show();
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mode.getMenuInflater().inflate(R.menu.fragment_picture_list, menu);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                final List<PictureModel> pictures = adapter.getSelectedItems();
                if (!pictures.isEmpty()) {
                    presenter.removePictures(adapter.getSelectedItems());
                }

                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        adapter.clearSelection();

        actionMode = null;
    }

    public void onPictureClicked(final int position) {
        if (actionMode == null) {
            navigator.navigateToPictureFullscreen(getContext(), adapter.getPicture(position), stationName);
            return;
        }

        adapter.toggleSelection(position);
    }

    public void onPictureLongClicked(final int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        }

        adapter.toggleSelection(position);
    }

    public void setActionModeTitle() {
        actionMode.setTitle(getString(R.string.title_mode, String.valueOf(adapter.getSelectedItemCount())));
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        addPicture.setVisibility(View.GONE);
    }

    private void showRecyclerView() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        addPicture.setVisibility(View.VISIBLE);
    }

    private void showEmptyView(boolean showRetry) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        addPicture.setVisibility(View.VISIBLE);
        buttonRetry.setVisibility(showRetry ? View.VISIBLE : View.GONE);
    }
}
