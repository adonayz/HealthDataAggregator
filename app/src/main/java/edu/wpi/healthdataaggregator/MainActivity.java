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

import it.sephiroth.android.library.tooltip.Tooltip;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, AuthenticationHandler, SwipeRefreshLayout.OnRefreshListener {

    private static final int ADAPTER_MODE = 1; // notifies adapter that it is generating cards for MainActivity. Not AddSourcesActivity.

    private ListView dataSourceList; // ListView that hold the cards
    private SourceBaseAdapter adapter; // Adapter that generates cards with data
    private static boolean isInSelectionMode; // Variable used to notify the application if the user has long clicked and is in selection mode.
    private ArrayList<Connector> selectedSources; // The sources selected by the user after entering selection mode
    private Toolbar toolbar;
    private TextView chooseTextView; // updates each time user selects sources
    private int counter; // variable used to store number of sources selected.
    private static LinkedList<Connector> sources; // Data sources the user has connected to the application.
    private RelativeLayout progressBarRelativeLayout; // layout that holds the progress bar
    private SwipeRefreshLayout mainSwipeRefreshLayout;
    private WebView loginWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        chooseTextView = (TextView) findViewById(R.id.select_source_text_view) ;
        loginWebview = (WebView) findViewById(R.id.invisibleWebView);
        progressBarRelativeLayout = (RelativeLayout) findViewById(R.id.mainActivityProgressBarLayout);
        mainSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.mainSwipeRefreshLayout);

        sources = new LinkedList<>();
        selectedSources = new ArrayList<Connector>();

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

        if(PreferencesManager.isFirstTime(this)){
            PreferencesManager.setToFirstTime(this);
        }

        if(sources.isEmpty()){
            createToolTip(fab, Tooltip.Gravity.TOP, getString(R.string.adding_hint));
        }
    }

    /**
     *
     */
    @Override
    public void onStart(){
        super.onStart();
        refreshSourceReadings();
    }

    /**
     * Method that gets executed when user comes back to activity after pausing the current activity.
     */
    @Override
    public void onResume(){
        super.onResume();
        refreshSourceReadings();
    }

    /**
     * Method that gets executed when the user pauses an activity to temporarily visit another one.
     */
    @Override
    public void onPause(){
        super.onPause();
        saveSources();
    }

    /**
     * Method that gets executed when the user exits an activity permanently
     */
    @Override
    public void onStop(){
        super.onStop();
        saveSources();
    }

    /**
     * This method is used to create the options/menu available on the toolbar
     * @param menu is the menu that gets inflated over the toolbar
     * @return true if menu is created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Used to specify what actions take place after a specific menu item is clicked by the user
     * @param item option that the user has chosen to click
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                //
                isInSelectionMode = false;
                adapter.notifyDataSetChanged();
                toolbar.getMenu().clear();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
                chooseTextView.setText(R.string.app_name);

                break;
            case R.id.action_settings:
                return true; // stub

        }
        // if none of the above were chosen, run the default version of the method
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that gets executed if the user clicks on an item in the application for a long time.
     * @param v view/item on screen that has been long clickeds
     * @return true if actions are executed succesfully
     */
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

    /**
     * This method is called from the SourceBaseAdapter class.
     * It is used to figure out which cards have been selected by the user.
     * It is also used to update the number of sources selected on the UI.
     * @param checkBox is the checkbox associated with the current source/card
     * @param position integer used to figure out which source/card has been chosen by the user
     */
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

    /**
     * Method used to update the number of sources selected on the UI.
     * @param count
     */
    public void updateTitle(int count){
        if (count == 0){
            chooseTextView.setText("0 data sources selected");
        }else{
            String selectedItemsTextDisplay = count + " source(s) selected";
            chooseTextView.setText(selectedItemsTextDisplay);
        }
    }

    /**
     * Helper method that informs the application if the user has started to select sources that the user wants to aggregates.
     * @return
     */
    public static boolean isInSelectionMode() {
        return isInSelectionMode;
    }

    /**
     * Is called when FitBit authentication has completed successfully
     * @param result
     */
    @Override
    public void onAuthFinished(AuthenticationResult result) {


    }

    /**
     * Loads sources that have been connected to the application by the user.
     * It uses data from SharedPreferences as reference.
     */
    public void loadSources(){
        sources.clear();
        for(SourceType sourceType: SourceType.values()){
            if(PreferencesManager.isSourceConnected(this, sourceType)){
                Connector connector = Connector.createConnector(sourceType, this, progressBarRelativeLayout);
                if(!connector.isConnected()){
                    connector.connect();
                }
                sources.add(connector);
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Used to get a Connector from the 'sources' LinkedList based on the sources type enumeration used as an input parameter.
     * @param sourceType enumeration used to specify what type of Connector is needed.
     * @return The specified source if it has been connected to the application. Returns null if source has not been connected or does not exist.
     */
    public static Connector getSource(SourceType sourceType){
        for(Connector connector: sources){
            if(connector.getSourceType() == sourceType){
                return connector;
            }
        }
        return null;
    }

    /**
     * Saves sources that have been connected to the application to shared preferences using their source type (enum).
     */
    public void saveSources(){
        for(Connector connector: sources){
            PreferencesManager.connectSource(this, connector.getSourceType(), connector.isConnected());
        }
    }

    /**
     * Attempts to pull new data from sources and updates the UI accordingly to display results.
     */
    public void refreshSourceReadings(){
        loadSources();
        //updateSourceListWithNewData();
        adapter.notifyDataSetChanged();
        mainSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Method that gets executed when user drags SwipeRefreshLayout downwards.
     */
    @Override
    public void onRefresh() {
        refreshSourceReadings();
    }

    /**
     * Removes the object that has the same subclass with the input from the sources LinkedList and replaces it with the input itself.
     * @param connector the object that replaces the removed object from the sources LinkedList
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

    /**
     * Updates the 'sources' LinkedList after it loads new data onto its elements calling
     * the loadHealthData() method on each of them.
     */
    private void updateSourceListWithNewData(){
        /*for(int i = 0; i < sources.size(); i++){
            Connector connector = sources.get(i);
            connector.loadHealthData();
            sources.remove(i);
            sources.add(i, connector);
        }*/
    }

    /**
     * Creates tooltips on the screen to guide the user through the application.
     * @param view View that the tooltip will be attached (pointing towards to).
     * @param gravity Specifies the position the tooltip will be placed relative to the attached view.
     * @param text The text that will be siplayed as a message on the tooltip.
     */
    public void createToolTip(View view, Tooltip.Gravity gravity, String text){
        Tooltip.make(this,
                new Tooltip.Builder(101)
                        .anchor(view, gravity)
                        .closePolicy(new Tooltip.ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(800)
                        .showDelay(300)
                        .text(text)
                        .maxWidth(700)
                        .withArrow(true)
                        .withOverlay(true)
                        .withStyleId(R.style.ToolTipLayoutCustomStyle)
                        .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                        .build()
        ).show();
    }

    public WebView getLoginWebview() {
        return loginWebview;
    }
}
