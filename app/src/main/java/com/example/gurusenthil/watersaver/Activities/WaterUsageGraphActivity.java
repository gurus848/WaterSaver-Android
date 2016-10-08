package com.example.gurusenthil.watersaver.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;


import com.example.gurusenthil.watersaver.DataModels.WaterRecordDataModel;
import com.example.gurusenthil.watersaver.Managers.WaterDataManager;
import com.example.gurusenthil.watersaver.R;
import com.example.gurusenthil.watersaver.ServerStuff.ServerRequests;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class WaterUsageGraphActivity extends Activity {

    private BarChart hourlyUsageChart;
    private Context context;
    private static int numHoursDataReceived;
    private Handler handler = new Handler();
    private DateTime now;

    private final ArrayList<BarEntry> hourlyBarEntries = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_usage_graph);

        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Water Usage Graphs");
        toolbar.setTitleTextColor(Color.WHITE);
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        hourlyUsageChart = (BarChart) findViewById(R.id.hourlyUsageChart);
        initializeCharts();


    }

    private void initializeCharts() {
        //Initializing hourly chart.
        initializeHourlyBarChart();

    }

    Runnable getHourlyDataFromServer = new Runnable() {
        @Override
        public void run() {
            int minutes = now.getMinute();
            int seconds = now.getSecond();
            DateTime startOfHour = now.minus(0, 0, 0, 0, minutes, seconds, 0, DateTime.DayOverflow.Spillover);
            DateTime endOfHour = startOfHour.plus(0, 0, 0, 1, 0, 0, 0, DateTime.DayOverflow.Spillover);
            long startOfHourEpoch = startOfHour.getMilliseconds(TimeZone.getDefault())/1000;
            long endOfHourEpoch = endOfHour.getMilliseconds(TimeZone.getDefault())/1000;
            //Log.d("test", "start of hour calculated: "+startOfHourEpoch+" End of hour calculated: "+endOfHourEpoch);
            WaterDataManager waterDataManager = WaterDataManager.getWaterDataManager();
            waterDataManager.getWaterRecordsForTimePeriod(context, startOfHourEpoch, endOfHourEpoch, new ServerRequests.WaterDataRequests.WaterDataRequestListener() {
                @Override
                public void dataReceived(ArrayList<WaterRecordDataModel> waterRecordDataModels, Integer index) {
                    numHoursDataReceived++;
                    double totalWaterUsed = 0;
                    for (WaterRecordDataModel waterRecordDataModel: waterRecordDataModels){
                        totalWaterUsed += waterRecordDataModel.getTotalWaterUsageOverRequiredTimePeriod();
                    }
                    //Log.d("test", "Hour: "+getHourlyChartXValues().get(24-numHoursDataReceived)+" Water Usage: "+totalWaterUsed);
                    NumberFormat formatter = new DecimalFormat("#0.0");
                    BarEntry barEntry = new BarEntry(Float.parseFloat(formatter.format(totalWaterUsed)), 24-numHoursDataReceived);
                    hourlyBarEntries.add(barEntry);
                    if (numHoursDataReceived < 24){
                        getHourlyDataFromServer.run();
                    }else {
                        showChart.run();
                    }
                }
            }, null);
            now = now.minus(0, 0, 0, 1, 0, 0, 0, DateTime.DayOverflow.Spillover);
        }
    };

    Runnable showChart = new Runnable() {
        @Override
        public void run() {

            BarDataSet barDataSet = new BarDataSet(hourlyBarEntries, "Water Usage (L)");
            BarData barData = new BarData(getHourlyChartXValues(), barDataSet);
            hourlyUsageChart.setData(barData);
            hourlyUsageChart.setDescription("Hourly Usage (L)");
            hourlyUsageChart.animateXY(1000, 1000);
            hourlyUsageChart.invalidate();

        }
    };

    private void initializeHourlyBarChart() {
        numHoursDataReceived = 0;
        now = DateTime.now(TimeZone.getDefault());
        getHourlyDataFromServer.run();
    }

    private ArrayList<String> getHourlyChartXValues() {
        ArrayList<String> xAxisValues = new ArrayList<>();

        DateTime now = DateTime.now(TimeZone.getDefault());
        now = now.minus(0, 0, 0, 23, 0, 0, 0, DateTime.DayOverflow.Spillover);
        for (int i = 0;i < 24; i++){
            Integer hour = now.getHour();
            String toAdd = "";
            if (hour > 12 && hour < 24){
                toAdd += (hour-12)+" PM";
            }else if (hour > 0 && hour < 12){
                toAdd += hour + " AM";
            }else if (hour == 12){
                toAdd = "12 PM";
            }else if (hour == 0){
                toAdd = "12 AM";
            }
            xAxisValues.add(toAdd);
            now = now.plus(0, 0, 0, 1, 0, 0, 0, DateTime.DayOverflow.Spillover);
        }
        return xAxisValues;
    }

}
