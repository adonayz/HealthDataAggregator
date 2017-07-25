package edu.wpi.healthdataaggregator;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Adonay on 7/19/2017.
 */

public class PreferencesManager {
    private static final String MY_PREFERENCES = "CONNECTED_SOURCES";

    public boolean isGoogleFitConnected(Context context){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return reader.getBoolean("googlefit", false);
    }

    public void setGoogleFitConnected(Context context, boolean isConnected){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("googlefit", isConnected);
        editor.commit();
    }

    public boolean isFitBitConnected(Context context){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return reader.getBoolean("fitbit", false);
    }

    public void setFitBitConnected(Context context, boolean isConnected){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("fitbit", isConnected);
        editor.commit();
    }

    public boolean isIHealthConnected(Context context){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return reader.getBoolean("ihealth", false);
    }

    public void setIHealthConnected(Context context, boolean isConnected){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("ihealth", isConnected);
        editor.commit();
    }
}
