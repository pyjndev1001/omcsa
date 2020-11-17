package logo.omcsa_v9.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import logo.omcsa_v9.R;
import logo.omcsa_v9.dialog.RenewWarningMessageDialog;
import logo.omcsa_v9.fragment.CategoryFragment;
import logo.omcsa_v9.fragment.DetailFragment;
import logo.omcsa_v9.fragment.DisclaimerFragment;
import logo.omcsa_v9.fragment.EditProfileFragment;
import logo.omcsa_v9.fragment.HomeFragment;
import logo.omcsa_v9.fragment.PrivacyPolicyFragment;
import logo.omcsa_v9.fragment.TipsToUseFragment;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.Utils;
import logo.omcsa_v9.widget.AutoCompleteArrayAdapter;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    DrawerLayout drawerLayout;
    boolean bSearch = false;

    int nSelId = 0;
    List<String> legendTextArray = new ArrayList<>();
    AutoCompleteArrayAdapter legendSearchAdapter = null;

    private final int MSG_TIMER_EXPIRED = 1;
    private final int BACKEY_TIMEOUT = 2000;
    private boolean mIsBackKeyPressed = false;
    private long mCurrentTimeInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.drawerLayout);

        findViewById(R.id.btnSearch).setOnClickListener(this);
        findViewById(R.id.btnCloseSearch).setOnClickListener(this);
        findViewById(R.id.btnMenu).setOnClickListener(this);
        findViewById(R.id.btnTipToUse).setOnClickListener(this);
        findViewById(R.id.btnDisclaimer).setOnClickListener(this);
        findViewById(R.id.btnPrivacyPolicy).setOnClickListener(this);
        findViewById(R.id.btnLogout).setOnClickListener(this);
        findViewById(R.id.txtTitle).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        ((AutoCompleteTextView)findViewById(R.id.editSearch)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                performSearch();
            }
        });

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                switch (nSelId)
                {
                    case R.id.btnEditProfile:
                        doEditProfile();
                        break;
                    case R.id.btnTipToUse:
                        doTipToUse();
                        break;
                    case R.id.btnPrivacyPolicy:
                        doPrivacyPolicy();
                        break;
                    case R.id.btnDisclaimer:
                        doDisclaimer();
                        break;
                }
                nSelId = 0;
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        ((AutoCompleteTextView)findViewById(R.id.editSearch)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(TextUtils.isEmpty(editable.toString()))
                {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
                    if(fragment instanceof  CategoryFragment)
                    {
                        ((CategoryFragment)fragment).doSearch();
                    }
                    findViewById(R.id.btnResetSearch).setVisibility(View.GONE);
                }
                else
                {
                    findViewById(R.id.btnResetSearch).setVisibility(View.VISIBLE);
                }

            }
        });

        findViewById(R.id.btnResetSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.btnResetSearch).setVisibility(View.GONE);
                ((AutoCompleteTextView)findViewById(R.id.editSearch)).setText("");
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
                if(fragment instanceof  CategoryFragment)
                {
                    ((CategoryFragment)fragment).doSearch();
                }
            }
        });

        Utils.doApplyAllFontForTextView(this, findViewById(R.id.layoutMain));
        loadUserInfo();
        legendTextArray = Utils.legendTextMap;
        legendSearchAdapter = new AutoCompleteArrayAdapter(MainActivity.this, 0, legendTextArray);
        ((AutoCompleteTextView)findViewById(R.id.editSearch)).setAdapter(legendSearchAdapter);
        ((AutoCompleteTextView)findViewById(R.id.editSearch)).setThreshold(1);

        HomeFragment fragment = new HomeFragment();
        fragment.setContext(MainActivity.this);
        replaceFragment(fragment);
        if(Utils.getBooleanSetting(MainActivity.this, Global.PREF_IS_LOGIN))
        {
            long expireTime = Utils.getLongSetting(MainActivity.this, Global.PREF_EXPIRE) * 1000;
            long currentTime = System.currentTimeMillis();
            long remain = expireTime - currentTime;
            if(remain < 0)
            {
                RenewWarningMessageDialog dialog = new RenewWarningMessageDialog(MainActivity.this, new RenewWarningMessageDialog.Callback() {
                    @Override
                    public void onOK() {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://omcsa.org/subscribe"));
                        startActivity(browserIntent);
                    }
                });
                dialog.setTitleAndMessage("OMCSA SUBSCRIPTIONS", "Your membership has expired.\nPlease renew.");
                dialog.show();
            }
        }

    }

    private void performSearch()
    {
        doCloseSearch();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
        if(fragment instanceof  CategoryFragment)
        {
            ((CategoryFragment)fragment).doSearch();
        }
        else
        {

            CategoryFragment newFragment = new CategoryFragment();
            newFragment.setContext(MainActivity.this);
            newFragment.setKeyword("Head");
            replaceFragment(newFragment);
        }
    }

    private void loadUserInfo()
    {
        if(Utils.getBooleanSetting(this, Global.PREF_IS_LOGIN))
        {
            long expireTime = Utils.getLongSetting(this, Global.PREF_EXPIRE) * 1000;
            long currentTime = System.currentTimeMillis();
            long remain = expireTime - currentTime;
            if(remain > 0)
            {
                ((TextView)findViewById(R.id.txtExpire)).setText(String.format("%d days of membership", remain / (3600 * 1000 * 24)));
                ((TextView)findViewById(R.id.txtExpire)).setTextColor(getResources().getColor(R.color.green));
            }
            else
            {
                ((TextView)findViewById(R.id.txtExpire)).setText("Expired!");
                ((TextView)findViewById(R.id.txtExpire)).setTextColor(getResources().getColor(R.color.red));
            }
            ((TextView)findViewById(R.id.txtUserName)).setText(Utils.getStringSetting(this, Global.PREF_USERNAME));
            findViewById(R.id.btnEditProfile).setEnabled(true);
            findViewById(R.id.btnEditProfile).setOnClickListener(this);
            ((TextView)findViewById(R.id.btnEditProfile)).setTextColor(getResources().getColor(R.color.black));
            ((TextView)findViewById(R.id.txtLogout)).setText("Log out");
            ((TextView)findViewById(R.id.txtEmail)).setText(Utils.getStringSetting(this, Global.PREF_EMAIL));
        }
        else
        {
            ((TextView)findViewById(R.id.txtExpire)).setText("");
            ((TextView)findViewById(R.id.txtUserName)).setText("Free Member");
            findViewById(R.id.btnEditProfile).setEnabled(false);
            findViewById(R.id.btnEditProfile).setOnClickListener(null);
            ((TextView)findViewById(R.id.btnEditProfile)).setTextColor(getResources().getColor(R.color.grey));
            ((TextView)findViewById(R.id.txtLogout)).setText("Log in");
            ((TextView)findViewById(R.id.txtEmail)).setText("");
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnSearch:
                doSearch();
                break;
            case R.id.btnCloseSearch:
                doCloseSearch();
                break;
            case R.id.btnMenu:
                doMenu();
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnEditProfile:
            case R.id.btnTipToUse:
            case R.id.btnPrivacyPolicy:
            case R.id.btnDisclaimer:
                nSelId = id;
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.btnLogout:
                doLogout();
                break;
            case R.id.txtTitle:
                doHomeScreen();
                break;
        }
    }

    private void doHomeScreen()
    {
        doCloseSearch();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
        if(!(fragment instanceof HomeFragment))
        {
            HomeFragment newFragment = new HomeFragment();
            newFragment.setContext(MainActivity.this);
            replaceFragment(newFragment);
        }
    }

    private void doSearch()
    {
        drawerLayout.closeDrawer(Gravity.LEFT);
        bSearch = true;
        findViewById(R.id.layoutSearch).setVisibility(View.VISIBLE);
        findViewById(R.id.btnSearch).setVisibility(View.GONE);
        findViewById(R.id.btnCloseSearch).setVisibility(View.VISIBLE);
    }

    private void doCloseSearch()
    {
        drawerLayout.closeDrawer(Gravity.LEFT);
        bSearch = false;
        Utils.showKeyboard(this, (EditText)findViewById(R.id.editSearch), false);
        findViewById(R.id.layoutSearch).setVisibility(View.GONE);
        findViewById(R.id.btnSearch).setVisibility(View.VISIBLE);
        findViewById(R.id.btnCloseSearch).setVisibility(View.GONE);
    }

    private void doMenu()
    {
        if(drawerLayout.isDrawerOpen(Gravity.LEFT))
        {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
        else
        {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    private void doEditProfile()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
        if(!(fragment instanceof EditProfileFragment))
        {
            EditProfileFragment newFragment = new EditProfileFragment();
            newFragment.setContext(MainActivity.this);
            replaceFragment(newFragment);
        }
    }

    private void doTipToUse()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
        if(!(fragment instanceof TipsToUseFragment))
        {
            TipsToUseFragment newFragment = new TipsToUseFragment();
            newFragment.setContext(MainActivity.this);
            replaceFragment(newFragment);
        }
    }

    private void doPrivacyPolicy()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
        if(!(fragment instanceof PrivacyPolicyFragment))
        {
            PrivacyPolicyFragment newFragment = new PrivacyPolicyFragment();
            newFragment.setContext(MainActivity.this);
            replaceFragment(newFragment);
        }
    }

    private void doDisclaimer()
    {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentPanel);
        if(!(fragment instanceof  DisclaimerFragment))
        {
            DisclaimerFragment newFragment = new DisclaimerFragment();
            newFragment.setContext(MainActivity.this);
            replaceFragment(newFragment);
        }
    }

    private void doLogout()
    {
        Utils.setBooleanSetting(this, Global.PREF_IS_LOGIN, false);
        Utils.setIntSetting(this, Global.PREF_USER_ID, 0);
        Utils.setStringSetting(this, Global.PREF_TITLE, "");
        Utils.setStringSetting(this, Global.PREF_FIRST_NAME, "");
        Utils.setStringSetting(this, Global.PREF_LAST_NAME, "");
        Utils.setStringSetting(this, Global.PREF_PROFESSION, "");
        Utils.setStringSetting(this, Global.PREF_COUNTRY, "");
        Utils.setStringSetting(this, Global.PREF_USERNAME, "");
        Utils.setStringSetting(this, Global.PREF_EMAIL, "");
        Utils.setLongSetting(this, Global.PREF_EXPIRE, 0);

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void replaceFragment(Fragment fragment) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        try
        {
            if (fragment != null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.contentPanel, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
            /*if(!(fragment instanceof CategoryFragment))
            {
                for(String key : Utils.projectBitmap.keySet())
                {
                    if(!Utils.projectBitmap.get(key).isRecycled())
                    {
                        Utils.projectBitmap.get(key).recycle();
                    }
                }
            }
            if(!(fragment instanceof DetailFragment))
            {
                for(Bitmap bitmap : Utils.bitmapList)
                {
                    if(!bitmap.isRecycled())
                    {
                        bitmap.recycle();
                    }
                }
                Utils.bitmapList.clear();
            }
            Utils.projectBitmap.clear();*/

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 1) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    private void procFinalFinish(){
        if(mIsBackKeyPressed == false){
            mIsBackKeyPressed = true;

            mCurrentTimeInMillis = Calendar.getInstance().getTimeInMillis();

            Toast.makeText(MainActivity.this, getResources().getString(R.string.press_back_once_more_to_exit), Toast.LENGTH_SHORT).show();
            startTimer();
        } else {
            mIsBackKeyPressed = false;

            if(Calendar.getInstance().getTimeInMillis() <= (mCurrentTimeInMillis + (BACKEY_TIMEOUT))){
                finish();

            }
        }
    }

    public Handler mHander = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MSG_TIMER_EXPIRED:
                    mIsBackKeyPressed = false;
                    break;
            }
        }
    };

    private void startTimer(){
        mHander.sendEmptyMessageDelayed(MSG_TIMER_EXPIRED, BACKEY_TIMEOUT);
    }

    public void doShowSearchButton(boolean bShow)
    {
        if(!bShow)
        {
            doCloseSearch();
            findViewById(R.id.btnSearch).setVisibility(View.INVISIBLE);
        }
        else
        {
            findViewById(R.id.btnSearch).setVisibility(View.VISIBLE);
        }
    }

    public void resetSearch(){
        ((EditText)findViewById(R.id.editSearch)).setText("");
        doCloseSearch();
    }

    public String getSearchText()
    {
        try{
            return ((EditText)findViewById(R.id.editSearch)).getText().toString();
        }catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

}
