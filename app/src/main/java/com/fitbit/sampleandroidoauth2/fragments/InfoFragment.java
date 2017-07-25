package com.fitbit.sampleandroidoauth2.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fitbit.api.loaders.ResourceLoaderResult;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

import edu.wpi.healthdataaggregator.R;

/**
 * Created by jboggess on 10/18/16.
 */

public abstract class InfoFragment<T> extends Fragment implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<T>>, SwipeRefreshLayout.OnRefreshListener {

    protected final String TAG = getClass().getSimpleName();
    private TextView titleText;
    private RelativeLayout relativeLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_info, container, false);

        titleText = (TextView) view.findViewById(R.id.titleText);
        relativeLayout = (RelativeLayout) view.findViewById(R.id.infoProgressRelativeLayout) ;
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout) ;
        webView = (WebView) view.findViewById(R.id.webview);

        titleText.setText(getTitleResourceId());
        setMainText(getActivity().getString(R.string.no_data));
        swipeRefreshLayout.setOnRefreshListener(this);
        relativeLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();

    }

    @Override
    public void onLoadFinished(Loader<ResourceLoaderResult<T>> loader, ResourceLoaderResult<T> data) {
        swipeRefreshLayout.setRefreshing(false);
        relativeLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        switch (data.getResultType()) {
            case ERROR:
                Toast.makeText(getActivity(), R.string.error_loading_data, Toast.LENGTH_LONG).show();
                break;
            case EXCEPTION:
                Log.e(TAG, "Error loading data", data.getException());
                Toast.makeText(getActivity(), R.string.error_loading_data, Toast.LENGTH_LONG).show();
                break;
        }
    }

    public abstract int getTitleResourceId();

    protected abstract int getLoaderId();

    @Override
    public void onLoaderReset(Loader<ResourceLoaderResult<T>> loader) {
        //no-op
    }


    @Override
    public void onRefresh() {
        getLoaderManager().getLoader(getLoaderId()).forceLoad();
    }


    private String formatNumber(Number number) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number);
    }

    private boolean isImageUrl(String string) {
        return (string.startsWith("http") &&
                (string.endsWith("jpg")
                        || string.endsWith("gif")
                        || string.endsWith("png")));
    }

    protected void printKeys(StringBuilder stringBuilder, Object object) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(new Gson().toJson(object));
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                if (!(value instanceof JSONObject)
                        && !(value instanceof JSONArray)) {
                    stringBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;<b>");
                    stringBuilder.append(key);
                    stringBuilder.append(":</b>&nbsp;");
                    if (value instanceof Number) {
                        stringBuilder.append(formatNumber((Number) value));
                    } else if (isImageUrl(value.toString())) {
                        stringBuilder.append("<br/>");
                        stringBuilder.append("<center><img src=\"");
                        stringBuilder.append(value.toString());
                        stringBuilder.append("\" width=\"150\" height=\"150\"></center>");
                    } else {
                        stringBuilder.append(value.toString());
                    }
                    stringBuilder.append("<br/>");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void setMainText(String text) {
        webView.loadData(text, "text/html", "UTF-8");
    }

}
