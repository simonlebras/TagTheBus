package fr.simonlebras.tagthebus.presentation.stations.map;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import butterknife.ButterKnife;
import fr.simonlebras.tagthebus.R;

class StationWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final LayoutInflater layoutInflater;

    StationWindowAdapter(LayoutInflater layoutInflater) {
        this.layoutInflater = layoutInflater;
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        @SuppressLint("InflateParams")
        final View view = layoutInflater.inflate(R.layout.window_marker_info, null);

        final TextView title = ButterKnife.findById(view, R.id.text_marker_title);
        title.setText(marker.getTitle());

        return view;
    }
}
