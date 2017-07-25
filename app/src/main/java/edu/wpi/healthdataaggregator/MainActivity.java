package edu.wpi.healthdataaggregator;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fitbit.authentication.AuthenticationHandler;
import com.fitbit.authentication.AuthenticationResult;

import java.util.ArrayList;
import java.util.LinkedList;

import static edu.wpi.healthdataaggregator.SourceType.FITBIT;
import static edu.wpi.healthdataaggregator.SourceType.GOOGLEFIT;
import static edu.wpi.healthdataaggregator.SourceType.IHEALTH;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, AuthenticationHandler, SwipeRefreshLayout.OnRefreshListener {

    private static final int ADAPTER_MODE = 1;

    private ListView dataSourceList;
    private SourceBaseAdapter adapter;
    private static boolean isInSelectionMode;
    private ArrayList<Connector> selectedSources;
    private Toolbar toolbar;
    private TextView chooseTextView;
    private int counter;
    private static LinkedList<Connector> sources;
    private WebView loginWebview;
    private RelativeLayout progressBarRelativeLayout;
    private SwipeRefreshLayout mainSwipeRefreshLayout;

    private PreferencesManager preferencesManager;

    private GoogleFitConnector googleFitConnector = null;
    private FitBitConnector fitBitConnector = null;
    private IHealthConnector iHealthConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        preferencesManager = new PreferencesManager();

        chooseTextView = (TextView) findViewById(R.id.select_source_text_view) ;
        loginWebview = (WebView) findViewById(R.id.invisibleWebView);
        progressBarRelativeLayout = (RelativeLayout) findViewById(R.id.mainActivityProgressBarLayout);
        mainSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainSwipeRefreshLayout);

        sources = new LinkedList<>();
        selectedSources = new ArrayList<Connector>();

        loadSources();

        mainSwipeRefreshLayout.setOnRefreshListener(this);
       // mainSwipeRefreshLayout.setVisibility(View.GONE);
        progressBarRelativeLayout.setVisibility(View.GONE);

        isInSelectionMode =false;
        counter = 0;

        dataSourceList = (ListView) findViewById(R.id.source_list);

        adapter = new SourceBaseAdapter(MainActivity.this, sources, ADAPTER_MODE);
        dataSourceList.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addSourceIntent = new Intent(MainActivity.this, AddSourcesActivity.class);
                startActivity(addSourceIntent);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();

        refreshSourceReadings();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadSources();
        refreshSourceReadings();
    }

    @Override
    public void onPause(){
        super.onPause();
        saveSources();
    }

    @Override
    public void onStop(){
        super.onStop();
        saveSources();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:

                isInSelectionMode = false;
                adapter.notifyDataSetChanged();
                toolbar.getMenu().clear();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                chooseTextView.setText(R.string.app_name);

                break;
            case R.id.action_settings:
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onLongClick(View v) {
        chooseTextView.setText(R.string.select_sources_header);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.contextual_menu);
        chooseTextView.setVisibility(View.VISIBLE);
        isInSelectionMode = true;
        adapter.notifyDataSetChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    public void prepareSelection(CheckBox checkBox, int position){

        if(checkBox.isChecked()){
            selectedSources.add(sources.get(position));
            counter+=1;
            updateTitle(counter);
        }else{
            selectedSources.remove(sources.get(position));
            counter-=1;
            updateTitle(counter);
        }
    }

    public void updateTitle(int count){
        if (count == 0){
            chooseTextView.setText("0 data sources selected");
        }else{
            String selectedItemsTextDisplay = count + " source(s) selected";
            chooseTextView.setText(selectedItemsTextDisplay);
        }
    }

    public static boolean isInSelectionMode() {
        return isInSelectionMode;
    }


    @Override
    public void onAuthFinished(AuthenticationResult result) {


    }

    public void loadSources(){
        if(preferencesManager.isGoogleFitConnected(this) && (getSource(GOOGLEFIT.getName()) == null)){
            googleFitConnector = new GoogleFitConnector(this, progressBarRelativeLayout);
            googleFitConnector.connect();
            sources.add(googleFitConnector);
        }
        if(preferencesManager.isFitBitConnected(this) && (getSource(FITBIT.getName()) == null)){
            fitBitConnector = new FitBitConnector(this, progressBarRelativeLayout);
            fitBitConnector.connect();
            sources.add(fitBitConnector);
        }
        if(preferencesManager.isIHealthConnected(this) && (getSource(IHEALTH.getName()) == null)){

        }
    }

    public static Connector getSource(String name){
        for(Connector connector: sources){
            if(connector.getSourceName().equals(name)){
                return connector;
            }
        }
        return null;
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

    public void refreshSourceReadings(){
        adapter.notifyDataSetChanged();
        for(Connector connector: sources){
            connector.loadHealthData();
        }
        adapter.notifyDataSetChanged();
        mainSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        refreshSourceReadings();
    }
}
