package fr.simonlebras.tagthebus.presentation.pictures;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;

import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.presentation.base.BaseActivity;

public class PictureListActivity extends BaseActivity {
    public static final String EXTRA_STATION_ID = "EXTRA_STATION_ID";
    public static final String EXTRA_STATION_NAME = "EXTRA_STATION_NAME";

    private static final String TAG_STATION_FRAGMENT = "TAG_STATION_FRAGMENT";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_picture_list);

        if (savedInstanceState == null) {
            final String stationId = getIntent().getStringExtra(EXTRA_STATION_ID);
            final String stationName = getIntent().getStringExtra(EXTRA_STATION_NAME);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, PictureListFragment.newInstance(stationId, stationName), TAG_STATION_FRAGMENT)
                    .commit();
        }
    }
}
