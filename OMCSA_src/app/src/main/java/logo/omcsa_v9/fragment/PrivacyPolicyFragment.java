package logo.omcsa_v9.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import logo.omcsa_v9.R;
import logo.omcsa_v9.activities.MainActivity;

public class PrivacyPolicyFragment extends BaseFragment {


    public PrivacyPolicyFragment() {
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
        mRootView = inflater.inflate(R.layout.fragment_privacy_policy, container, false);
        //Utils.doApplyAllFontForTextView(mContext, mRootView);
        mContext.doShowSearchButton(false);
        showBackButton(true);
        ourWebViewClient webViewClient = new ourWebViewClient("https://omcsa.org/policy");
        ((WebView)mRootView.findViewById(R.id.webView)).setWebViewClient(webViewClient);
        ((WebView)mRootView.findViewById(R.id.webView)).loadUrl("https://omcsa.org/policy");
        mRootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        return mRootView;
    }



    public class ourWebViewClient extends WebViewClient{
        private String currentUrl;
        public ourWebViewClient(String currentUrl) {
            this.currentUrl = currentUrl;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals(currentUrl)) {
                view.loadUrl(url);
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mRootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
