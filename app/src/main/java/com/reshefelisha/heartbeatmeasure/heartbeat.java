package com.reshefelisha.heartbeatmeasure;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

public class heartbeat extends Activity {

    TextView heartbeatText;
    TextView heartbeatAverageText;
    Queue<Double> btQ;
    Queue<Double> avgQ;
    LineChart lineChart;
    RelativeLayout bodyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartbeat);

        Pubnub pubnub = new Pubnub("pubKey", "subKey");

        heartbeatText = (TextView) findViewById(R.id.TXTHeartbeat);
        heartbeatAverageText = (TextView) findViewById((R.id.TXTAvgHeartbeat));
        lineChart = (LineChart) findViewById(R.id.LNCHRTChart);
        bodyLayout = (RelativeLayout) findViewById(R.id.RLLYTBody);

        btQ = new Queue<>(60); //BPM values Queue
        avgQ = new Queue<>(60); //15 beat average values Queue

        setupChart(lineChart);

        try {
            pubnub.subscribe("hrtbt", new Callback() { //subscribe to pubnub channel "hrtbt"
                public void successCallback(String channel, Object message) { //on success
                    hbtText(message);
                }
                public void errorCallback(String channel, PubnubError error) { //on failure
                    System.out.println("PN: " + error.getErrorString());
                }
            });
        } catch (PubnubException e) {
            e.printStackTrace();
        }
    }

    public void hbtText(Object message){
        try {
            final double bt = Double.parseDouble(new JSONObject(message.toString()).getString("hbt")); //sent heartbeat
            final double avg = Double.parseDouble(new JSONObject(message.toString()).getString("avg")); //sent average (calculated server-side)
            btQ.add(bt); //add values to Queues
            avgQ.add(avg);
            drawChart(); //draw chart with new values
            runOnUiThread(new Runnable() { //update text fields with new values
                @Override
                public void run() {
                    heartbeatText.setText(bt+"");
                    heartbeatAverageText.setText(avg+"");
                }
            });
            if(avg<40) { //ALERT
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bodyLayout.setBackgroundColor(Color.RED); //we'll just turn the background red this time.
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bodyLayout.setBackgroundColor(Color.WHITE); //turn it back white when heart rate goes up again.
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setupChart(LineChart chart) {

        chart.setDescription("");
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setNoDataText("");

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaxValue(150f); //max BPM expected is around 120, so 150 is a decent top limit
        leftAxis.setAxisMinValue(0f);
        chart.getAxisRight().setEnabled(false); //turn off left axis

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < 60; i++) xVals.add(""); //create a list of empty strings for the X-Axis

        LineDataSet beats = new LineDataSet(new ArrayList<Entry>(), "BPM");
        beats.setColor(Color.parseColor("#23AAD7"));
        beats.setLineWidth(1f);
        beats.setDrawCircles(false);
        beats.setDrawValues(false);
        beats.setDrawCubic(true);
        beats.setFillAlpha(0);

        LineDataSet average = new LineDataSet(new ArrayList<Entry>(), "Average 15");
        average.setColor(Color.parseColor("#AB24DB"));
        average.setLineWidth(1f);
        average.setDrawCircles(false);
        average.setDrawValues(false);
        average.setDrawCubic(true);
        average.setFillAlpha(0);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(beats);
        dataSets.add(average);
        LineData data = new LineData(xVals, dataSets);
        chart.setData(data);
    }

    private void drawChart() {
        lineChart.getLineData().getDataSetByIndex(0).clear(); //clear out existing lines
        lineChart.getLineData().getDataSetByIndex(1).clear();
        for (int i = 0; i < btQ.size(); i++) {//beats data set
            lineChart.getLineData().getDataSetByIndex(0).addEntry(new Entry((float) btQ.get(i).doubleValue(), i)); //redraw them
            lineChart.getLineData().getDataSetByIndex(1).addEntry(new Entry((float) avgQ.get(i).doubleValue(), i)); //with updated Queues
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lineChart.notifyDataSetChanged();
                lineChart.invalidate(); //invalidate() triggers a redraw of the chart.
            }
        });
    }
}

class Queue<E> extends LinkedList<E> {
    private int length; //Queue length

    public Queue(int length) {
        this.length = length;
    }
    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > length) { super.remove(); } //if size is bigger than specified length drop oldest value
        return true;
    }
}