package fr.simonlebras.tagthebus.data.pictures;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.pictures.exceptions.AddPictureException;
import fr.simonlebras.tagthebus.data.pictures.exceptions.LoadPictureListException;
import fr.simonlebras.tagthebus.data.pictures.exceptions.RemovePictureException;
import fr.simonlebras.tagthebus.data.pictures.provider.PictureContract;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.PictureModel;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

@PerFragment
public class PictureRepositoryImpl implements PictureRepository {
    private final Context context;

    private final PictureMapper mapper;

    @Inject
    PictureRepositoryImpl(Context context, PictureMapper mapper) {
        this.context = context;

        this.mapper = mapper;
    }

    @Override
    public Observable<List<PictureModel>> loadPictureList(final String stationId) {
        return Observable
                .<Pair<Cursor, Boolean>>create(subscriber -> {
                    final Cursor cursor = queryPictures(stationId);

                    if (cursor != null) {
                        final ContentObserver contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                            @Override
                            public boolean deliverSelfNotifications() {
                                return true;
                            }

                            @Override
                            public void onChange(final boolean selfChange) {
                                final Cursor cursor = queryPictures(stationId);

                                if (!subscriber.isDisposed() && cursor != null) {
                                    subscriber.onNext(new Pair<>(cursor, true));
                                }
                            }
                        };

                        cursor.registerContentObserver(contentObserver);

                        if (!subscriber.isDisposed()) {
                            subscriber.onNext(new Pair<>(cursor, false));
                        }

                        subscriber.setCancellable(() -> {
                            cursor.unregisterContentObserver(contentObserver);

                            cursor.close();
                        });
                        return;
                    }

                    if (!subscriber.isDisposed()) {
                        subscriber.onError(new LoadPictureListException());
                    }
                })
                .observeOn(Schedulers.computation())
                .map(pair -> {
                    final List<PictureModel> pictures = mapper.transform(pair.first);

                    //prevents the first cursor from being closed
                    if (pair.second) {
                        pair.first.close();
                    }

                    return pictures;
                });
    }

    @Override
    public Completable addPicture(final PictureModel picture) {
        return Completable.create(subscriber -> {
            final ContentValues values = new ContentValues();

            values.put(PictureContract.PictureEntry.COLUMN_NAME_TITLE, picture.title());
            values.put(PictureContract.PictureEntry.COLUMN_NAME_CREATED_AT, picture.createdAt());
            values.put(PictureContract.PictureEntry.COLUMN_NAME_STATION_ID, picture.stationId());
            values.put(PictureContract.PictureEntry.COLUMN_NAME_IMAGE_PATH, picture.imagePath());

            try {
                context.getContentResolver().insert(PictureContract.PictureEntry.CONTENT_URI, values);
                if (!subscriber.isDisposed()) {
                    subscriber.onComplete();
                }
            } catch (Exception e) {
                if (!subscriber.isDisposed()) {
                    subscriber.onError(new AddPictureException());
                }
            }
        });
    }

    @Override
    public Completable removePictures(final List<PictureModel> pictures) {
        return Completable.create(subscriber -> {
            try {
                final int size = pictures.size();
                final String[] pictureIds = new String[size];
                for (int i = 0; i < size; i++) {
                    pictureIds[i] = String.valueOf(pictures.get(i).id());

                    final File image = new File(pictures.get(i).imagePath());
                    if (image.exists()) {
                        final boolean deleted = image.delete();

                        if (!deleted && !subscriber.isDisposed()) {
                            subscriber.onError(new RemovePictureException());
                            return;
                        }
                    }
                }

                context.getContentResolver().delete(
                        PictureContract.PictureEntry.CONTENT_URI,
                        null,
                        pictureIds
                );

                if (!subscriber.isDisposed()) {
                    subscriber.onComplete();
                }
            } catch (Exception e) {
                if (!subscriber.isDisposed()) {
                    subscriber.onError(new AddPictureException());
                }
            }
        });
    }

    private Cursor queryPictures(String stationId) {
        final String[] projections = new String[]{
                PictureContract.PictureEntry._ID,
                PictureContract.PictureEntry.COLUMN_NAME_TITLE,
                PictureContract.PictureEntry.COLUMN_NAME_CREATED_AT,
                PictureContract.PictureEntry.COLUMN_NAME_STATION_ID,
                PictureContract.PictureEntry.COLUMN_NAME_IMAGE_PATH
        };

        return context.getContentResolver().query(
                PictureContract.PictureEntry.CONTENT_URI,
                projections,
                PictureContract.PictureEntry.COLUMN_NAME_STATION_ID + " = ?",
                new String[]{stationId},
                PictureContract.PictureEntry.COLUMN_NAME_CREATED_AT + " DESC"
        );
    }
}
