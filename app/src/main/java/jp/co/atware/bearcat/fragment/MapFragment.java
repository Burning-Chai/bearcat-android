package jp.co.atware.bearcat.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import jp.co.atware.bearcat.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MapFragment extends Fragment {

    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mContext = getContext();

        MapView map = new org.osmdroid.views.MapView(mContext, 256);
        map.setBuiltInZoomControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        MapView.LayoutParams mapParams = new MapView.LayoutParams(
                MapView.LayoutParams.MATCH_PARENT,
                MapView.LayoutParams.MATCH_PARENT, null,
                0, 0, MapView.LayoutParams.BOTTOM_CENTER);

        LinearLayout map_layout = ButterKnife.findById(view, R.id.mapview);
        map_layout.addView(map, mapParams);

        IMapController mapController = map.getController();
        mapController.setZoom(15);

        double tempo_center_lat = 35.45797; //横浜みなとみらいの緯度
        double tempo_center_lng = 139.632314; //横浜みなとみらいの経度
        GeoPoint center_gpt = new GeoPoint(tempo_center_lat, tempo_center_lng);
        mapController.setCenter(center_gpt);

        return view;
    }

}

