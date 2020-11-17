package logo.omcsa_v9.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;
import logo.omcsa_v9.activities.OrderHistoryActivity;
import logo.omcsa_v9.api.ApiClient;
import logo.omcsa_v9.model.GeneralResponse;
import logo.omcsa_v9.model.OrderHistory;
import logo.omcsa_v9.model.OrderHistoryResponse;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends BaseFragment implements View.OnClickListener {

    private int mTabIdx = 0;
    Spinner yearSpinner, monthSpinner;
    ListView orderListView;
    OrderHistoryAdapter orderListAdapter;
    String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    int selYear, selMonth;

    private List<OrderHistory> orderHistoryList = new ArrayList<>();
    private List<OrderHistory> selOrderHistoryList = new ArrayList<>();

    public EditProfileFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        Utils.doApplyAllFontForTextView(mContext, mRootView);
        mContext.doShowSearchButton(false);
        mRootView.findViewById(R.id.btnRenew).setOnClickListener(this);
        mRootView.findViewById(R.id.btnNext).setOnClickListener(this);
        mRootView.findViewById(R.id.txtEditProfile).setOnClickListener(this);
        mRootView.findViewById(R.id.txtOrderHistory).setOnClickListener(this);

        yearSpinner = mRootView.findViewById(R.id.year_spinner);
        List<Integer> years = new ArrayList<>();
        for(int i = 1900; i < 3200; i++)
        {
            years.add(i);
        }
        ArrayAdapter<Integer> yearAdapter = new ArrayAdapter<>(mContext,
                R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        monthSpinner = mRootView.findViewById(R.id.month_spinner);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(mContext,
                R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        Date date = new Date();
        yearSpinner.setSelection(date.getYear());
        monthSpinner.setSelection(date.getMonth());
        selYear = date.getYear();
        selMonth = date.getMonth();

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selMonth = i;
                refreshOrderList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selYear = i;
                refreshOrderList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        orderListView = mRootView.findViewById(R.id.order_list);
        orderListAdapter = new OrderHistoryAdapter();
        orderListView.setAdapter(orderListAdapter);
        orderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                OrderHistory orderHistory = orderListAdapter.getItem(i);
                Global.selOrderHistory = orderHistory;
                startActivity(new Intent(mContext, OrderHistoryActivity.class));
            }
        });

        ((EditText)mRootView.findViewById(R.id.editNewPassword)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkPasswordValidation();
            }
        });
        showBackButton(false);
        loadData();
        return mRootView;
    }

    private void checkPasswordValidation()
    {
        String strText = ((EditText)mRootView.findViewById(R.id.editNewPassword)).getText().toString();
        if(strText.toLowerCase().equals(strText))
        {
            ((ImageView)mRootView.findViewById(R.id.imgCapital)).setImageDrawable(getResources().getDrawable(R.drawable.cross_grey));
            ((TextView)mRootView.findViewById(R.id.txtCapital)).setTextColor(getResources().getColor(R.color.on_color));
        }
        else
        {
            ((ImageView)mRootView.findViewById(R.id.imgCapital)).setImageDrawable(getResources().getDrawable(R.drawable.check_green));
            ((TextView)mRootView.findViewById(R.id.txtCapital)).setTextColor(getResources().getColor(R.color.grey));
        }
        if(strText.length() < 8)
        {
            ((ImageView)mRootView.findViewById(R.id.imgPasswordLength)).setImageDrawable(getResources().getDrawable(R.drawable.cross_grey));
            ((TextView)mRootView.findViewById(R.id.txtPasswordLength)).setTextColor(getResources().getColor(R.color.on_color));
        }
        else
        {
            ((ImageView)mRootView.findViewById(R.id.imgPasswordLength)).setImageDrawable(getResources().getDrawable(R.drawable.check_green));
            ((TextView)mRootView.findViewById(R.id.txtPasswordLength)).setTextColor(getResources().getColor(R.color.grey));
        }
    }

    private void refreshOrderList()
    {
        selOrderHistoryList.clear();
        for(OrderHistory orderHistory : orderHistoryList)
        {
            String key = String.format("%04d-%02d", selYear + 1900, selMonth + 1);
            if(orderHistory.paydate.startsWith(key))
            {
                selOrderHistoryList.add(orderHistory);
            }
        }
        orderListAdapter.notifyDataSetChanged();
    }

    private void loadData()
    {
        long expireTime = Utils.getLongSetting(mContext, Global.PREF_EXPIRE) * 1000;
        long currentTime = System.currentTimeMillis();
        final long remain = expireTime - currentTime;
        if(remain > 0)
        {
            ((TextView)mRootView.findViewById(R.id.txtExpire)).setText(String.format("%d days left expired", remain / (3600 * 1000 * 24)));
            ((TextView)mRootView.findViewById(R.id.txtExpire)).setTextColor(getResources().getColor(R.color.green));
            mRootView.findViewById(R.id.btnRenew).setVisibility(View.GONE);
        }
        else
        {
            ((TextView)mRootView.findViewById(R.id.txtExpire)).setText("Expired!");
            ((TextView)mRootView.findViewById(R.id.txtExpire)).setTextColor(getResources().getColor(R.color.red));
            mRootView.findViewById(R.id.btnRenew).setVisibility(View.VISIBLE);
        }
        ((TextView)mRootView.findViewById(R.id.txtUserName)).setText("Username : " + Utils.getStringSetting(mContext, Global.PREF_USERNAME));
        ((TextView)mRootView.findViewById(R.id.txtName)).setText(Utils.getStringSetting(mContext, Global.PREF_FIRST_NAME) + " " + Utils.getStringSetting(mContext, Global.PREF_LAST_NAME));
        ((TextView)mRootView.findViewById(R.id.txtEmail)).setText("Email : " + Utils.getStringSetting(mContext, Global.PREF_EMAIL));

        Map<String, String> params = new HashMap<>();
        //params.put("id", String.valueOf(Utils.getIntSetting(mContext, Global.PREF_USER_ID)));
        params.put("id", "3");
        mContext.showProgressDialog("Loading Data...");
        ApiClient.getMainApiClient().getOrderHistory(params).enqueue(new Callback<OrderHistoryResponse>() {
            @Override
            public void onResponse(Call<OrderHistoryResponse> call, Response<OrderHistoryResponse> response) {
                mContext.hideProgressDialog();
                OrderHistoryResponse orderHistoryResponse = response.body();
                if(orderHistoryResponse != null && orderHistoryResponse.result == 1)
                {
                    orderHistoryList = orderHistoryResponse.data;
                    refreshOrderList();
                }
                else
                {
                    if(orderHistoryResponse == null || TextUtils.isEmpty(orderHistoryResponse.message))
                    {
                        mContext.showErrorMessage("Error", "Network connection Error. Please check your internet connection and try again.", null, null);
                    }
                    else
                    {
                        mContext.showErrorMessage("Error", orderHistoryResponse.message, null, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderHistoryResponse> call, Throwable t) {
                mContext.hideProgressDialog();
                mContext.showErrorMessage("Error", "Network connection Error. Please check your internet connection and try again.", null, null);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnRenew:
                doReNew();
                break;
            case R.id.btnNext:
                doNext();
                break;
            case R.id.txtEditProfile:
                switchLayout(0);
                break;
            case R.id.txtOrderHistory:
                switchLayout(1);
                break;
        }
    }

    private void doReNew()
    {
        String url = "http://omcsa.org/subscribe";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void doNext()
    {
        String strEmail = ((EditText)mRootView.findViewById(R.id.editEmail)).getText().toString();
        if(TextUtils.isEmpty(strEmail))
        {
            mContext.showErrorMessage("Error", "Please input Email address", null, null);
            return;
        }

        String strUserName = ((EditText)mRootView.findViewById(R.id.editUserName)).getText().toString();
        if(TextUtils.isEmpty(strUserName))
        {
            mContext.showErrorMessage("Error", "Please input Username", null, null);
            return;
        }

        String strOldPassword = ((EditText)mRootView.findViewById(R.id.editCurrentPassword)).getText().toString();

        if(TextUtils.isEmpty(strOldPassword))
        {
            mContext.showErrorMessage("Error", "Please input Current Password", null, null);
            return;
        }

        String strNewPassword = ((EditText)mRootView.findViewById(R.id.editNewPassword)).getText().toString();

        if(TextUtils.isEmpty(strNewPassword))
        {
            mContext.showErrorMessage("Error", "Please input New Password", null, null);
            return;
        }

        if(strNewPassword.toLowerCase().equals(strNewPassword))
        {
            mContext.showErrorMessage("Error", "Password contains at least one capital letter", null, null);
            return;
        }

        if(strNewPassword.length() < 8)
        {
            mContext.showErrorMessage("Error", "Password Length must be at least 8 characters", null, null);
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("id", String.valueOf(Utils.getIntSetting(mContext, Global.PREF_USER_ID)));
        params.put("new_password", strNewPassword);
        params.put("old_password", strOldPassword);
        params.put("username", strUserName);
        params.put("email", strEmail);

        mContext.showProgressDialog("Submitting Request...");
        ApiClient.getMainApiClient().updateUserInfo(params).enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                mContext.hideProgressDialog();
                GeneralResponse generalResponse = response.body();
                if(generalResponse != null && generalResponse.result == 1)
                {
                    ((EditText)mRootView.findViewById(R.id.editEmail)).setText("");
                    ((EditText)mRootView.findViewById(R.id.editUserName)).setText("");
                    ((EditText)mRootView.findViewById(R.id.editCurrentPassword)).setText("");
                    ((EditText)mRootView.findViewById(R.id.editNewPassword)).setText("");
                    checkPasswordValidation();
                    mRootView.findViewById(R.id.txtOrderHistory).performClick();
                }
                else
                {
                    if(generalResponse != null && !TextUtils.isEmpty(generalResponse.message))
                    {
                        mContext.showErrorMessage("Error", generalResponse.message, null, null);
                    }
                    else
                    {
                        mContext.showErrorMessage("Error", "Network connection Error. Please check your internet connection and try again.", null, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse> call, Throwable t) {
                mContext.hideProgressDialog();
                mContext.showErrorMessage("Error", "Network connection Error. Please check your internet connection and try again.", null, null);
            }
        });

    }

    private void switchLayout(int tabIdx)
    {
        if(mTabIdx == tabIdx) return;
        if(tabIdx == 0)
        {
            mRootView.findViewById(R.id.layoutEditProfile).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.layoutOrderHistory).setVisibility(View.GONE);
            ((TextView)mRootView.findViewById(R.id.txtOrderHistory)).setTextColor(getResources().getColor(R.color.on_color));
            ((TextView)mRootView.findViewById(R.id.txtEditProfile)).setTextColor(getResources().getColor(R.color.grey));
        }
        else if(tabIdx == 1)
        {
            mRootView.findViewById(R.id.layoutEditProfile).setVisibility(View.GONE);
            mRootView.findViewById(R.id.layoutOrderHistory).setVisibility(View.VISIBLE);
            ((TextView)mRootView.findViewById(R.id.txtOrderHistory)).setTextColor(getResources().getColor(R.color.grey));
            ((TextView)mRootView.findViewById(R.id.txtEditProfile)).setTextColor(getResources().getColor(R.color.on_color));
        }
        mTabIdx = tabIdx;
    }

    public class OrderHistoryAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return selOrderHistoryList.size();
        }

        @Override
        public OrderHistory getItem(int i) {
            return selOrderHistoryList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            if(view == null)
            {
                view = mContext.getLayoutInflater().inflate(R.layout.row_order_history, viewGroup, false);
            }
            Utils.doApplyAllFontForTextView(mContext, view);
            OrderHistory orderHistory = getItem(i);
            ((TextView)view.findViewById(R.id.order_number)).setText(String.format("%02d", orderHistory.id));
            ((TextView)view.findViewById(R.id.order_id)).setText(String.format("ID%08d", orderHistory.id));
            return view;
        }
    }
}
