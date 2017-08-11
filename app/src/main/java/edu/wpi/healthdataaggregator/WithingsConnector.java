package edu.wpi.healthdataaggregator;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * Created by Adonay on 8/9/2017.
 */

public class WithingsConnector extends Connector {

    private WebView loginWebView = null;
    public static OAuthService service;
    public static Token requestToken;
    String secret, token;
    Token accessToken;
    String userid = "";

    /**
     *
     * @param activity
     * @param progressBarLayout
     */
    public WithingsConnector(AppCompatActivity activity, RelativeLayout progressBarLayout){
        super(SourceType.WITHINGS, activity, progressBarLayout);
    }


    @Override
    public void connect() {
        setConnected(true);
        getProgressBarLayout().setVisibility(View.GONE);
        getCredentials();
    }

    @Override
    public void disconnect() {
        setConnected(false);
        getProgressBarLayout().setVisibility(View.GONE);
    }

    @Override
    public void loadHealthData(TextView textView) {

    }

    public void withingsLogin(){

        if(getActivity() instanceof MainActivity){
            loginWebView = ((MainActivity) getActivity()).getLoginWebview();
            Log.i(getTAG(), "FROM MAIN");
        }else if(getActivity() instanceof AddSourcesActivity){
            loginWebView = ((AddSourcesActivity) getActivity()).getLoginWebview();
            Log.i(getTAG(), "FROM AddSources");
        }

        loginWebView.setVisibility(View.VISIBLE);

        loginWebView.getSettings().setJavaScriptEnabled(true);
        loginWebView.setWebViewClient(new MyWebViewClient(loginWebView));

        service = new ServiceBuilder().provider(WithingsApi.class)
                .apiKey(WithingsApi.getKey())
                .apiSecret(WithingsApi.getSecret())
                .build();

        new Thread(new Runnable() {
            public void run() {
                requestToken = service.getRequestToken();
                final String authURL = service.getAuthorizationUrl(requestToken);
                loginWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        loginWebView.loadUrl(authURL);
                    }
                });

            }
        }).start();
    }


    class MyWebViewClient extends WebViewClient {
        WebView wvAuthorise;
        MyWebViewClient(WebView wv){
            wvAuthorise = wv;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            getUSERID(url);
        }
    }

    private void getUSERID(final String url) {

        try {
            String divStr = "userid=";
            int first = url.indexOf(divStr);

            if(first!=-1){
                userid = url.substring(first+divStr.length());

                getAccessTokenThread.execute((Object) null);
                loginWebView.setVisibility(View.GONE);
            } else {
                //...
            }

        } catch (Exception e) {
            Log.e(getTAG(),e.getMessage());
            //...
        }
    }

    private void getCredentials() {
        try {
            if (!PreferencesManager.isSourceConnected(getActivity(), SourceType.WITHINGS)) {
                withingsLogin();
            } else {
                // TODO load all users and if isn't anyone correct
                // startAuthenticationActivity
                secret = PreferencesManager.getWithingsLoginInfo(getActivity(), "secret");
                token = PreferencesManager.getWithingsLoginInfo(getActivity(), "token");
                userid = PreferencesManager.getWithingsLoginInfo(getActivity(), "userid");
                Log.i(TAG, "secret  : " + secret);
                Log.i(TAG, "token  : " + token);
                Log.i(TAG, "userid  : " + userid);
                try {
                    service = new ServiceBuilder().provider(WithingsApi.class)
                            .apiKey(WithingsApi.getKey())
                            .apiSecret(WithingsApi.getSecret()).build();
                    accessToken = new Token(token, secret);

                    //loadData();
                } catch (Exception ex) {
                    withingsLogin();
                }

            }
        } catch (Exception ex) {
            Log.e(TAG, "try on create" + ex.getLocalizedMessage());
        }
    }

    AsyncTask<Object, Object, Object> getAccessTokenThread = new AsyncTask<Object, Object, Object>() {
        @Override
        protected Object doInBackground(Object... params) {
            accessToken = service
                    .getAccessToken(requestToken, new Verifier(""));

            secret = accessToken.getSecret();
            token = accessToken.getToken();
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // authentication complete save the token,secret,userid
            saveUserDatatoSharedPreferences(token, secret, userid);
            //loadData();
        }

    };


    private void saveUserDatatoSharedPreferences(String token, String secret, String userid){
        PreferencesManager.setWithingsLoginInfo(getActivity(), "token", token);
        PreferencesManager.setWithingsLoginInfo(getActivity(), "secret", secret);
        PreferencesManager.setWithingsLoginInfo(getActivity(), "userid", userid);
    }


}

class WithingsApi extends DefaultApi10a {

    private static final String AUTHORIZATION_URL ="https://oauth.withings.com/account/authorize?oauth_token=%s";
    private static final String apiKey = "6d5f94d62bd90518f672234c158853913cdd09200fbd635f546f43de3";
    private static final String apiSecret = "d2b53e5534d2937522f7c9212b363e8e331ee971159d535c72e65dd93063";

    @Override
    public String getRequestTokenEndpoint() {
        return "https://oauth.withings.com/account/request_token";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://oauth.withings.com/account/access_token";
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return String.format(getAUTHORIZATION_URL(), requestToken.getToken());
    }

    public static String getKey(){
        return apiKey;
    }

    public static String getSecret(){
        return apiSecret;
    }

    public static String getAUTHORIZATION_URL() {
        return AUTHORIZATION_URL;
    }
}
