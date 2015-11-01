package jp.co.atware.bearcat.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import jp.co.atware.bearcat.R;

public class SampleFragment extends Fragment {

    public SampleFragment() {
    }

    public static SampleFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt("page", page);

        SampleFragment fragment = new SampleFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        int page = getArguments().getInt("page", 999);

        View view = inflater.inflate(R.layout.fragment_sample, container, false);

        TextView pageTextView = ButterKnife.findById(view, R.id.page);
        pageTextView.setText(String.valueOf(page));

        return view;
    }
}
