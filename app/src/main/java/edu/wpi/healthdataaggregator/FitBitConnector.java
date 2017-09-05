package edu.wpi.healthdataaggregator;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fitbit.api.loaders.ResourceLoaderResult;
import com.fitbit.api.models.DailyActivitySummary;
import com.fitbit.api.models.User;
import com.fitbit.api.models.UserContainer;
import com.fitbit.api.services.ActivityService;
import com.fitbit.api.services.UserService;
import com.fitbit.authentication.AuthenticationConfiguration;
import com.fitbit.authentication.AuthenticationConfigurationBuilder;
import com.fitbit.authentication.AuthenticationHandler;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.AuthenticationResult;
import com.fitbit.authentication.ClientCredentials;
import com.fitbit.authentication.Scope;

import java.util.Date;
import java.util.Set;

import static com.fitbit.authentication.Scope.activity;

/**
 * Created by Adonay on 7/19/2017.
 */

public class FitBitConnector extends Connector implements AuthenticationHandler {

    /**
     * FitBitConnector Constructor
     * @param activity
     * @param progressBarLayout
     */
    public FitBitConnector(AppCompatActivity activity, RelativeLayout progressBarLayout) {
        super(SourceType.FITBIT, activity, progressBarLayout);
    }

    public void connect(){
        AuthenticationManager.configure(getActivity(), generateAuthenticationConfiguration(getActivity(), AddSourcesActivity.class));
        fitBitLogin();
        setConnected(true);
        setProfileInfo(PreferencesManager.getFitbitProfileInfo(getActivity()));
    }

    /**
     *
     * @param context
     * @param addSourcesActivityClass
     * @return
     */
    public AuthenticationConfiguration generateAuthenticationConfiguration(Context context, Class<AddSourcesActivity> addSourcesActivityClass) {

        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;

            String clientId = bundle.getString("CLIENT_ID");
            String redirectUrl = bundle.getString("REDIRECT_URL");
            String CLIENT_SECRET = bundle.getString("CLIENT_SECRET");
            String SECURE_KEY = bundle.getString("SECURE_KEY");


            ClientCredentials CLIENT_CREDENTIALS = new ClientCredentials(clientId, CLIENT_SECRET, redirectUrl);

            return new AuthenticationConfigurationBuilder()

                    .setClientCredentials(CLIENT_CREDENTIALS)
                    .setEncryptionKey(SECURE_KEY)
                    .setTokenExpiresIn(2592000L) // 30 days
                    .setBeforeLoginActivity(new Intent(context, addSourcesActivityClass))
                    .addRequiredScopes(Scope.profile, Scope.settings)
                    .addOptionalScopes(activity, Scope.weight)
                    .addOptionalScopes(activity, Scope.heartrate)
                    .addOptionalScopes(activity, Scope.location)
                    .addOptionalScopes(activity, Scope.sleep)
                    .setLogoutOnAuthFailure(true)

                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    public void onFitBitLoggedIn() {
        Log.d(getTAG(), "LOGGED IN");
        getProgressBarLayout().setVisibility(View.GONE);

        Loader<ResourceLoaderResult<UserContainer>> loaderResultLoader = UserService.getLoggedInUserLoader(getActivity());
        loaderResultLoader.registerListener(1235, new Loader.OnLoadCompleteListener<ResourceLoaderResult<UserContainer>>() {
            @Override
            public void onLoadComplete(Loader<ResourceLoaderResult<UserContainer>> loader, ResourceLoaderResult<UserContainer> userContainerResourceLoaderResult) {
                if(userContainerResourceLoaderResult.isSuccessful()){
                    User user = userContainerResourceLoaderResult.getResult().getUser();
                    String profileInfo = "Full Name: " + user.getFullName() + "\n" +
                            "Age: " + user.getAge() + "\nGender: " + user.getGender() + "\nWeight: " +
                            user.getWeight() + user.getWeightUnit();
                    PreferencesManager.setFitbitProfileInfo(getActivity(), profileInfo);
                }
            }
        });

        /*
        Intent intent = UserDataActivity.newIntent(getActivity());
        getActivity().startActivity(intent);*/

        //loadHealthData();
    }

    /**
     *
     */
    public void fitBitLogin() {
        /**
         *  3. Call login to show the login UI
         */
        /*if(AuthenticationManager.isLoggedIn()){
            onFitBitLoggedIn();
        }else{
            AuthenticationManager.login(getActivity());
        }*/
        AuthenticationManager.login(getActivity());
    }

    /**
     *
     * @param authenticationResult
     */
    public void onAuthFinished(AuthenticationResult authenticationResult) {
        getProgressBarLayout().setVisibility(View.GONE);

        /**
         * 5. Now we can parse the auth response! If the auth was successful, we can continue onto
         *      the next activity. Otherwise, we display a generic error message here
         */
        if (authenticationResult.isSuccessful()) {
            onFitBitLoggedIn();
        } else {
            displayAuthError(authenticationResult);
        }
    }

    /**
     *
     * @param authenticationResult
     */
    private void displayAuthError(AuthenticationResult authenticationResult) {
        String message = "";

        switch (authenticationResult.getStatus()) {
            case dismissed:
                message = getActivity().getString(R.string.fitbit_login_dismissed);
                break;
            case error:
                message = authenticationResult.getErrorMessage();
                break;
            case missing_required_scopes:
                Set<Scope> missingScopes = authenticationResult.getMissingScopes();
                String missingScopesText = TextUtils.join(", ", missingScopes);
                message = getActivity().getString(R.string.missing_scopes_error) + missingScopesText;
                break;
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.login_title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create()
                .show();
    }


    @Override
    public void disconnect(){
        AuthenticationManager.logout(getActivity());
        setConnected(false);
        getProgressBarLayout().setVisibility(View.GONE);
    }

    /**
     *
     * @param textView
     */
    public void loadHealthData(final TextView textView){
        Loader<ResourceLoaderResult<DailyActivitySummary>> loaderResultLoader = ActivityService.getDailyActivitySummaryLoader(getActivity(), new Date());
        loaderResultLoader.registerListener(1234, new Loader.OnLoadCompleteListener<ResourceLoaderResult<DailyActivitySummary>>() {
            @Override
            public void onLoadComplete(Loader<ResourceLoaderResult<DailyActivitySummary>> loader, ResourceLoaderResult<DailyActivitySummary> dailyActivitySummaryResourceLoaderResult) {
                if(dailyActivitySummaryResourceLoaderResult.isSuccessful()){
                    String data = "";
                    Log.d(getTAG(), "minutes = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getSedentaryMinutes());
                    setHealthData("sedentary minutes = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getSedentaryMinutes().toString());
                    data += "Sedentary Minutes = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getSedentaryMinutes().toString() + "\n";
                    data += "Steps = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getSteps().toString() + "\n";
                    data += "Distance = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getDistances().get(0).getDistance() + "\n";
                    data += "Floors = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getElevation() + "\n";
                    data += "Lightly Active Minutes = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getLightlyActiveMinutes() + "\n";
                    data += "Fairly Active Minutes = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getFairlyActiveMinutes() + "\n";
                    data += "Heavily Active Minutes = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getVeryActiveMinutes() + "\n";
                    data += "Activity Calories = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getActivityCalories().toString() + "\n";
                    data += "Active Score = " + dailyActivitySummaryResourceLoaderResult.getResult().getSummary().getActiveScore().toString() + "\n";
                    textView.setText(data);
                    Log.d(getTAG(), "from test " + getHealthData());
                }
            }
        });
        loaderResultLoader.forceLoad();
    }
}
