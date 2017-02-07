package fr.simonlebras.tagthebus.presentation.stations.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.simonlebras.tagthebus.R;
import fr.simonlebras.tagthebus.models.StationModel;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

class StationListAdapter extends RecyclerView.Adapter<StationListAdapter.ViewHolder> {
    private final StationListFragment fragment;

    private final LayoutInflater layoutInflater;

    private final List<StationModel> stations = new ArrayList<>();

    StationListAdapter(StationListFragment fragment) {
        this.fragment = fragment;

        this.layoutInflater = LayoutInflater.from(fragment.getContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final ViewHolder viewHolder = new ViewHolder(layoutInflater.inflate(R.layout.list_item_station, parent, false));

        viewHolder.itemView.setOnClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            if (position != NO_POSITION) {
                final StationModel station = stations.get(position);
                fragment.onStationSelected(station.id(), station.streetName());
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.bindStation(stations.get(position));
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    void addAll(List<StationModel> stations) {
        this.stations.clear();
        this.stations.addAll(stations);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_station_street_name)
        TextView stationStreetName;

        ViewHolder(final View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        void bindStation(StationModel station) {
            stationStreetName.setText(station.streetName());
        }
    }
}
