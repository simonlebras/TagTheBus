package fr.simonlebras.tagthebus.presentation.pictures;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.TagTheBusApplication;
import fr.simonlebras.tagthebus.models.PictureModel;
import fr.simonlebras.tagthebus.presentation.navigator.Navigator;

public class PictureCreationActivity extends AppCompatActivity {
    public static final int TAKE_PICTURE_REQUEST_CODE = 100;

    public static final String EXTRA_STATION_ID = "EXTRA_STATION_ID";

    private static final String BUNDLE_PICTURE_TAKEN = "BUNDLE_PICTURE_TAKEN";
    private static final String BUNDLE_CURRENT_IMAGE = "BUNDLE_CURRENT_IMAGE";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject
    Navigator navigator;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinator;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.text_layout_picture_title)
    TextInputLayout textInputLayout;

    @BindView(R.id.text_picture_title)
    EditText textTitle;

    @BindView(R.id.image_picture_thumbnail)
    ImageView imageThumbnail;

    @BindView(R.id.button_take_picture)
    FloatingActionButton buttonTakePicture;

    private String stationId;

    private File currentImage;
    private boolean pictureTaken;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stationId = getIntent().getStringExtra(EXTRA_STATION_ID);

        ((TagTheBusApplication) getApplication()).getApplicationComponent()
                .inject(this);

        setContentView(R.layout.activity_picture_creation);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            pictureTaken = savedInstanceState.getBoolean(BUNDLE_PICTURE_TAKEN);

            final String imagePath = savedInstanceState.getString(BUNDLE_CURRENT_IMAGE);
            if (imagePath != null) {
                currentImage = new File(imagePath);

                if (pictureTaken) {
                    displayCurrentImage();
                }
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textInputLayout.setHint(getString(R.string.hint_title));

        buttonTakePicture.setOnClickListener(v -> takePicture());
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(BUNDLE_PICTURE_TAKEN, pictureTaken);

        if (currentImage != null) {
            outState.putString(BUNDLE_CURRENT_IMAGE, currentImage.getAbsolutePath());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_picture_creation, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_validate:
                final String pictureTitle = textTitle.getText().toString();
                if (pictureTitle.isEmpty()) {
                    Snackbar.make(coordinator, R.string.title_required, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }

                if (!pictureTaken) {
                    Snackbar.make(coordinator, R.string.picture_required, Snackbar.LENGTH_LONG)
                            .show();
                    return true;
                }

                final Intent intent = new Intent();
                intent.putExtra(PictureListFragment.EXTRA_NEW_PICTURE, PictureModel.create(-1, pictureTitle, System.currentTimeMillis(), stationId, currentImage.getAbsolutePath()));

                setResult(Activity.RESULT_OK, intent);
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                pictureTaken = true;

                displayCurrentImage();
            }
        }
    }

    private void takePicture() {
        try {
            createImageFile();
        } catch (IOException e) {
            Snackbar.make(coordinator, R.string.error_occured, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry, v -> takePicture())
                    .show();
        }

        navigator.takePicture(this, currentImage);
    }

    private void createImageFile() throws IOException {
        if (currentImage != null) {
            return;
        }

        currentImage = File.createTempFile(UUID.randomUUID().toString(), ".jpg", getFilesDir());
    }

    private void displayCurrentImage() {
        Glide.with(this)
                .load(Uri.fromFile(new File(currentImage.getAbsolutePath())))
                .asBitmap()
                .placeholder(ContextCompat.getDrawable(this, R.drawable.ic_photo_grey_300dp))
                .error(ContextCompat.getDrawable(this, R.drawable.ic_photo_grey_300dp))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageThumbnail);
    }
}
