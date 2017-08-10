package edu.wpi.healthdataaggregator;

import android.widget.TextView;

/**
 * Created by Adonay on 7/19/2017.
 */

public interface IConnector {
    void connect();
    void disconnect();
    void loadHealthData(TextView textView);
}
