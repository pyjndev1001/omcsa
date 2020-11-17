package logo.omcsa_v9.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import logo.omcsa_v9.dialog.CustomProgressDialog;
import logo.omcsa_v9.dialog.MessageDialog;

public class BaseActivity extends AppCompatActivity {

    CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showProgressDialog(String strMessage)
    {
        if( progressDialog == null )
        {
            progressDialog = new CustomProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
        }
        progressDialog.show();
        progressDialog.setMessage(strMessage);
    }

    public void hideProgressDialog()
    {
        if(progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


    public void showErrorMessage(String title, String message, String buttonText, MessageDialog.Callback callback)
    {
        /*new MaterialDialog.Builder(this)
                .title(title)
                .content(message)
                .positiveText(R.string.okay)
                .cancelable(false)
                .show();*/
        MessageDialog dialog = new MessageDialog(this, callback);
        dialog.setButtonTitle(buttonText);
        dialog.setTitleAndMessage(title, message);
        dialog.show();
    }
}
