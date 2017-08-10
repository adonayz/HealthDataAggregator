package edu.wpi.healthdataaggregator;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Adonay on 7/19/2017.
 */

public class PreferencesManager {
    private static final String MY_PREFERENCES = "CONNECTED_SOURCES";
    private static final String IS_FIRST_TIME_PREFERENCE = "IS_FIRST_TIME";

    /**
     *
     * @param context
     * @param sourceType
     * @return
     */
    public static boolean isSourceConnected(Context context, SourceType sourceType){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return reader.getBoolean(sourceType.getName(), false);
    }

    /**
     *
     * @param context
     * @param sourceType
     * @param isConnected
     */
    public static void connectSource(Context context, SourceType sourceType, boolean isConnected){
        final SharedPreferences reader = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean(sourceType.getName(), isConnected);
        editor.commit();
    }

    /**
     *
     * @param context
     * @return
     */
    public static boolean isFirstTime(Context context){
        final SharedPreferences reader = context.getSharedPreferences(IS_FIRST_TIME_PREFERENCE, Context.MODE_PRIVATE);
        final boolean first = reader.getBoolean("is_first", true);
        return first;
    }

    /**
     *
     * @param context
     */
    public static void setToFirstTime(Context context){
        final SharedPreferences reader = context.getSharedPreferences(IS_FIRST_TIME_PREFERENCE, Context.MODE_PRIVATE);

        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("is_first", false);
        editor.commit();

    }
}
