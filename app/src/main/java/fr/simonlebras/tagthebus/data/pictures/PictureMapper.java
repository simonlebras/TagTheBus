package fr.simonlebras.tagthebus.data.pictures;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.simonlebras.tagthebus.data.pictures.provider.PictureContract;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.models.PictureModel;

@PerFragment
public class PictureMapper {
    @Inject
    PictureMapper() {
    }

    List<PictureModel> transform(Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(PictureContract.PictureEntry._ID);
        final int titleIndex = cursor.getColumnIndex(PictureContract.PictureEntry.COLUMN_NAME_TITLE);
        final int createdAtIndex = cursor.getColumnIndex(PictureContract.PictureEntry.COLUMN_NAME_CREATED_AT);
        final int stationIdIndex = cursor.getColumnIndex(PictureContract.PictureEntry.COLUMN_NAME_STATION_ID);
        final int imagePathIndex = cursor.getColumnIndex(PictureContract.PictureEntry.COLUMN_NAME_IMAGE_PATH);

        final List<PictureModel> pictures = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            final PictureModel picture = PictureModel.create(
                    cursor.getLong(idIndex),
                    cursor.getString(titleIndex),
                    cursor.getLong(createdAtIndex),
                    cursor.getString(stationIdIndex),
                    cursor.getString(imagePathIndex)
            );

            pictures.add(picture);
        }

        return pictures;
    }
}
