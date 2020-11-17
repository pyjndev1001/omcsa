package logo.omcsa_v9.fragment;

import android.support.v4.app.Fragment;
import android.view.View;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;

public class BaseFragment extends Fragment {

    public View mRootView;
    public MainActivity mContext;

    public void setContext(MainActivity context)
    {
        mContext = context;
    }

    public void showBackButton(boolean bValue)
    {
        mContext.findViewById(R.id.btnBack).setVisibility(bValue ? View.VISIBLE : View.GONE);
    }
}
