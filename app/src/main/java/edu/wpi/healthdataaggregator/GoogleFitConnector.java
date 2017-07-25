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
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
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

    public GoogleFitConnector(AppCompatActivity activity, RelativeLayout progressBarLayout){
        super(SourceType.GOOGLEFIT, activity, progressBarLayout);
    }

    @Override
    public void connect(){
        setConnected(true);
        buildGoogleClient();
    }

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
                                    Log.i(getTAG(), "Connected!!!");
                                    // Now you can make calls to the Fitness APIs.
                                    findGooglefitDataSources();
                                    getProgressBarLayout().setVisibility(View.GONE);
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i(getTAG(), "Connection lost.  Cause: Network Lost.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i(getTAG(),
                                                "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(getActivity(), id_count, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i(getTAG(), "Google Play services connection failed. Cause: " +
                                    result.toString());
                        }
                    })
                    .build();
            mClient.connect();
        }

    }

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

    public static void requestGoogeFitPermisions(AppCompatActivity activity){
        activity.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    private void findGooglefitDataSources(){
        recordDataToHistory(DataType.TYPE_STEP_COUNT_DELTA);
        loadHealthData();
        listGoogleFitSubscriptions(DataType.TYPE_STEP_COUNT_DELTA);
    }

    private void getLiveDataFromSensors(){

        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                // At least one datatype must be specified.
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                // Can specify whether data type is raw or derived.
                .setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        Log.i(getTAG(), "Result: " + dataSourcesResult.getStatus().toString());
                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Log.i(getTAG(), "Data source found: " + dataSource.toString());
                            Log.i(getTAG(), "Data Source type: " + dataSource.getDataType().getName());

                            //Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                    && mListener == null) {
                                Log.i(getTAG(), "Data source for LOCATION_SAMPLE found!  Registering.");
                                registerGoogleFitDataListener(dataSource,
                                        DataType.TYPE_STEP_COUNT_CUMULATIVE);
                            }
                        }
                    }
                });

    }


    private void registerGoogleFitDataListener(DataSource dataSource, com.google.android.gms.fitness.data.DataType dataType){

        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);

                    Log.d(getTAG(), "Value is: " + val.toString());

                    Log.i(getTAG(), "Detected DataPoint field: " + field.getName());
                    Log.i(getTAG(), "Detected DataPoint value: " + val);
                }
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource) // Optional but recommended for custom data sets.
                        .setDataType(dataType) // Can't be omitted.
                        .setSamplingRate(1, TimeUnit.SECONDS)
                        .build(),
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(getTAG(), "Listener registered!");
                        } else {
                            Log.i(getTAG(), "Listener not registered.");
                        }
                    }
                });
    }

    public void recordDataToHistory(DataType dataType){
        Fitness.RecordingApi.subscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(getTAG(), "Existing subscription for activity detected.");
                            } else {
                                Log.i(getTAG(), "Successfully subscribed!");
                            }
                        } else {
                            Log.i(getTAG(), "There was a problem subscribing.");
                        }
                    }
                });
    }

    public void loadHealthData(){
        readDailyData(DataType.TYPE_STEP_COUNT_DELTA);
    }

    public void readDailyData(DataType dataType){
        PendingResult<DailyTotalResult> pendingResult = Fitness.HistoryApi.readDailyTotal(mClient, dataType);
        Log.d(getTAG(), "AWAITING RESULT");
        pendingResult.setResultCallback(new ResultCallback<DailyTotalResult>() {
            @Override
            public void onResult(@NonNull DailyTotalResult dailyTotalResult) {
                if(dailyTotalResult.getStatus().isSuccess()){
                    Log.d(getTAG(), "RESULT SUCCESSFUL");
                    DataSet totalSet = dailyTotalResult.getTotal();
                    /*long total = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(FIELD_STEPS).asInt();
                    Log.d(getTAG(), "TOTAL STEPS: " + total);*/
                    dumpDataSet(totalSet);
                }
            }
        });
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(getTAG(), "Data returned for Data type: " + dataSet.getDataType().getName());
        Log.i(getTAG(), "amount: " + dataSet.getDataPoints().size());
        DateFormat dateFormat = getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(getTAG(), "Data point:");
            Log.i(getTAG(), "\tType: " + dp.getDataType().getName());
            Log.i(getTAG(), "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(getTAG(), "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.d(getTAG(), "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                this.setHealthData("steps = " + dp.getValue(field));
            }
        }
    }

    public void listGoogleFitSubscriptions(DataType dataType){
        Log.d(getTAG(), "LISTING SUBSCRIPTIONS");
        Fitness.RecordingApi.listSubscriptions(mClient, dataType)
                // Create the callback to retrieve the list of subscriptions asynchronously.
                .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                        for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                            Log.d(getTAG(), "LISTING SUCCESSFUL");
                            DataType dt = sc.getDataType();
                            Log.i(getTAG(), "Active subscription for data type: " + dt.getName());
                        }
                    }
                });
    }

    public void unsubscribeFromSubscriptions(final DataType dataType){
        Fitness.RecordingApi.unsubscribe(mClient, dataType)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i(getTAG(), "Successfully unsubscribed for data type: " + dataType.toString());
                        } else {
                            // Subscription not removed
                            Log.i(getTAG(), "Failed to unsubscribe for data type: " + dataType.toString());
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
                            Log.i(getTAG(), "Listener was removed!");
                        } else {
                            Log.i(getTAG(), "Listener was not removed.");
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
