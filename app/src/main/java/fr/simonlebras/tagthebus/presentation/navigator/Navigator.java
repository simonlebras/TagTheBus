package fr.simonlebras.tagthebus.presentation.navigator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.simonlebras.tagthebus.BuildConfig;
import fr.simonlebras.tagthebus.models.PictureModel;
import fr.simonlebras.tagthebus.presentation.pictures.PictureCreationActivity;
import fr.simonlebras.tagthebus.presentation.pictures.PictureFullscreenActivity;
import fr.simonlebras.tagthebus.presentation.pictures.PictureListActivity;
import fr.simonlebras.tagthebus.presentation.pictures.PictureListFragment;

@Singleton
public class Navigator {
    @Inject
    Navigator() {
    }

    public void navigateToPictureList(Context context, String stationId, String stationName) {
        final Intent intent = new Intent(context, PictureListActivity.class)
                .putExtra(PictureListActivity.EXTRA_STATION_ID, stationId)
                .putExtra(PictureListActivity.EXTRA_STATION_NAME, stationName);

        context.startActivity(intent);
    }

    public void navigateToPictureCreation(final Fragment fragment, String stationId) {
        final Intent intent = new Intent(fragment.getContext(), PictureCreationActivity.class)
                .putExtra(PictureCreationActivity.EXTRA_STATION_ID, stationId);

        fragment.startActivityForResult(intent, PictureListFragment.NEW_PICTURE_REQUEST_CODE);
    }

    public void takePicture(final Activity activity, final File pictureFile) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            final Uri photoURI = FileProvider.getUriForFile(activity, BuildConfig.FILES_AUTHORITY, pictureFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            activity.startActivityForResult(intent, PictureCreationActivity.TAKE_PICTURE_REQUEST_CODE);
        }
    }

    public void navigateToPictureFullscreen(final Context context, final PictureModel picture, final String stationName) {
        final Intent intent = new Intent(context, PictureFullscreenActivity.class)
                .putExtra(PictureFullscreenActivity.EXTRA_PICTURE, picture)
                .putExtra(PictureFullscreenActivity.EXTRA_STATION_NAME, stationName);

        context.startActivity(intent);
    }

    public void sharePicture(final Activity activity, final PictureModel picture, final String stationName) {
        final File imageFile = new File(picture.imagePath());
        final Uri imageUri = FileProvider.getUriForFile(activity, BuildConfig.FILES_AUTHORITY, imageFile);

        final DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(activity);
        final String date = dateFormat.format(new Date(picture.createdAt()));

        final Intent intent = ShareCompat.IntentBuilder.from(activity)
                .setType("image/jpg")
                .setSubject(picture.title())
                .setText(stationName + " - " + date)
                .setStream(imageUri)
                .getIntent();

        intent.setData(imageUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }
}
