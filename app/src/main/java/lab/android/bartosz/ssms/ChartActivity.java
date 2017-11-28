package lab.android.bartosz.ssms;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;


import java.io.File;
import java.io.NotSerializableException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChartActivity extends AppCompatActivity implements OnSeekBarChangeListener, OnChartValueSelectedListener {


    ProgressDialog dialog;
    private LineChart mChart;
    //private SeekBar mSeekBarX, mSeekBarY;
    //private TextView tvX, tvY;
    boolean toast;
    //SensorDataDbHelper sensorDataDbHelper = new SensorDataDbHelper(this.getBaseContext());
    protected SensorService sensorService;
    //NsdHelper nsdHelper;
    protected boolean bounded = false;

    private ArrayList<SensorData> sensorDataArrayList;
    boolean temperature;


    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(this,SensorService.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
//        Bundle extras = getIntent().getExtras();
//        String date = extras.getString("date");
//        setData(date);
//        //mChart.notifyDataSetChanged();
//        mChart.invalidate();
//        mChart.animate();
//        mChart.animateX(3000);

//
//        mChart.animateX(2500);
//
//
//        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
//
//        // get the legend (only possible after setting data)
//        Legend l = mChart.getLegend();
//
//        // modify the legend ...
//        // l.setPosition(LegendPosition.LEFT_OF_CHART);
//        l.setForm(LegendForm.LINE);
//        l.setTypeface(tf);
//        l.setTextSize(18f);
//        l.setTextColor(Color.BLACK);
//        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
////        l.setYOffset(11f);
//
//        XAxis xAxis = mChart.getXAxis();
//
//        xAxis.setTypeface(tf);
//        xAxis.setTextSize(6f);
//        xAxis.setTextColor(Color.BLACK);
//        xAxis.setDrawGridLines(false);
//        xAxis.setDrawAxisLine(false);
//        xAxis.setSpaceBetweenLabels(10);
//        xAxis.setLabelRotationAngle(90);
//
//        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(tf);
//        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
//        leftAxis.setAxisMaxValue(100f);
//        leftAxis.setAxisMinValue(-30f);
//        leftAxis.setDrawGridLines(true);
//        leftAxis.setGranularityEnabled(true);
//
//
//        YAxis rightAxis = mChart.getAxisRight();
//        rightAxis.setTypeface(tf);
//        rightAxis.setTextColor(Color.RED);
//        rightAxis.setAxisMaxValue(100f);
//        rightAxis.setAxisMinValue(-30f);
//        rightAxis.setDrawGridLines(false);
//        rightAxis.setDrawZeroLine(false);
//        rightAxis.setGranularityEnabled(false);


        Log.e("DBG", "ON START");



        //sensorDataDbHelper = new SensorDataDbHelper(getApplicationContext());
        //nsdHelper = new NsdHelper(getApplicationContext());

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bounded) {
            unbindService(connection);
            bounded = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            bounded = true;
            //sensorService.setHelpers(nsdHelper, sensorDataDbHelper);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //sensorService.stopSearching();
            bounded = false;
        }
    };

    /*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        Bundle extrasBundle = getIntent().getExtras();
        if(extrasBundle!=null) {

            temperature = extrasBundle.getBoolean("temperature");
            sensorDataArrayList = (ArrayList<SensorData>) extrasBundle.getSerializable("list");
        }
        else
        {
            this.finishActivity(0);
        }

//        tvX = (TextView) findViewById(R.id.tvXMax);
//        tvY = (TextView) findViewById(R.id.tvYMax);
//        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
//        mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);
//
//        mSeekBarX.setProgress(45);
//        mSeekBarY.setProgress(100);
//
//        mSeekBarY.setOnSeekBarChangeListener(this);
//        mSeekBarX.setOnSeekBarChangeListener(this);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        // add data
        setData();


        mChart.animateX(2500);


        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextSize(18f);
        l.setTextColor(Color.BLACK);
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
//        l.setYOffset(11f);

        XAxis xAxis = mChart.getXAxis();

        xAxis.setTypeface(tf);
        xAxis.setTextSize(6f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(2);
        xAxis.setLabelRotationAngle(90);


        if (!temperature) {
            YAxis humidityAxis = mChart.getAxisLeft();
            humidityAxis.setTypeface(tf);
            humidityAxis.setTextColor(ColorTemplate.getHoloBlue());
            humidityAxis.setAxisMaxValue(100f);
            humidityAxis.setAxisMinValue(0f);
            humidityAxis.setDrawGridLines(true);
            humidityAxis.setGranularityEnabled(true);
            YAxis right = mChart.getAxisRight();
            right.setTypeface(tf);
            right.setTextColor(ColorTemplate.getHoloBlue());
            right.setAxisMaxValue(100f);
            right.setAxisMinValue(0f);
            right.setDrawGridLines(false);
            right.setGranularityEnabled(true);
        } else {

            YAxis temperatureAxis = mChart.getAxisLeft();
            temperatureAxis.setTypeface(tf);
            temperatureAxis.setTextColor(Color.RED);
            temperatureAxis.setAxisMaxValue(50f);
            temperatureAxis.setAxisMinValue(-30f);
            temperatureAxis.setDrawGridLines(true);
            temperatureAxis.setDrawZeroLine(true);
            temperatureAxis.setGranularityEnabled(false);
            YAxis right = mChart.getAxisRight();
            right.setTypeface(tf);
            right.setTextColor(Color.RED);
            right.setAxisMaxValue(50f);
            right.setAxisMinValue(-30f);
            right.setDrawGridLines(false);
            right.setDrawZeroLine(false);
            right.setGranularityEnabled(false);
        }
        Log.e("DBG", "ON CREATE");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        toast = prefs.getBoolean("show_toast",true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionToggleValues: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleHighlight: {
                if (mChart.getData() != null) {
                    mChart.getData().setHighlightEnabled(!mChart.getData().isHighlightEnabled());
                    mChart.invalidate();
                }
                break;
            }
            case R.id.actionToggleFilled: {

                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawFilledEnabled())
                        set.setDrawFilled(false);
                    else
                        set.setDrawFilled(true);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleCircles: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawCirclesEnabled())
                        set.setDrawCircles(false);
                    else
                        set.setDrawCircles(true);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleCubic: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.CUBIC_BEZIER);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleStepped: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.STEPPED
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.STEPPED);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleHorizontalCubic: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.HORIZONTAL_BEZIER);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionTogglePinch: {
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;
            }
            case R.id.animateX: {
                mChart.animateX(3000);
                break;
            }
            case R.id.animateY: {
                mChart.animateY(3000);
                break;
            }
            case R.id.animateXY: {
                mChart.animateXY(3000, 3000);
                break;
            }

            case R.id.actionSave: {
                Date now = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String str = dateFormat.format(now);
                //String date = "" + now.getYear()+"-"+now.getMonth()+"-"+now.getDay()+"_"+now.getHours()+"-"+now.getMinutes()+"-"+now.getSeconds();
                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "ssms");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {
                    // Do something on success
                    if (mChart.saveToPath("ssms-" + str, "/ssms/")) {
                        //mChart.saveToPath("test","/sdcard/")
                        if (toast)
                            Toast.makeText(getApplicationContext(), getString(R.string.save_success),
                                    Toast.LENGTH_SHORT).show();
                    } else if (toast)
                        Toast.makeText(getApplicationContext(), getString(R.string.save_failed), Toast.LENGTH_SHORT)
                                .show();
                } else {
                    // Do something else on failure
                }

                // mChart.saveToGallery("title"+System.currentTimeMillis())
                break;
            }
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        //tvX.setText("" + (mSeekBarX.getProgress() + 1));
        //tvY.setText("" + (mSeekBarY.getProgress()));

        //setData(mSeekBarX.getProgress() + 1, mSeekBarY.getProgress());
//        Bundle extras = getIntent().getExtras();
//        String date = extras.getString("date");
//        setData(date);
        //setData();
        // redraw
        mChart.invalidate();
    }


    private void setData() {


        ArrayList<String> xVals = new ArrayList<String>();
        /*for (int i = 0; i < count; i++) {
            xVals.add((i) + "");
        } */

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        /*
        for (int i = 0; i < count; i++) {
            float mult = range / 2f;
            float val = (float) (Math.random() * mult) + 50;// + (float)
            // ((mult *
            // 0.1) / 10);
            yVals1.add(new Entry(val, i));
        }

        */
        //ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        /*
        for (int i = 0; i < count; i++) {
            float mult = range;
            float val = (float) (Math.random() * mult) + 450;// + (float)
            // ((mult *
            // 0.1) / 10);
            yVals2.add(new Entry(val, i));
        }
        */


            List<SensorData> sensorDatas = sensorDataArrayList;
            int k = 0;
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        for (SensorData sensorData : sensorDatas) {

                String text = dateFormat.format(sensorData.getDate());
               /*
                if(sensorData.getDate().getHours()<10)
                {
                    text += "0"+sensorData.getDate().getHours();
                }
                else
                {
                    text += sensorData.getDate().getHours();
                }
                if(sensorData.getDate().getMinutes()<10)
                {
                    text += ":0" + sensorData.getDate().getMinutes();
                }
                else
                {
                    text += ":"+ sensorData.getDate().getMinutes();
                }
                if(sensorData.getDate().getSeconds()<10)
                {
                    text += ":0"+ sensorData.getDate().getSeconds();
                }
                else
                {
                    text += ":"+ sensorData.getDate().getSeconds();
                }
               */
                xVals.add(text);
                if(temperature) {
                    yVals1.add(new Entry(sensorData.getTemperature(), k));
                } else {
                    yVals1.add(new Entry(sensorData.getHumidity(), k));
                }
                k++;
            }

        LineDataSet set1; //, set2;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            //set2 = (LineDataSet)mChart.getData().getDataSetByIndex(1);
            set1.setYVals(yVals1);
            //set2.setYVals(yVals1);
            mChart.getData().setXVals(xVals);
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            if(temperature) {
                set1 = new LineDataSet(yVals1, getString(R.string.chart_temperature));

                set1.setAxisDependency(AxisDependency.LEFT);
                set1.setColor(Color.RED);
                set1.setCircleColor(Color.WHITE);
                set1.setLineWidth(2f);
                set1.setCircleRadius(3f);
                set1.setFillAlpha(65);
                set1.setFillColor(Color.RED);
                set1.setHighLightColor(Color.rgb(244, 117, 117));
                set1.setDrawCircleHole(false);
            }
            else {
                //set1.setFillFormatter(new MyFillFormatter(0f));
                //set1.setDrawHorizontalHighlightIndicator(false);
                //set1.setVisible(false);
                //set1.setCircleHoleColor(Color.WHITE);

                // create a dataset and give it a type
                set1 = new LineDataSet(yVals1, getString(R.string.chart_humidity));
                set1.setAxisDependency(AxisDependency.LEFT);
                set1.setColor(ColorTemplate.getHoloBlue());
                set1.setCircleColor(Color.WHITE);
                set1.setLineWidth(2f);
                set1.setCircleRadius(3f);
                set1.setFillAlpha(65);
                set1.setFillColor(ColorTemplate.getHoloBlue());
                set1.setDrawCircleHole(false);
                set1.setHighLightColor(Color.rgb(244, 117, 117));
            }
            //set2.setFillFormatter(new MyFillFormatter(900f));

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            //dataSets.add(set2);
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(9f);


            // set data
            mChart.setData(data);
        }
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());

        mChart.centerViewToAnimated(e.getXIndex(), e.getVal(), mChart.getData().getDataSetByIndex(dataSetIndex).getAxisDependency(), 500);
        //mChart.zoomAndCenterAnimated(2.5f, 2.5f, e.getXIndex(), e.getVal(), mChart.getData().getDataSetByIndex(dataSetIndex).getAxisDependency(), 1000);
        //mChart.zoomAndCenterAnimated(1.8f, 1.8f, e.getXIndex(), e.getVal(), mChart.getData().getDataSetByIndex(dataSetIndex).getAxisDependency(), 1000);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

}