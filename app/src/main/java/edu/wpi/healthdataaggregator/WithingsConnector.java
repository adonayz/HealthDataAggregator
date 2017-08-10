package edu.wpi.healthdataaggregator;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Adonay on 8/9/2017.
 */

public class WithingsConnector extends Connector {
    final String URL_BASE = "https://wbsapi.withings.net/measure";
    final String URL_ACTION = "?action=getmeas";
    final String URL_USERID = "&userid=10706169";
    final String URL_OAUTH_CONSUMER_KEY = "&oauth_consumer_key=6d97254d677d31bc7781930df84b37212c4e90016117dd54cd9edec6ec";
    final String URL_OAUTH_NONCE = "&oauth_nonce=70d4c03622482ddc1d42ad40d95ac4ca";
    final String URL_OAUTH_SIGNATURE = "&oauth_signature=JsvwKDgNdT1CsTocHWZ6prDZ%2F98%3D";
    final String URL_OAUTH_SIGNATURE_METHOD = "&oauth_signature_method=HMAC-SHA1";
    final String URL_OAUTH_TIMESTAMP = "&oauth_timestamp=1472349100";
    final String URL_OAUTH_TOKEN = "&oauth_token=9be95634668b178f5397ccc56bb3d52e808492f5f0cbe08895b64674f9e6c";
    final String URL_OAUTH_VERSION = "&oauth_version=1.0";

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
    }

    @Override
    public void disconnect() {
        setConnected(false);
        getProgressBarLayout().setVisibility(View.GONE);
    }

    @Override
    public void loadHealthData(TextView textView) {

    }
}
