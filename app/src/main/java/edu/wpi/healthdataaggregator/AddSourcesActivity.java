package edu.wpi.healthdataaggregator;

import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fitbit.authentication.AuthenticationManager;

import java.util.LinkedList;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.LAYER_TYPE_NONE;
import static edu.wpi.healthdataaggregator.GoogleFitConnector.checkGoogleFitPermissions;
import static edu.wpi.healthdataaggregator.GoogleFitConnector.requestGoogeFitPermisions;
import static edu.wpi.healthdataaggregator.SourceType.FITBIT;
import static edu.wpi.healthdataaggregator.SourceType.GOOGLEFIT;
import static edu.wpi.healthdataaggregator.SourceType.IHEALTH;

public class AddSourcesActivity extends AppCompatActivity{

    private static final String TAG = "AddSourcesActivity";
    private static final int GOOGLEFIT_ID = 1;
    private static final int FITBIT_ID = 2;
    private static final int IHEALTH_ID = 3;
    private static final int ADAPTER_MODE = 2;

    private ListView dataSourceList;
    private SourceBaseAdapter adapter;
    private LinkedList<Connector> sources;
    private static int isConnectingSource = 0;
    private RelativeLayout addingProgressBarLayout;
    private GoogleFitConnector googleFitConnector = null;
    private FitBitConnector fitBitConnector = null;
    private IHealthConnector iHealthConnector = null;
    private PreferencesManager preferencesManager;
    private WebView loginWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sources);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.add_sources_title));

        addingProgressBarLayout = (RelativeLayout) findViewById(R.id.addingProgressBarLayout);
        loginWebview = (WebView) findViewById(R.id.invisibleWebView);

        preferencesManager = new PreferencesManager();

        googleFitConnector = new GoogleFitConnector(this, addingProgressBarLayout);
        fitBitConnector = new FitBitConnector(this, addingProgressBarLayout);
        iHealthConnector = new IHealthConnector(this, addingProgressBarLayout);

        sources = new LinkedList<>();

        sources.add(googleFitConnector);
        sources.add(fitBitConnector);
        sources.add(iHealthConnector);

        loadSources();
        saveSources();

        addingProgressBarLayout.setVisibility(View.GONE);

        dataSourceList = (ListView) findViewById(R.id.add_source_list);
        adapter = new SourceBaseAdapter(AddSourcesActivity.this, sources, ADAPTER_MODE);
        dataSourceList.setAdapter(adapter);
    }

    public void chooseSource(View v, int position, Connector connector){
        isConnectingSource = position+1;
        addingProgressBarLayout.setVisibility(View.VISIBLE);
        if(!connector.isConnected()){
            switch (isConnectingSource){
                case GOOGLEFIT_ID:
                    if(!checkGoogleFitPermissions(this)) {
                        requestGoogeFitPermisions(this);
                    }
                    googleFitConnector.connect();
                    break;
                case FITBIT_ID:
                    fitBitConnector.connect();
                    break;
                case IHEALTH_ID:
                    iHealthConnector.connect();
                    break;
            }

        }else{
            switch (isConnectingSource){
                case GOOGLEFIT_ID:
                    googleFitConnector.disconnect();
                    break;
                case FITBIT_ID:
                    fitBitConnector.disconnect();
                    break;
                case IHEALTH_ID:
                    iHealthConnector.disconnect();
                    break;
            }
        }

        Connector Connector = sources.get(position);
        sources.remove(position);
        sources.add(position, Connector);

        adapter.notifyDataSetChanged(ADAPTER_MODE);
        saveSources();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        finish();
        return true;

    }

    // Code from http://blog.bradcampbell.nz/greyscale-views-on-android/
    public void setGreyscale(View v, boolean greyscale) {
        if (greyscale) {
            // Create a paint object with 0 saturation (black and white)
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            Paint greyscalePaint = new Paint();
            greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
            // Create a hardware layer with the greyscale paint
            v.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
        } else {
            // Remove the hardware layer
            v.setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        addingProgressBarLayout.setVisibility(View.GONE);

        if(isConnectingSource == GOOGLEFIT.getSourceID()){

        }else if(isConnectingSource == FITBIT.getSourceID()){
            if (AuthenticationManager.isLoggedIn()) {
                fitBitConnector.onFitBitLoggedIn();
            }
        }else if(isConnectingSource == IHEALTH.getSourceID()){

        }
        isConnectingSource = 0;

        loadSources();

        adapter.notifyDataSetChanged(ADAPTER_MODE);
    }

    public void saveSources(){
        for(Connector connector: sources){
            if(connector.getSourceName().equals(GOOGLEFIT.getName())){
                preferencesManager.setGoogleFitConnected(this, connector.isConnected());
            }else if(connector.getSourceName().equals(FITBIT.getName())){
                preferencesManager.setFitBitConnected(this, connector.isConnected());
            }else if(connector.getSourceName().equals(IHEALTH.getName())){
                preferencesManager.setIHealthConnected(this, connector.isConnected());
            }
        }
    }

    public void loadSources(){
        if(preferencesManager.isGoogleFitConnected(this)){
            if(MainActivity.getSource(GOOGLEFIT.getName()) != null){
                googleFitConnector = (GoogleFitConnector) MainActivity.getSource(GOOGLEFIT.getName());
                sources.remove(0);
                sources.add(0, googleFitConnector);
            }
        }
        if(preferencesManager.isFitBitConnected(this)){
            if(MainActivity.getSource(FITBIT.getName()) != null){
                fitBitConnector = (FitBitConnector) MainActivity.getSource(FITBIT.getName());
                sources.remove(1);
                sources.add(1, fitBitConnector);
            }
        }
        if(preferencesManager.isIHealthConnected(this)){

        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        saveSources();
    }

    @Override
    protected void onStop(){
        super.onStop();
        isConnectingSource = 0;
        saveSources();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         *  4. When the Login UI finishes, it will invoke the `onActivityResult` of this activity.
         *  We call `AuthenticationManager.onActivityResult` and set ourselves as a login listener
         *  (via AuthenticationHandler) to check to see if this result was a login result. If the
         *  result code matches login, the AuthenticationManager will process the login request,
         *  and invoke our `onAuthFinished` method.
         *
         *  If the result code was not a login result code, then `onActivityResult` will return
         *  false, and we can handle other onActivityResult result codes.
         *
         */

        if(isConnectingSource == FITBIT.getSourceID()){
            if (!AuthenticationManager.onActivityResult(requestCode, resultCode, data, fitBitConnector)) {
                // Handle other activity results, if needed
            }
        }

    }
}
