package logo.omcsa_v9.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;
import logo.omcsa_v9.utils.Utils;

public class TipsToUseFragment extends BaseFragment {

    public TipsToUseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_tips_to_use, container, false);
        Utils.doApplyAllFontForTextView(mContext, mRootView);
        mContext.doShowSearchButton(false);
        showBackButton(true);
        return mRootView;
    }
}
