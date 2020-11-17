package logo.omcsa_v9.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;
import java.util.Map;

import logo.omcsa_v9.R;
import logo.omcsa_v9.api.ApiClient;
import logo.omcsa_v9.model.LoginResponse;
import logo.omcsa_v9.model.UserInfo;
import logo.omcsa_v9.utils.Global;
import logo.omcsa_v9.utils.Utils;
import logo.omcsa_v9.widget.GalleryNavigator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    ViewPager tutorialPager = null;
    ImagePageAdapter imagePageAdapter = null;
    GalleryNavigator navigator = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tutorialPager = findViewById(R.id.viewPager);
        navigator = findViewById(R.id.navigator);
        imagePageAdapter = new ImagePageAdapter();
        tutorialPager.setAdapter(imagePageAdapter);
        navigator.setSize(3);

        tutorialPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                navigator.setPosition(i);
                navigator.invalidate();
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnForgetPassword).setOnClickListener(this);
        findViewById(R.id.btnSignup).setOnClickListener(this);
        findViewById(R.id.btnSkipLogin).setOnClickListener(this);
        Utils.doApplyAllFontForTextView(this, findViewById(R.id.layoutMain));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnLogin:
                doLogin();
                break;
            case R.id.btnForgetPassword:
                doForgetPassword();
                break;
            case R.id.btnSignup:
                doSignup();
                break;
            case R.id.btnSkipLogin:
                doSkipLogin();
                break;
        }
    }

    private void doLogin()
    {
        String username = ((EditText)findViewById(R.id.editUserName)).getText().toString();
        String password = ((EditText)findViewById(R.id.editPassword)).getText().toString();

        if(TextUtils.isEmpty(username))
        {
            ((EditText)findViewById(R.id.editUserName)).setError("Please input UserName");
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            ((EditText)findViewById(R.id.editPassword)).setError("Please input Password");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        showProgressDialog("Login now...");
        ApiClient.getMainApiClient().login(params).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                hideProgressDialog();
                LoginResponse loginResponse = response.body();
                if(loginResponse != null && loginResponse.result == 1)
                {
                    doSaveLoginInfo(loginResponse.data);
                    doSkipLogin();
                }
                else if(loginResponse != null && loginResponse.result == 0 && loginResponse.message != null)
                {
                    showErrorMessage("Login Error", loginResponse.message, null, null);
                }
                else
                {
                    showErrorMessage("Login Error", "Network connection error. Please confirm your network connection and try again", null, null);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                hideProgressDialog();
                showErrorMessage("Login Error", "Network connection error. Please confirm your network connection and try again", null, null);
            }
        });

    }

    private void doSaveLoginInfo(UserInfo userInfo)
    {
        Utils.setBooleanSetting(this, Global.PREF_IS_LOGIN, true);
        Utils.setIntSetting(this, Global.PREF_USER_ID, userInfo.userid);
        Utils.setStringSetting(this, Global.PREF_TITLE, userInfo.title);
        Utils.setStringSetting(this, Global.PREF_FIRST_NAME, userInfo.firstname);
        Utils.setStringSetting(this, Global.PREF_LAST_NAME, userInfo.lastname);
        Utils.setStringSetting(this, Global.PREF_PROFESSION, userInfo.profession);
        Utils.setStringSetting(this, Global.PREF_COUNTRY, userInfo.country);
        Utils.setStringSetting(this, Global.PREF_USERNAME, userInfo.username);
        Utils.setStringSetting(this, Global.PREF_EMAIL, userInfo.email);
        Utils.setLongSetting(this, Global.PREF_EXPIRE, userInfo.expire);
    }

    private void doSkipLogin()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void doForgetPassword()
    {
        Intent intent = new Intent(this, ForgetPasswordActivity.class);
        startActivity(intent);
    }

    private void doSignup()
    {
        String url = "http://omcsa.org/subscribe";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public class ImagePageAdapter extends PagerAdapter
    {

        @Override
        public int getCount() {
            return 1;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(LoginActivity.this);
            RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.tutorial_image, container, false);
            ((ImageView)layout.findViewById(R.id.image)).setImageDrawable(getResources().getDrawable(R.drawable.tutorial_image));
            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }
}
