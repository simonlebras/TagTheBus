package fr.simonlebras.tagthebus.models;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PictureModel implements Parcelable {
    public static PictureModel create(long id, String title, long createdAt, String stationId, String imagePath) {
        return new AutoValue_PictureModel(id, title, createdAt, stationId, imagePath);
    }

    public abstract long id();

    public abstract String title();

    public abstract long createdAt();

    public abstract String stationId();

    public abstract String imagePath();
}
