package edu.wpi.healthdataaggregator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.LinkedList;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.LAYER_TYPE_NONE;

class SourceBaseAdapter extends BaseAdapter {

    private LinkedList<Connector> results;
    private LayoutInflater inflater;
    private Context context;
    private int mode; // mode = 1 for cards in MainActivity, mode = 2 for cards in AddSourcesActivity
    private Connector currentSource;
    private Holder holder;

    /**
     *
     * @param context
     * @param results
     * @param mode
     */
    SourceBaseAdapter(Context context, LinkedList<Connector> results, int mode) {
        this.results = results;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.mode = mode;

    }

    /**
     *
     * @return
     */
    @Override
    public int getCount() {
        return results.size();
    }

    /**
     *
     * @param position
     * @return
     */
    @Override
    public Connector getItem(int position) {
        return results.get(position);
    }

    /**
     *
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        currentSource = results.get(position);

        if(convertView == null){
            if(mode == 1){
                convertView = inflater.inflate(R.layout.data_source_item, parent, false);
                holder = new Holder(convertView, this.context);
                holder.progressBarLayout = (RelativeLayout) convertView.findViewById(R.id.cardProgressRelativeLayout);
                holder.progressBarLayout.setVisibility(View.GONE);
            }else{
                convertView = inflater.inflate(R.layout.add_source_item, parent, false);
                holder = new Holder(convertView, this.context);
            }
            convertView.setTag(holder);
        }else{
            holder = (Holder) convertView.getTag();
        }

        holder.position = position;
        holder.connector = currentSource;
        holder.connector.getProgressBarLayout().setVisibility(View.GONE);

        holder.sourceLogo = (ImageView) convertView.findViewById(R.id.source_logo);

        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(currentSource.getSourceName().toLowerCase() + "_logo", "drawable",
                context.getPackageName());

        holder.sourceLogo.setImageResource(resourceId);

        holder.resultName = (TextView) convertView.findViewById(R.id.source_name);

        if(currentSource.isConnected()){
            holder.resultName.setText(currentSource.getSourceName() + " - CONNECTED");
        }else{
            holder.resultName.setText(currentSource.getSourceName());
        }
        holder.resultDescription = (TextView) convertView.findViewById(R.id.health_data);

        String description = "";
        if(mode == 1){
            Log.d("ADAPTER", "Should print " + currentSource.getHealthData());
            currentSource.loadHealthData(holder.resultDescription);
        }else{
            if(currentSource.isConnected()){
                description = currentSource.getProfileInfo();
            }else{
                description = currentSource.getMessage();
            }

            holder.resultDescription.setText(description);
        }

        holder.cardView = (CardView) convertView.findViewById(R.id.card_view);


        // set which cards are clickable (now only cards from mode 1 or MainActivity are)
        if (mode == 1) {
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.selectSourceCheckBox);
            if(MainActivity.isInSelectionMode()){
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.cardView.setOnClickListener(holder);
            }else{
                holder.checkBox.setVisibility(View.GONE);
                holder.checkBox.setChecked(false);
                holder.cardView.setOnClickListener(null);
            }
            holder.cardView.setOnLongClickListener((MainActivity) this.context);
            holder.checkBox.setOnClickListener(holder);
        }else{

            holder.button = (ToggleButton) convertView.findViewById(R.id.connectButton);
            holder.button.setOnClickListener(holder);

            if(currentSource.isConnected()){
                holder.button.setChecked(true);
                setGreyscale(convertView, false);
            }else{
                holder.button.setChecked(false);
                setGreyscale(convertView, true);
            }

            /*final View finalConvertView = convertView;
            holder.button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        setGreyscale(finalConvertView, false);
                    } else {
                        setGreyscale(finalConvertView, true);
                    }
                }
            });*/

        }
        return convertView;
    }

    /**
     *
     */
    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView sourceLogo;
        TextView resultName;
        TextView resultDescription;
        CheckBox checkBox;
        ToggleButton button;
        CardView cardView;
        Context activity;
        RelativeLayout progressBarLayout;
        Connector connector;

        int position;

        /**
         *
         * @param view
         * @param activity
         */
        Holder(View view, Context activity){
            super(view);
            this.activity = activity;
        }

        /**
         *
         * @param v
         */
        @Override
        public void onClick(View v){

            if(mode == 1){
                MainActivity mainActivity = (MainActivity) activity;

                // If statement used to make clicking on card view also check and uncheck the checkboxes
                if(v.getClass().equals(CardView.class)){
                    if(checkBox.isChecked()){
                        checkBox.setChecked(false);
                    }else{
                        checkBox.setChecked(true);
                    }
                }
                mainActivity.prepareSelection(checkBox,position);
            }else{
                AddSourcesActivity addSourcesActivity = (AddSourcesActivity) activity;
                addSourcesActivity.chooseSource(v, position, connector);
            }

        }
    }


    /*private class LoadHealthDataTask extends AsyncTask<SourceBaseAdapter, Void, SourceBaseAdapter> {

        @Override
        protected void onPreExecute(){
            holder.progressBarLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected SourceBaseAdapter doInBackground(SourceBaseAdapter... sourceBaseAdapters) {
            currentSource.loadHealthData();
            return sourceBaseAdapters[0];
        }

        @Override
        protected void onPostExecute(SourceBaseAdapter adapter) {
            holder.resultDescription.setText(currentSource.getHealthData());
            holder.progressBarLayout.setVisibility(View.GONE);
        }
    }
*/

    /**
     *
     * @param v
     * @param greyscale
     */
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

}
