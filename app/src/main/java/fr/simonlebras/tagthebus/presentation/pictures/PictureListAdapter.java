package fr.simonlebras.tagthebus.presentation.pictures;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.models.PictureModel;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

class PictureListAdapter extends RecyclerView.Adapter<PictureListAdapter.ViewHolder> {
    private final PictureListFragment fragment;

    private final LayoutInflater layoutInflater;

    private final DateFormat dateFormat;

    private final BitmapRequestBuilder<Uri, Bitmap> glideRequest;

    private final List<PictureModel> pictures = new ArrayList<>();

    private final SparseBooleanArray selectedItems = new SparseBooleanArray();

    PictureListAdapter(PictureListFragment fragment) {
        this.fragment = fragment;

        this.layoutInflater = LayoutInflater.from(fragment.getContext());

        dateFormat = android.text.format.DateFormat.getDateFormat(fragment.getContext());

        glideRequest = Glide.with(fragment)
                .from(Uri.class)
                .asBitmap()
                .placeholder(ContextCompat.getDrawable(fragment.getContext(), R.drawable.ic_directions_bus_blue_40dp))
                .error(ContextCompat.getDrawable(fragment.getContext(), R.drawable.ic_directions_bus_blue_40dp))
                .diskCacheStrategy(DiskCacheStrategy.NONE);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final ViewHolder viewHolder = new ViewHolder(layoutInflater.inflate(R.layout.list_item_picture, parent, false));

        viewHolder.itemView.setOnClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            if (position != NO_POSITION) {
                fragment.onPictureClicked(position);
            }
        });

        viewHolder.itemView.setOnLongClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            if (position != NO_POSITION) {
                fragment.onPictureLongClicked(position);
            }
            return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bindPicture(pictures.get(position));

        holder.itemView.setSelected(isSelected(position));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position, final List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);

            return;
        }

        final PictureModel picture = pictures.get(position);

        final Bundle diff = (Bundle) payloads.get(0);
        for (String key : diff.keySet()) {
            if (PictureListDiffCallback.BUNDLE_DIFF_TITLE.equals(key)) {
                holder.bindPictureTitle(picture.title());
            } else if (PictureListDiffCallback.BUNDLE_DIFF_CREATED_AT.equals(key)) {
                holder.bindPictureCreatedAt(picture.createdAt());
            } else if (PictureListDiffCallback.BUNDLE_DIFF_IMAGE_PATH.equals(key)) {
                holder.bindPictureThumbnail(picture.imagePath());
            }
        }

        holder.itemView.setSelected(isSelected(position));
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    public List<PictureModel> getPictures() {
        return pictures;
    }

    PictureModel getPicture(int position) {
        return pictures.get(position);
    }

    void addAll(List<PictureModel> pictures) {
        this.pictures.clear();
        this.pictures.addAll(pictures);
    }

    void removeAll() {
        pictures.clear();
    }

    private boolean isSelected(int position) {
        return selectedItems.get(position, false);
    }

    void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }

        fragment.setActionModeTitle();

        notifyItemChanged(position);
    }

    void clearSelection() {
        selectedItems.clear();

        notifyDataSetChanged();
    }

    int getSelectedItemCount() {
        return selectedItems.size();
    }

    List<PictureModel> getSelectedItems() {
        final int size = selectedItems.size();
        final List<PictureModel> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(pictures.get(selectedItems.keyAt(i)));
        }
        return items;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_picture_title)
        TextView pictureTitle;

        @BindView(R.id.text_picture_created_at)
        TextView pictureCreatedAt;

        @BindView(R.id.image_picture_thumbnail)
        ImageView pictureThumbnail;

        ViewHolder(final View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void bindPicture(PictureModel picture) {
            bindPictureTitle(picture.title());
            bindPictureCreatedAt(picture.createdAt());
            bindPictureThumbnail(picture.imagePath());
        }

        void bindPictureTitle(String title) {
            pictureTitle.setText(title);
        }

        void bindPictureCreatedAt(long createdAt) {
            final Date date = new Date(createdAt);

            pictureCreatedAt.setText(dateFormat.format(date));
        }

        void bindPictureThumbnail(String imagePath) {
            glideRequest.load(Uri.fromFile(new File(imagePath)))
                    .into(pictureThumbnail);
        }
    }
}
