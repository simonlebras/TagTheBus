package fr.simonlebras.tagthebus.presentation.pictures;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.TagTheBusApplication;
import fr.simonlebras.tagthebus.models.PictureModel;
import fr.simonlebras.tagthebus.presentation.navigator.Navigator;

public class PictureFullscreenActivity extends AppCompatActivity {
    public static final String EXTRA_PICTURE = "EXTRA_PICTURE";
    public static final String EXTRA_STATION_NAME = "EXTRA_STATION_NAME";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject
    Navigator navigator;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.image_picture_thumbnail)
    ImageView thumbnail;

    private PictureModel picture;

    private String stationName;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        picture = getIntent().getParcelableExtra(EXTRA_PICTURE);
        stationName = getIntent().getStringExtra(EXTRA_STATION_NAME);

        ((TagTheBusApplication) getApplication()).getApplicationComponent()
                .inject(this);

        setContentView(R.layout.activity_picture_fullscreen);
        ButterKnife.bind(this);

        toolbar.setTitle(picture.title());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadPicture();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_picture_fullscreen, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                navigator.sharePicture(this, picture, stationName);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadPicture() {
        Glide.with(this)
                .load(Uri.fromFile(new File(picture.imagePath())))
                .asBitmap()
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_photo_grey_300dp))
                .error(ContextCompat.getDrawable(this, R.drawable.ic_photo_grey_300dp))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(thumbnail);
    }
}
