package jp.co.atware.bearcat.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import jp.co.atware.bearcat.R;
import jp.co.atware.bearcat.model.NavDrawerItem;

import java.util.List;

public class NavDrawerListAdapter extends BaseAdapter {

    private Context mContext;
    private List<NavDrawerItem> mNavDrawerItemList;

    public NavDrawerListAdapter(final Context context, final List<NavDrawerItem> navDrawerItemList) {
        this.mContext = context;
        this.mNavDrawerItemList = navDrawerItemList;
    }

    @Override
    public int getCount() {
        return mNavDrawerItemList.size();
    }

    @Override
    public Object getItem(final int position) {
        return mNavDrawerItemList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = ButterKnife.findById(view, R.id.icon);
        TextView txtTitle = ButterKnife.findById(view, R.id.title);

        imgIcon.setImageResource(mNavDrawerItemList.get(position).getIcon());
        txtTitle.setText(mNavDrawerItemList.get(position).getTitle());

        return view;
    }

}
