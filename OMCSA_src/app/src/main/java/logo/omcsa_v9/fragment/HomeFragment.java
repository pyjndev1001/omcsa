package logo.omcsa_v9.fragment;

import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;
import logo.omcsa_v9.api.ApiClient;
import logo.omcsa_v9.model.CategoryInfoResponse;
import logo.omcsa_v9.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    int[] imgIds = {R.id.imgCategory1, R.id.imgCategory2, R.id.imgCategory3, R.id.imgCategory4, R.id.imgCategory5, R.id.imgCategory6, R.id.imgCategory7};
    int[] txtIds = {R.id.txtCategory1, R.id.txtCategory2, R.id.txtCategory3, R.id.txtCategory4, R.id.txtCategory5, R.id.txtCategory6, R.id.txtCategory7};
    Map<String, Integer> imageMap = new HashMap<>();
    Point size;
    public HomeFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_home, container, false);

        Display display = mContext.getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);

        Utils.doApplyAllFontForTextView(mContext, mRootView);
        if(mContext != null)
        {
            mContext.doShowSearchButton(true);
        }

        mRootView.findViewById(R.id.layoutCategory1).setOnClickListener(this);
        mRootView.findViewById(R.id.layoutCategory2).setOnClickListener(this);
        mRootView.findViewById(R.id.layoutCategory3).setOnClickListener(this);
        mRootView.findViewById(R.id.layoutCategory4).setOnClickListener(this);
        mRootView.findViewById(R.id.layoutCategory5).setOnClickListener(this);
        mRootView.findViewById(R.id.layoutCategory6).setOnClickListener(this);
        mRootView.findViewById(R.id.layoutCategory7).setOnClickListener(this);

        imageMap.put("Head", R.drawable.head);
        imageMap.put("neck & lower face", R.drawable.neck);
        imageMap.put("spine", R.drawable.spine);
        imageMap.put("thorax", R.drawable.thorax);
        imageMap.put("abdomen & pelvis", R.drawable.abdomen);
        imageMap.put("Upper limb", R.drawable.upper_limb);
        imageMap.put("lower limb", R.drawable.lower_limb);

        loadData();
        showBackButton(false);
        return mRootView;
    }

    private void loadData()
    {
        initView();
    }

    private void initView()
    {
        mRootView.findViewById(R.id.layoutCategory1).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layoutCategory2).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layoutCategory3).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layoutCategory4).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layoutCategory5).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layoutCategory6).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.layoutCategory7).setVisibility(View.VISIBLE);

        mRootView.findViewById(R.id.layoutCategory1).setTag(0);
        mRootView.findViewById(R.id.layoutCategory2).setTag(1);
        mRootView.findViewById(R.id.layoutCategory3).setTag(2);
        mRootView.findViewById(R.id.layoutCategory4).setTag(3);
        mRootView.findViewById(R.id.layoutCategory5).setTag(4);
        mRootView.findViewById(R.id.layoutCategory6).setTag(5);
        mRootView.findViewById(R.id.layoutCategory7).setTag(6);

        for(int i = 0; i < 7; i++)
        {
            ((TextView)mRootView.findViewById(txtIds[i])).setText(Utils.categoryInfoList.get(i).name);
            ((ImageView)mRootView.findViewById(imgIds[i])).setImageDrawable(getResources().getDrawable(imageMap.get(Utils.categoryInfoList.get(i).name)));
            if(size.x >= 1024)
            {
                ((TextView)mRootView.findViewById(txtIds[i])).setTextSize(16);
            }
            else if(size.x >= 800)
            {
                ((TextView)mRootView.findViewById(txtIds[i])).setTextSize(14);
            }
            else if(size.x >= 600)
            {
                ((TextView)mRootView.findViewById(txtIds[i])).setTextSize(12);
            }
            else if(size.x >= 400)
            {
                ((TextView)mRootView.findViewById(txtIds[i])).setTextSize(10);
            }
        }

    }

    @Override
    public void onClick(View view) {
        int index = ((Integer)view.getTag()).intValue();
        String category = Utils.categoryInfoList.get(index).name;
        mContext.resetSearch();

        CategoryFragment newFragment = new CategoryFragment();
        newFragment.setContext(mContext);
        newFragment.setKeyword(category);

        mContext.replaceFragment(newFragment);
    }

}
