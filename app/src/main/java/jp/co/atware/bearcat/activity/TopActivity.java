package jp.co.atware.bearcat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import jp.co.atware.bearcat.R;
import jp.co.atware.bearcat.adapter.NavDrawerListAdapter;
import jp.co.atware.bearcat.factory.CouchbaseLiteFactory;
import jp.co.atware.bearcat.fragment.MapFragment;
import jp.co.atware.bearcat.fragment.SampleFragment;
import jp.co.atware.bearcat.model.NavDrawerItem;
import jp.co.atware.bearcat.util.IntentName;
import jp.co.atware.bearcat.util.PreferenceName;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TopActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = TopActivity.class.getName();

    private Activity mActivity;
    private String mUserId;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.list_slider_menu)
    ListView mDrawerList;

    private String[] mNavMenuTitles;
    private Database mDatabase;
    private LocationManager mLocationManager;

    private static SparseArray<Fragment> fragmentSparseArray = new SparseArray<Fragment>() {{
        append(0, new MapFragment());
        append(1, SampleFragment.newInstance(100));
        append(2, SampleFragment.newInstance(200));
        append(3, SampleFragment.newInstance(300));
        append(4, SampleFragment.newInstance(400));
        append(5, SampleFragment.newInstance(500));
        append(6, SampleFragment.newInstance(600));
        append(7, SampleFragment.newInstance(700));
        append(8, SampleFragment.newInstance(800));
        append(9, SampleFragment.newInstance(900));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        ButterKnife.bind(this);
        mActivity = this;

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        mUserId = preferences.getString(PreferenceName.USER_ID, null);
        if (TextUtils.isEmpty(mUserId)) {
            Toast.makeText(this, R.string.failed_to_login, Toast.LENGTH_SHORT).show();
            return;
        }

        // Couchbase Lite
        try {
            mDatabase = CouchbaseLiteFactory.getDatabase(this, Manager.DEFAULT_OPTIONS, "bearcat");
        } catch (CouchbaseLiteException | IOException e) {
            Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.putExtra(IntentName.LOGOUT, false);
            setResult(RESULT_OK, intent);
            this.finish();
            return;
        }

        // GPS
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkEnableGPS();

        // NavBar
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(), createNavDrawerItemList());
        mDrawerList.setAdapter(adapter);
    }

    private List<NavDrawerItem> createNavDrawerItemList() {
        mNavMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);

        List<NavDrawerItem> navDrawerItemList = new ArrayList<>();
        for (int i = 0; i < mNavMenuTitles.length; i++) {
            navDrawerItemList.add(new NavDrawerItem(mNavMenuTitles[i], navMenuIcons.getResourceId(i, 0)));
        }
        navMenuIcons.recycle();

        return navDrawerItemList;
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
            Intent intent = new Intent();
            intent.putExtra(IntentName.LOGOUT, false);
            setResult(RESULT_OK, intent);
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

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment fragment = fragmentSparseArray.get(position);
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

                // update selected item and title, then close the drawer
                mDrawerList.setItemChecked(position, true);
                mDrawerList.setSelection(position);
                setTitle(mNavMenuTitles[position]);
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra(IntentName.LOGOUT, false);
                setResult(RESULT_OK, intent);
                mActivity.finish();
            }
        }
    }

}
