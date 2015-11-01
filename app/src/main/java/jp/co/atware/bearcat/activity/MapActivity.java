package jp.co.atware.bearcat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.ButterKnife;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import jp.co.atware.bearcat.R;
import jp.co.atware.bearcat.factory.CouchbaseLiteFactory;
import jp.co.atware.bearcat.util.PreferenceName;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = MapActivity.class.getName();

    private Activity mActivity;
    private String mUserId;

    private Database mDatabase;
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ButterKnife.bind(this);
        mActivity = this;

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        mUserId = preferences.getString(PreferenceName.USER_ID, null);
        if (TextUtils.isEmpty(mUserId)) {
            Toast.makeText(this, R.string.failed_to_login, Toast.LENGTH_SHORT).show();
            return;
        }

        /* --------------------------------------- MAP --------------------------------------- */
        MapView map = new org.osmdroid.views.MapView(this, 256);
        map.setBuiltInZoomControls(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        MapView.LayoutParams mapParams = new MapView.LayoutParams(
                MapView.LayoutParams.MATCH_PARENT,
                MapView.LayoutParams.MATCH_PARENT, null,
                0, 0, MapView.LayoutParams.BOTTOM_CENTER);


        LinearLayout map_layout = ButterKnife.findById(this, R.id.mapview);
        map_layout.addView(map, mapParams);

        IMapController mapController = map.getController();
        mapController.setZoom(15);

        double tempo_center_lat = 35.45797; //横浜みなとみらいの緯度
        double tempo_center_lng = 139.632314; //横浜みなとみらいの経度
        GeoPoint center_gpt = new GeoPoint(tempo_center_lat, tempo_center_lng);
        mapController.setCenter(center_gpt);
        /* --------------------------------------- MAP --------------------------------------- */

        try {
            mDatabase = CouchbaseLiteFactory.getDatabase(this, Manager.DEFAULT_OPTIONS, "bearcat");
        } catch (CouchbaseLiteException | IOException e) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        // GPS
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkEnableGPS();
    }

    private void checkEnableGPS() {
        boolean enabledGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabledGPS) {
            new AlertDialog.Builder(this)
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
    protected void onResume() {
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
    protected void onPause() {
        super.onPause();

        if (Build.VERSION_CODES.M <= Build.VERSION.SDK_INT &&
                ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(final Location location) {
        String msg = "{\"lat\": " + location.getLatitude() + ", \"lng\": " + location.getLongitude() + "}";
        Log.d(TAG, msg); // 35.721065, 139.747899

        try {
            saveOrUpdateTraceLog((long) (location.getLatitude() * 1e6), (long) (location.getLongitude() * 1e6));
        } catch (CouchbaseLiteException e) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            this.finish();
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

    /*
    {
  "id": "12_2015_11_01",
  "category": "log",
  "date": "2015_11_01",
  "logs": [
    {
      "lat": 35721065,
      "lng": 139747899
    },
    {
      "lat": 35721065,
      "lng": 139747899
    }
  ],
  "userId": 12
}
     */

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
