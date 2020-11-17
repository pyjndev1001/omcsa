package logo.omcsa_v9.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import logo.omcsa_v9.R;
import logo.omcsa_v9.api.ApiClient;
import logo.omcsa_v9.dialog.MessageDialog;
import logo.omcsa_v9.model.GeneralResponse;
import logo.omcsa_v9.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        findViewById(R.id.btnResetPassword).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        Utils.doApplyAllFontForTextView(this, findViewById(R.id.layoutMain));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id)
        {
            case R.id.btnResetPassword:
                doResetPassword();
                break;
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }

    private void doResetPassword()
    {
        final String username = ((EditText)findViewById(R.id.editUserName)).getText().toString();
        if(TextUtils.isEmpty(username))
        {
            ((EditText)findViewById(R.id.editUserName)).setError("Please input username or password");
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        showProgressDialog("Sending email...");

        ApiClient.getMainApiClient().forgetPassword(params).enqueue(new Callback<GeneralResponse>() {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                hideProgressDialog();
                GeneralResponse generalResponse = response.body();
                if(generalResponse != null && generalResponse.result == 1)
                {
                    showErrorMessage("Password Reset", "A mail has been sent to " + username + ". It contains a link you must click to reset your password.", "close", new MessageDialog.Callback() {
                        @Override
                        public void onOK() {
                            onBackPressed();
                        }
                    });
                }
                else
                {
                    if(generalResponse != null && !TextUtils.isEmpty(generalResponse.message))
                    {
                        showErrorMessage("Sending Email Error", generalResponse.message, null, null);
                    }
                    else
                    {
                        showErrorMessage("Sending Email Error", "Network connection error. Please confirm your network connection and try again", null, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse> call, Throwable t) {
                hideProgressDialog();
                showErrorMessage("Sending Email Error", "Network connection error. Please confirm your network connection and try again", null, null);
            }
        });
    }
}

