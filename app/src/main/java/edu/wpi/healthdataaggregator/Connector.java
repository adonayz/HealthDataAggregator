package edu.wpi.healthdataaggregator;

import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

/**
 * Created by Adonay on 7/19/2017.
 */

public abstract class Connector implements IConnector{

    private SourceType sourceType;
    private boolean isConnected;
    protected String TAG;
    private AppCompatActivity activity;
    private RelativeLayout progressBarLayout;
    private String profileInfo;
    private String data;

    /**
     *
     * @param sourceType
     * @param activity
     * @param progressBarLayout
     */
    Connector(SourceType sourceType, AppCompatActivity activity, RelativeLayout progressBarLayout) {
        this.sourceType = sourceType;
        this.TAG = sourceType.getTAG();
        this.activity = activity;
        this.progressBarLayout = progressBarLayout;
        this.isConnected = false;
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


    /**
     *
     * @param sourceType
     * @param activity
     * @param progressBarLayout
     * @return
     */
    public static Connector createConnector(SourceType sourceType, AppCompatActivity activity, RelativeLayout progressBarLayout){
        switch (sourceType){
            case GOOGLEFIT:
                return new GoogleFitConnector(activity, progressBarLayout);
            case FITBIT:
                return new FitBitConnector(activity, progressBarLayout);
            case IHEALTH:
                return new IHealthConnector(activity, progressBarLayout);
            case JAWBONE:
                return new JawboneConnector(activity, progressBarLayout);
            case WITHINGS:
                return new WithingsConnector(activity, progressBarLayout);
            default:
                return null;
        }
    }
}
