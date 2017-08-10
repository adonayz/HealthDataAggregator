package edu.wpi.healthdataaggregator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.fitbit.authentication.AuthenticationManager;
import com.jawbone.upplatformsdk.api.ApiManager;
import com.jawbone.upplatformsdk.utils.UpPlatformSdkConstants;

import java.util.LinkedList;

import static edu.wpi.healthdataaggregator.GoogleFitConnector.checkGoogleFitPermissions;
import static edu.wpi.healthdataaggregator.GoogleFitConnector.requestGoogeFitPermisions;
import static edu.wpi.healthdataaggregator.SourceType.FITBIT;
import static edu.wpi.healthdataaggregator.SourceType.JAWBONE;

public class AddSourcesActivity extends AppCompatActivity{

    private static final int ADAPTER_MODE = 2;

    private ListView dataSourceList;
    private SourceBaseAdapter adapter;
    private LinkedList<Connector> sources;
    private static SourceType isConnectingSource = null;
    private RelativeLayout addingProgressBarLayout;
    private WebView loginWebview;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sources);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.add_sources_title));

        addingProgressBarLayout = (RelativeLayout) findViewById(R.id.addingProgressBarLayout);
        loginWebview = (WebView) findViewById(R.id.invisibleWebView);

        sources = new LinkedList<>();
        loadSources();
        saveSources();

        addingProgressBarLayout.setVisibility(View.GONE);

        dataSourceList = (ListView) findViewById(R.id.add_source_list);
        adapter = new SourceBaseAdapter(AddSourcesActivity.this, sources, ADAPTER_MODE);
        dataSourceList.setAdapter(adapter);
    }

    /**
     *
     * @param v
     * @param position
     * @param connector
     */
    public void chooseSource(View v, int position, Connector connector){
        isConnectingSource =connector.getSourceType();
        addingProgressBarLayout.setVisibility(View.VISIBLE);
        if(!connector.isConnected()){
            switch (isConnectingSource){
                case GOOGLEFIT:
                    if(!checkGoogleFitPermissions(this)) {
                        requestGoogeFitPermisions(this);
                    }
                    connector.connect();
                    break;
                default:
                    connector.connect();
                    break;
            }

        }else{
            connector.disconnect();
        }

        updateSourceListItem(connector);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        onBackPressed();
        return true;

    }

    /**
     *
     */
    @Override
    public void onBackPressed(){
        saveSources();
        super.onBackPressed();
        this.finish();
    }

    /**
     *
     */
    @Override
    protected void onResume(){
        super.onResume();

        addingProgressBarLayout.setVisibility(View.GONE);

        if(isConnectingSource != null){
            switch (isConnectingSource){
                case FITBIT:
                    if (AuthenticationManager.isLoggedIn()) {
                        FitBitConnector fitBitConnector = (FitBitConnector) getSource(FITBIT);
                        fitBitConnector.onFitBitLoggedIn();
                        updateSourceListItem(fitBitConnector);
                    }
            }
            isConnectingSource = null;
        }

        saveSources();
    }

    /**
     *
     */
    private void saveSources(){
        for(Connector connector: sources){
            PreferencesManager.connectSource(this, connector.getSourceType(), connector.isConnected());
        }
    }

    /**
     *
     */
    private void loadSources(){
        sources.clear();
        for(SourceType sourceType: SourceType.values()){
            Connector connector;
            if((PreferencesManager.isSourceConnected(this, sourceType)) && (MainActivity.getSource(sourceType) != null)){
                connector = MainActivity.getSource(sourceType);
            }else{
                connector = Connector.createConnector(sourceType, this, addingProgressBarLayout);
            }
            sources.add(connector);
        }
    }

    /**
     *
     */
    @Override
    protected void onPause(){
        super.onPause();
        saveSources();
    }

    /**
     *
     */
    @Override
    protected void onStop(){
        super.onStop();
        isConnectingSource = null;
        saveSources();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

        if(isConnectingSource == FITBIT){
            FitBitConnector fitBitConnector = (FitBitConnector) getSource(FITBIT);
            if (!AuthenticationManager.onActivityResult(requestCode, resultCode, data, fitBitConnector)) {
                fitBitConnector.setConnected(false);
            }

            updateSourceListItem(fitBitConnector);
        }

        if(isConnectingSource == JAWBONE){

            JawboneConnector jawboneConnector = (JawboneConnector) getSource(JAWBONE);

            updateSourceListItem(jawboneConnector);

            if(resultCode == RESULT_OK) {
                if (requestCode == JawboneConnector.getOauthRequestCode()) {
                    String code = data.getStringExtra(UpPlatformSdkConstants.ACCESS_CODE);
                    if (code != null) {
                        //first clear older accessToken, if it exists..
                        ApiManager.getRequestInterceptor().clearAccessToken();

                        ApiManager.getRestApiInterface().getAccessToken(
                                JawboneConnector.getClientId(),
                                JawboneConnector.getClientSecret(),
                                code,
                                jawboneConnector.getAccessTokenRequestListener());
                    }
                }
            }
        }

    }

    /**
     *
     * @param sourceType
     * @return
     */
    private Connector getSource(SourceType sourceType){
        for(Connector connector: sources){
            if(connector.getSourceType() == sourceType){
                return connector;
            }
        }
        return null;
    }

    /**
     *
     * @param connector
     */
    private void updateSourceListItem(Connector connector){
        for(int i = 0; i < sources.size(); i++){
            if(sources.get(i).getSourceType() == connector.getSourceType()){
                sources.remove(i);
                sources.add(i, connector);
            }
        }
        adapter.notifyDataSetChanged();
        saveSources();
    }
}
