package jp.co.atware.bearcat.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import jp.co.atware.bearcat.R;
import jp.co.atware.bearcat.factory.CouchbaseLiteFactory;
import jp.co.atware.bearcat.util.IntentName;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapFragment extends Fragment implements LocationListener {

    private static final String TAG = MapFragment.class.getName();

    private Activity mActivity;
    private String mUserId;

    private LocationManager mLocationManager;
    private Database mDatabase;

    public static MapFragment newInstance(final String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);

        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);

        return mapFragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        mDatabase = CouchbaseLiteFactory.getDatabase();

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mUserId = getArguments().getString("userId");

        mActivity = getActivity();
        mLocationManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        checkEnableGPS();

        MapView map = ButterKnife.findById(view, R.id.map_view);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(false);

        IMapController mapController = map.getController();
        mapController.setZoom(2);

        List<OverlayItem> anotherOverlayItemArray = new ArrayList<>();
        anotherOverlayItemArray.add(new OverlayItem("0, 0", "0, 0", new GeoPoint(0, 0)));
        anotherOverlayItemArray.add(new OverlayItem("US", "US", new GeoPoint(38.883333, -77.016667)));
        anotherOverlayItemArray.add(new OverlayItem("China", "China", new GeoPoint(39.916667, 116.383333)));
        anotherOverlayItemArray.add(new OverlayItem("United Kingdom", "United Kingdom", new GeoPoint(51.5, -0.116667)));
        anotherOverlayItemArray.add(new OverlayItem("Germany", "Germany", new GeoPoint(52.516667, 13.383333)));
        anotherOverlayItemArray.add(new OverlayItem("Korea", "Korea", new GeoPoint(38.316667, 127.233333)));
        anotherOverlayItemArray.add(new OverlayItem("India", "India", new GeoPoint(28.613333, 77.208333)));
        anotherOverlayItemArray.add(new OverlayItem("Russia", "Russia", new GeoPoint(55.75, 37.616667)));
        anotherOverlayItemArray.add(new OverlayItem("France", "France", new GeoPoint(48.856667, 2.350833)));
        anotherOverlayItemArray.add(new OverlayItem("Canada", "Canada", new GeoPoint(45.4, -75.666667)));

        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = new ItemizedIconOverlay<>(mActivity, anotherOverlayItemArray, null);
        map.getOverlays().add(anotherItemizedIconOverlay);

        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(mActivity);
        map.getOverlays().add(myScaleBarOverlay);

        return view;
    }

    private void checkEnableGPS() {
        boolean enabledGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabledGPS) {
            new AlertDialog.Builder(mActivity)
                    .setCancelable(false)
                    .setTitle(R.string.alert_gps_setting_title)
                    .setMessage(R.string.alert_gps_setting_message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            Toast.makeText(mActivity, R.string.failed_to_enabled_gps, Toast.LENGTH_SHORT).show();
                            mActivity.finish();
                        }
                    })
                    .create()
                    .show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkEnableGPS();

        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT &&
                ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
    }

    @Override
    public void onLocationChanged(final Location location) {
        String msg = "{\"lat\": " + location.getLatitude() + ", \"lng\": " + location.getLongitude() + "}";
        Log.d(TAG, msg); // 35.721065, 139.747899

        try {
            saveOrUpdateTraceLog((long) (location.getLatitude() * 1e6), (long) (location.getLongitude() * 1e6));
        } catch (CouchbaseLiteException e) {
            Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra(IntentName.LOGOUT, false);
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
        }
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
    }

    @Override
    public void onProviderEnabled(final String provider) {
    }

    @Override
    public void onProviderDisabled(final String provider) {
    }

    private void saveOrUpdateTraceLog(final long lat, final long lng) throws CouchbaseLiteException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_mm_dd", Locale.getDefault());

        String date = format.format(new Date());
        Document document = mDatabase.getDocument("log_" + mUserId + "_" + date);
        if (document == null) {
            saveTraceLog(lat, lng, date);
        } else {
            updateTraceLog(lat, lng, document);
        }
    }

    private void saveTraceLog(final long lat, final long lng, final String date) throws CouchbaseLiteException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("lat", lat);
        logMap.put("ng", lng);

        List<Map<String, Object>> logList = new ArrayList<>();
        logList.add(logMap);

        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("_id", "log_" + mUserId + date);
        propertyMap.put("id", "log_" + mUserId + date);
        propertyMap.put("category", "log");
        propertyMap.put("logs", logList);
        propertyMap.put("userId", mUserId);

        Document document = mDatabase.createDocument();
        document.putProperties(propertyMap);
    }

    @SuppressWarnings("unchecked")
    private void updateTraceLog(final long lat, final long lng, final Document document) throws CouchbaseLiteException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("lat", lat);
        logMap.put("ng", lng);

        Map<String, Object> propertyMap = document.getProperties();
        List<Map<String, Object>> logList = (List<Map<String, Object>>) propertyMap.get("logs");
        logList.add(logMap);

        document.putProperties(new HashMap<>(propertyMap));
    }

}

