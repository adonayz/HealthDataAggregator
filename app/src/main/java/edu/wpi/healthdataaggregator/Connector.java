package edu.wpi.healthdataaggregator;

import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

/**
 * Created by Adonay on 7/19/2017.
 */

public abstract class Connector implements IConnector{

    private SourceType sourceType;
    private boolean isConnected;
    private String TAG;
    private AppCompatActivity activity;
    private RelativeLayout progressBarLayout;
    private String profileInfo;
    private String data;

    Connector(SourceType sourceType, AppCompatActivity activity, RelativeLayout progressBarLayout) {
        this.sourceType = sourceType;
        this.TAG = sourceType.getTAG();
        this.activity = activity;
        this.progressBarLayout = progressBarLayout;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceType.getName();
    }

    public String getMessage() {
        return sourceType.getMessage();
    }

    public int getSourceID(){
        return sourceType.getSourceID();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getTAG() {
        return TAG;
    }

    public AppCompatActivity getActivity() {
        return activity;
    }

    public RelativeLayout getProgressBarLayout() {
        return progressBarLayout;
    }

    public String getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(String profileInfo) {
        this.profileInfo = profileInfo;
    }

    public String getHealthData() {
        return data;
    }

    public void setHealthData(String data) {
        this.data = data;
    }
}
