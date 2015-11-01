package jp.co.atware.bearcat.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.couchbase.lite.CouchbaseLiteException;
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
import java.util.ArrayList;
import java.util.List;

public class TopActivity extends AppCompatActivity {

    private Activity mActivity;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.list_slider_menu)
    ListView mDrawerList;

    private String[] mNavMenuTitles;
    private SparseArray<Fragment> fragmentSparseArray = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        ButterKnife.bind(this);
        mActivity = this;

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String mUserId = preferences.getString(PreferenceName.USER_ID, null);
        if (TextUtils.isEmpty(mUserId)) {
            Toast.makeText(this, R.string.failed_to_login, Toast.LENGTH_SHORT).show();
            return;
        }

        // Couchbase Lite
        try {
            CouchbaseLiteFactory.getDatabase(this, Manager.DEFAULT_OPTIONS, "bearcat");
        } catch (CouchbaseLiteException | IOException e) {
            Toast.makeText(mActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra(IntentName.LOGOUT, false);
            setResult(RESULT_OK, intent);
            mActivity.finish();
            return;
        }

        // Fragment
        fragmentSparseArray.append(0, MapFragment.newInstance(mUserId));
        fragmentSparseArray.append(1, SampleFragment.newInstance(100));
        fragmentSparseArray.append(2, SampleFragment.newInstance(200));
        fragmentSparseArray.append(3, SampleFragment.newInstance(300));
        fragmentSparseArray.append(4, SampleFragment.newInstance(400));
        fragmentSparseArray.append(5, SampleFragment.newInstance(500));
        fragmentSparseArray.append(6, SampleFragment.newInstance(600));
        fragmentSparseArray.append(7, SampleFragment.newInstance(700));
        fragmentSparseArray.append(8, SampleFragment.newInstance(800));
        fragmentSparseArray.append(9, SampleFragment.newInstance(900));

        // NavBar
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
        NavDrawerListAdapter adapter = new NavDrawerListAdapter(getApplicationContext(), createNavDrawerItemList());
        mDrawerList.setAdapter(adapter);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragmentSparseArray.get(0)).commit();
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
