package fr.simonlebras.tagthebus.presentation.pictures;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

import fr.simonlebras.tagthebus.models.PictureModel;

class PictureListDiffCallback extends DiffUtil.Callback {
    static final String BUNDLE_DIFF_TITLE = "BUNDLE_DIFF_TITLE";
    static final String BUNDLE_DIFF_CREATED_AT = "BUNDLE_DIFF_CREATED_AT";
    static final String BUNDLE_DIFF_IMAGE_PATH = "BUNDLE_DIFF_IMAGE_PATH";

    private final List<PictureModel> oldList;
    private final List<PictureModel> newList;

    PictureListDiffCallback(List<PictureModel> oldList, List<PictureModel> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
        return oldList.get(oldItemPosition).id() == newList.get(newItemPosition).id();
    }

    @Override
    public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
        final PictureModel oldPicture = oldList.get(oldItemPosition);
        final PictureModel newPicture = newList.get(newItemPosition);

        return oldPicture.title().equals(newPicture.title()) &&
                oldPicture.createdAt() == newPicture.createdAt() &&
                oldPicture.imagePath().equals(newPicture.imagePath());
    }

    @Nullable
    @Override
    public Object getChangePayload(final int oldItemPosition, final int newItemPosition) {
        final Bundle diff = new Bundle();

        final PictureModel oldPicture = oldList.get(oldItemPosition);
        final PictureModel newPicture = newList.get(newItemPosition);

        if (!oldPicture.title().equals(newPicture.title())) {
            diff.putBoolean(BUNDLE_DIFF_TITLE, true);
        }

        if (oldPicture.createdAt() != newPicture.createdAt()) {
            diff.putBoolean(BUNDLE_DIFF_CREATED_AT, true);
        }

        if (!oldPicture.imagePath().equals(newPicture.imagePath())) {
            diff.putBoolean(BUNDLE_DIFF_IMAGE_PATH, true);
        }

        return diff;
    }
}
