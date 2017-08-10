package edu.wpi.healthdataaggregator;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Adonay on 7/19/2017.
 */

public class IHealthConnector extends Connector {

    public IHealthConnector(AppCompatActivity activity, RelativeLayout progressBarLayout){
        super(SourceType.IHEALTH, activity, progressBarLayout);
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
        this.setHealthData("TEST");
    }

}
