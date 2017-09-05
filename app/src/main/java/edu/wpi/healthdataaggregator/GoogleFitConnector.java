package edu.wpi.healthdataaggregator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

/**
 * Created by Adonay on 7/17/2017.
 */

public class GoogleFitConnector extends Connector{

    private static final String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BODY_SENSORS};

    private static final int PERMISSION_REQUEST_CODE = 1337;

    private static GoogleApiClient mClient = null;
    private static OnDataPointListener mListener = null;

    private static int id_count = 0;

    /**
     *
     * @param activity
     * @param progressBarLayout
     */
    public GoogleFitConnector(AppCompatActivity activity, RelativeLayout progressBarLayout){
        super(SourceType.GOOGLEFIT, activity, progressBarLayout);
    }

    @Override
    public void connect(){
        setConnected(true);
        buildGoogleClient();
    }

    /**
     *
     */
    public void buildGoogleClient(){
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Fitness.SENSORS_API)
                    .addApi(Fitness.RECORDING_API)
                    .addApi(Fitness.HISTORY_API)
                    .addApi(Fitness.CONFIG_API)
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                    .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                    .addScope(new Scope(Scopes.FITNESS_NUTRITION_READ_WRITE))
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i(TAG, "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    findGooglefitDataSources();
                                    setProfileInfo("Adonay Resom");
                                    getProgressBarLayout().setVisibility(View.GONE);
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(TAG,
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(getActivity(), id_count, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(TAG, "Google Play services connection failed. Cause: " +
                                    result.toString());
                        }
                    })
                    .build();
            mClient.connect();
        }

    }

    /**
     *
     * @param activity
     * @return
     */
    public static boolean checkGoogleFitPermissions(AppCompatActivity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param activity
     */
    public static void requestGoogeFitPermisions(AppCompatActivity activity){
        activity.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    /**
     *
     */
    private void findGooglefitDataSources(){
        recordDataToHistory(DataType.TYPE_STEP_COUNT_DELTA);
        //loadHealthData();
        listGoogleFitSubscriptions(DataType.TYPE_STEP_COUNT_DELTA);
    }


    /**
     *
     * @param dataType
     */
    public void recordDataToHistory(DataType dataType){
        Fitness.RecordingApi.subscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(TAG, "There was a problem subscribing.");
                        }
                    }
                });
    }

    /**
     *
     * @param textView
     */
    public void loadHealthData(TextView textView){
        readDailyData(DataType.TYPE_STEP_COUNT_DELTA, textView);
    }

    /**
     *
     * @param dataType
     * @param textView
     */
    public void readDailyData(DataType dataType, final TextView textView){
        PendingResult<DailyTotalResult> pendingResult = Fitness.HistoryApi.readDailyTotal(mClient, dataType);
        Log.d(TAG, "AWAITING RESULT");
        pendingResult.setResultCallback(new ResultCallback<DailyTotalResult>() {
            @Override
            public void onResult(@NonNull DailyTotalResult dailyTotalResult) {
                if(dailyTotalResult.getStatus().isSuccess()){
                    Log.d(TAG, "RESULT SUCCESSFUL");
                    DataSet totalSet = dailyTotalResult.getTotal();
                    /*long total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
                    Log.d(TAG, "TOTAL STEPS: " + total);*/
                    dumpDataSet(totalSet, textView);
                }
            }
        });
    }

    /**
     *
     * @param dataSet
     * @param textView
     */
    private void dumpDataSet(DataSet dataSet, TextView textView) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        Log.i(TAG, "amount: " + dataSet.getDataPoints().size());
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.d(TAG, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                this.setHealthData("steps = " + dp.getValue(field));
                textView.setText("steps = " + dp.getValue(field));
            }
        }
    }

    /**
     *
     * @param dataType
     */
    public void listGoogleFitSubscriptions(DataType dataType){
        Log.d(TAG, "LISTING SUBSCRIPTIONS");
        Fitness.RecordingApi.listSubscriptions(mClient, dataType)
                // Create the callback to retrieve the list of subscriptions asynchronously.
                .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            Log.d(TAG, "LISTING SUCCESSFUL");
                            DataType dt = sc.getDataType();
                            Log.i(TAG, "Active subscription for data type: " + dt.getName());
                        }
                    }
                });
    }

    /**
     *
     * @param dataType
     */
    public void unsubscribeFromSubscriptions(final DataType dataType){
        Fitness.RecordingApi.unsubscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Successfully unsubscribed for data type: " + dataType.toString());
                        } else {
                            // Subscription not removed
                            Log.i(TAG, "Failed to unsubscribe for data type: " + dataType.toString());
                        }
                    }
                });
    }

    @Override
    public void disconnect(){

        if(!mClient.isConnected()){
            mClient.connect();
        }

        id_count++;

        unsubscribeFromSubscriptions(DataType.TYPE_STEP_COUNT_DELTA);

        Fitness.SensorsApi.remove(
                mClient,
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });

        PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(mClient);

        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){

                }
            }
        });

        if (mClient != null && mClient.isConnected()) {
            mClient.clearDefaultAccountAndReconnect().setResultCallback(new ResultCallback<Status>() {

                @Override
                public void onResult(Status status) {

                    mClient.disconnect();
                }
            });

        }

        setConnected(false);
    }
}
