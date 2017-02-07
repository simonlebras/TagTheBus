package fr.simonlebras.tagthebus.data.pictures.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class PictureProvider extends ContentProvider {
    private static final int PICTURE = 100;
    private static final int PICTURE_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(PictureContract.CONTENT_AUTHORITY, PictureContract.PATH_PICTURE, PICTURE);
        uriMatcher.addURI(PictureContract.CONTENT_AUTHORITY, PictureContract.PATH_PICTURE + "/#", PICTURE_ID);
    }

    private PictureDbHelper pictureDbHelper;

    @Override
    public boolean onCreate() {
        pictureDbHelper = new PictureDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public String getType(final Uri uri) {
        switch (uriMatcher.match(uri)) {
            case PICTURE:
                return PictureContract.PictureEntry.CONTENT_TYPE;
            case PICTURE_ID:
                return PictureContract.PictureEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        final SQLiteDatabase db = pictureDbHelper.getWritableDatabase();
        Cursor result;
        switch (uriMatcher.match(uri)) {
            case PICTURE:
                result = db.query(
                        PictureContract.PictureEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PICTURE_ID:
                long _id = ContentUris.parseId(uri);
                result = db.query(
                        PictureContract.PictureEntry.TABLE_NAME,
                        projection,
                        PictureContract.PictureEntry._ID + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null) {
            result.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return result;
    }

    @Nullable
    @Override
    public Uri insert(final Uri uri, final ContentValues values) {
        final SQLiteDatabase db = pictureDbHelper.getWritableDatabase();
        long _id;
        Uri returnUri;

        switch (uriMatcher.match(uri)) {
            case PICTURE:
                _id = db.insert(PictureContract.PictureEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = PictureContract.PictureEntry.buildPictureUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return returnUri;
    }

    @Override
    public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
        final SQLiteDatabase db = pictureDbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case PICTURE:
                final String args = TextUtils.join(", ", selectionArgs);
                db.execSQL(String.format("DELETE FROM " + PictureContract.PictureEntry.TABLE_NAME + " WHERE " + PictureContract.PictureEntry._ID + " IN (%s);", args));

                if (getContext() != null) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return 0;
    }

    @Override
    public int update(final Uri uri, final ContentValues values, final String selection,
                      final String[] selectionArgs) {
        final SQLiteDatabase db = pictureDbHelper.getWritableDatabase();
        int rows;

        switch (uriMatcher.match(uri)) {
            case PICTURE:
                rows = db.update(PictureContract.PictureEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rows != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }
}
