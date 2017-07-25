package edu.wpi.healthdataaggregator;

import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Adonay on 7/19/2017.
 */

public class IHealthConnector extends Connector {

    public IHealthConnector(AddSourcesActivity addSourcesActivity, RelativeLayout progressBarLayout){
        super(SourceType.IHEALTH, addSourcesActivity, progressBarLayout);
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
    public void loadHealthData() {

    }

}
