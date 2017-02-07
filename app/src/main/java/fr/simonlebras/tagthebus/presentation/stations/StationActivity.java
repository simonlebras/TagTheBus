package fr.simonlebras.tagthebus.presentation.stations;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.presentation.base.BaseActivity;
import fr.simonlebras.tagthebus.presentation.stations.list.StationListFragment;
import fr.simonlebras.tagthebus.presentation.stations.map.StationMapFragment;

public class StationActivity extends BaseActivity implements StationMapFragment.Callback {
    private static final String TAG_STATION_FRAGMENT = "TAG_STATION_FRAGMENT";

    private static final String BUNDLE_SELECTED_ITEM_ID = "BUNDLE_SELECTED_ITEM_ID";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private boolean mapReady;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.bottom_navigation_view)
    BottomNavigationView bottomNavigationView;

    private int selectedItemId = R.id.action_map;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //reset to app theme after launch
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_station);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        //restore previous state
        if (savedInstanceState != null) {
            selectedItemId = savedInstanceState.getInt(BUNDLE_SELECTED_ITEM_ID, R.id.action_map);

            bottomNavigationView.getMenu().findItem(selectedItemId).setChecked(true);
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, StationMapFragment.newInstance(), TAG_STATION_FRAGMENT)
                    .commit();
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() != selectedItemId) {
                selectedItemId = item.getItemId();

                mapReady = false;

                invalidateOptionsMenu();

                switch (selectedItemId) {
                    case R.id.action_map:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, StationMapFragment.newInstance(), TAG_STATION_FRAGMENT)
                                .commit();
                        break;
                    case R.id.action_list:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, StationListFragment.newInstance(), TAG_STATION_FRAGMENT)
                                .commit();
                        break;
                }
            }

            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BUNDLE_SELECTED_ITEM_ID, selectedItemId);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_station, menu);

        if (selectedItemId != R.id.action_map || !mapReady) {
            menu.findItem(R.id.action_location).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_location:
                final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_STATION_FRAGMENT);
                if (fragment instanceof StationMapFragment) {
                    ((StationMapFragment) fragment).requestCurrentLocation();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == StationMapFragment.RESOLVE_ERROR_REQUEST_CODE) {
            final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_STATION_FRAGMENT);
            if (fragment instanceof StationMapFragment) {
                ((StationMapFragment) fragment).onErrorResolved(resultCode);
            }
        } else if (requestCode == StationMapFragment.CHECK_SETTINGS_REQUEST_CODE) {
            final Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_STATION_FRAGMENT);
            if (fragment instanceof StationMapFragment) {
                ((StationMapFragment) fragment).onSettingsChanged(resultCode);
            }
        }
    }

    @Override
    public void onMapReady() {
        mapReady = true;

        invalidateOptionsMenu();
    }
}
