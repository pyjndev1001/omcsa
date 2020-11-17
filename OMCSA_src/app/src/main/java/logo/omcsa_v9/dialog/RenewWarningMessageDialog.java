package logo.omcsa_v9.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import logo.omcsa_v9.R;
import logo.omcsa_v9.utils.Utils;


public class RenewWarningMessageDialog extends Dialog {

	String title = "", message = "";

	Callback callback;
	String button;
	public interface Callback
	{
		public void onOK();
	}

	public RenewWarningMessageDialog(Context context, Callback callback)
	{
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.callback = callback;
	}


    public RenewWarningMessageDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.0f;
		getWindow().setAttributes(lpWindow);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.dialog_renew_warning);
		Utils.doApplyAllFontForTextView(getContext(), findViewById(R.id.layoutMain));
		findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
				if(callback != null)
				{
					callback.onOK();
				}
			}
		});

		findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		((TextView)findViewById(R.id.title)).setText(title);
		((TextView)findViewById(R.id.message)).setText(message);
		if(!TextUtils.isEmpty(button))
		{
			((TextView)findViewById(R.id.okTxt)).setText(button);
		}
	}

	public void setTitleAndMessage(String title, String message)
	{
		this.title = title;
		this.message = message;
	}

	public void setButtonTitle(String text)
	{
		button = text;
	}
}

