package fr.simonlebras.tagthebus.data.pictures;

import java.util.List;

import fr.simonlebras.tagthebus.models.PictureModel;
import io.reactivex.Completable;
import io.reactivex.Observable;

public interface PictureRepository {
    Observable<List<PictureModel>> loadPictureList(String stationId);

    Completable addPicture(PictureModel picture);

    Completable removePictures(List<PictureModel> pictures);
}
