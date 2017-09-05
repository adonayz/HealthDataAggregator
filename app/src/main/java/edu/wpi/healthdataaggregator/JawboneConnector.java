package edu.wpi.healthdataaggregator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jawbone.upplatformsdk.api.ApiManager;
import com.jawbone.upplatformsdk.api.response.OauthAccessTokenResponse;
import com.jawbone.upplatformsdk.oauth.OauthUtils;
import com.jawbone.upplatformsdk.oauth.OauthWebViewActivity;
import com.jawbone.upplatformsdk.utils.UpPlatformSdkConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Adonay on 7/26/2017.
 */

public class JawboneConnector extends Connector {

    private static final String CLIENT_ID = "CbkdwBxrD1c";
    private static final String CLIENT_SECRET = "ea3dc1856a1212c772b556544636ebe0d55567f5";
    private static final String OAUTH_CALLBACK_URL = "up-platform://redirect";
    private List<UpPlatformSdkConstants.UpPlatformAuthScope> authScope;
    private static final int OAUTH_REQUEST_CODE = 25;
    private TextView textView;

    /**
     *
     * @param activity
     * @param progressBarLayout
     */
    JawboneConnector(AppCompatActivity activity, RelativeLayout progressBarLayout) {
        super(SourceType.JAWBONE, activity, progressBarLayout);
    }

    @Override
    public void connect() {
        if(!PreferencesManager.isSourceConnected(getActivity(), getSourceType())){
            authScope  = new ArrayList<UpPlatformSdkConstants.UpPlatformAuthScope>();
            authScope.add(UpPlatformSdkConstants.UpPlatformAuthScope.ALL);
            Intent intent = getIntentForWebView();
            getActivity().startActivityForResult(intent, OAUTH_REQUEST_CODE);
        }

        setConnected(true);
    }

    @Override
    public void disconnect() {
        this.setConnected(false);

    }

    @Override
    public void loadHealthData(TextView textView) {
        this.textView = textView;
        ApiManager.getRestApiInterface().getTrends(
                UpPlatformSdkConstants.API_VERSION_STRING,
                getTrendsRequestParams(),
                genericCallbackListener);

    }

    /**
     *
     * @return
     */
    private Intent getIntentForWebView() {
        Uri.Builder builder = OauthUtils.setOauthParameters(CLIENT_ID, OAUTH_CALLBACK_URL, authScope);

        Intent intent = new Intent(OauthWebViewActivity.class.getName());
        intent.putExtra(UpPlatformSdkConstants.AUTH_URI, builder.build());
        return intent;
    }

    /**
     *
     */
    private Callback accessTokenRequestListener = new Callback<OauthAccessTokenResponse>() {
        @Override
        public void success(OauthAccessTokenResponse result, Response response) {

            if (result.access_token != null) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(UpPlatformSdkConstants.UP_PLATFORM_ACCESS_TOKEN, result.access_token);
                editor.putString(UpPlatformSdkConstants.UP_PLATFORM_REFRESH_TOKEN, result.refresh_token);
                editor.commit();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra(UpPlatformSdkConstants.CLIENT_SECRET, CLIENT_SECRET);
                getActivity().startActivity(intent);

                Log.e(TAG, "accessToken:" + result.access_token);
            } else {
                Log.e(TAG, "accessToken not returned by Oauth call, exiting...");
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG, "failed to get accessToken:" + retrofitError.getMessage());
        }
    };

    public static String getClientId() {
        return CLIENT_ID;
    }
    public static String getClientSecret() {
        return CLIENT_SECRET;
    }

    public static String getOauthCallbackUrl() {
        return OAUTH_CALLBACK_URL;
    }

    public static int getOauthRequestCode() {
        return OAUTH_REQUEST_CODE;
    }

    public Callback getAccessTokenRequestListener() {
        return accessTokenRequestListener;
    }

    /**
     *
     * @return
     */
    private static HashMap<String, Object> getTrendsRequestParams() {
        HashMap<String, Object> queryHashMap = new HashMap<String, Object>();

//        //uncomment to add as needed parameters
//        queryHashMap.put("end_date", "<insert-date>");
//        queryHashMap.put("bucket_size", 50);
//        queryHashMap.put("num_buckets", 10);

        return queryHashMap;
    }

    /**
     *
     */
    //TODO the callbacks are not yet backed by data model, but will get json response,
    //TODO which for now is logged to console
    private Callback genericCallbackListener = new Callback<Object>() {
        @Override
        public void success(Object o, Response response) {
            Log.e(TAG,  "api call successful, json output: " + o.toString());
            textView.setText(o.toString());
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Log.e(TAG,  "api call failed, error message: " + retrofitError.getMessage());
            textView.setText(retrofitError.getMessage());
        }
    };
}
