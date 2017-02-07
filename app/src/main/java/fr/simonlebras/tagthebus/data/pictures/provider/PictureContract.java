package fr.simonlebras.tagthebus.data.pictures.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import fr.simonlebras.tagthebus.BuildConfig;

public final class PictureContract {
    private PictureContract() {
    }

    static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    static final String PATH_PICTURE = "picture";

    public static final class PictureEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PICTURE).build();

        static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_URI + "/" + PATH_PICTURE;
        static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_URI + "/" + PATH_PICTURE;

        static final String TABLE_NAME = "picture";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CREATED_AT = "createdAt";
        public static final String COLUMN_NAME_STATION_ID = "stationId";
        public static final String COLUMN_NAME_IMAGE_PATH = "imagePath";

        static final String INDEX_NAME_STATION_ID = "stationIdIndex";

        static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + PictureEntry.TABLE_NAME + " (" +
                        PictureEntry._ID + " INTEGER PRIMARY KEY," +
                        PictureEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL," +
                        PictureEntry.COLUMN_NAME_CREATED_AT + " INTEGER NOT NULL," +
                        PictureEntry.COLUMN_NAME_STATION_ID + " TEXT NOT NULL," +
                        PictureEntry.COLUMN_NAME_IMAGE_PATH + " TEXT NOT NULL);";

        static final String SQL_CREATE_INDEX =
                "CREATE INDEX " + INDEX_NAME_STATION_ID + " ON " + TABLE_NAME + "(" + COLUMN_NAME_STATION_ID + ");";

        static Uri buildPictureUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
