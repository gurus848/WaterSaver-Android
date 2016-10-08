package com.example.gurusenthil.watersaver.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.gurusenthil.watersaver.R;
import com.example.gurusenthil.watersaver.ServerStuff.ServerRequests;
import com.example.gurusenthil.watersaver.Managers.WaterDataManager;
import com.example.gurusenthil.watersaver.DataModels.WaterRecordDataModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class WaterUsageQueryActivity extends Activity {

    private Button selectFromDateButton;
    private Button selectToDateButton;
    private Button queryButton;
    private TextView waterUsedView;
    private TextView unitLabel;

    protected static DateTime fromDate;
    protected static DateTime toDate;

    private Context context;

    public static int year;
    public static int month;
    public static int day;
    public static int hour;
    public static int minutes;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_usage_query);


        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Get Water Usage");
        toolbar.setTitleTextColor(Color.WHITE);

        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        selectFromDateButton = (Button) findViewById(R.id.fromDateTimeButton);
        selectToDateButton = (Button) findViewById(R.id.toDateTimeButton);
        queryButton = (Button) findViewById(R.id.queryButton);
        waterUsedView = (TextView) findViewById(R.id.queried_water_usage_label);
        unitLabel = (TextView) findViewById(R.id.unitLabel);

        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryServer();
            }
        });
        selectFromDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {showFromDateTimePicker();
            }
        });
        selectToDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToDateTimePicker();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showToDateTimePicker() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setDateTimeSetListener(new DatePickerFragment.DateTimeSetListener() {
            @Override
            public void dateTimeSelected() {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setDateTimeSetListener(new DatePickerFragment.DateTimeSetListener() {
                    @Override
                    public void dateTimeSelected() {
                        toDate = new DateTime(WaterUsageQueryActivity.year, WaterUsageQueryActivity.month,WaterUsageQueryActivity.day, WaterUsageQueryActivity.hour, WaterUsageQueryActivity.minutes, 0,0);
                        selectToDateButton.setText(toDate.toString());
                    }
                });
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void showFromDateTimePicker() {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setDateTimeSetListener(new DatePickerFragment.DateTimeSetListener() {
            @Override
            public void dateTimeSelected() {
                TimePickerFragment newFragment = new TimePickerFragment();
                newFragment.setDateTimeSetListener(new DatePickerFragment.DateTimeSetListener() {
                    @Override
                    public void dateTimeSelected() {
                        fromDate = new DateTime(WaterUsageQueryActivity.year, WaterUsageQueryActivity.month,WaterUsageQueryActivity.day, WaterUsageQueryActivity.hour, WaterUsageQueryActivity.minutes, 0,0);
                        selectFromDateButton.setText(fromDate.toString());
                    }
                });
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void queryServer() {
        if (fromDate != null && toDate!= null){
            long toTime = toDate.getMilliseconds(TimeZone.getDefault())/1000;
            long fromTime = fromDate.getMilliseconds(TimeZone.getDefault())/1000;

            waterUsedView.setText("Querying...");

            WaterDataManager waterDataManager = new WaterDataManager();
            waterDataManager.getWaterRecordsForTimePeriod(context, fromTime, toTime, new ServerRequests.WaterDataRequests.WaterDataRequestListener() {
                @Override
                public void dataReceived(ArrayList<WaterRecordDataModel> waterRecordDataModels, Integer index) {
                    double totalWaterUsage = 0;
                    for (WaterRecordDataModel waterRecordDataModel: waterRecordDataModels){
                        totalWaterUsage += waterRecordDataModel.getTotalWaterUsageOverRequiredTimePeriod();
                        Log.d("water_calculated", ""+waterRecordDataModel.getTotalWaterUsageOverRequiredTimePeriod());
                    }

                    NumberFormat formatter = new DecimalFormat("#0.0");
                    unitLabel.setVisibility(View.VISIBLE);
                    waterUsedView.setText(formatter.format(totalWaterUsage));
                }
            }, null);
        }else {
            Toast.makeText(context, "Set the dates and times for querying.", Toast.LENGTH_SHORT).show();
        }
    }

    public static class DatePickerFragment extends android.app.DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        public interface DateTimeSetListener {
            void dateTimeSelected();
        }

        private DateTimeSetListener dateTimeSetListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void setDateTimeSetListener(DateTimeSetListener dateTimeSetListener){
            this.dateTimeSetListener = dateTimeSetListener;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            WaterUsageQueryActivity.year = year;
            WaterUsageQueryActivity.month = month+1;
            WaterUsageQueryActivity.day = day;
            dateTimeSetListener.dateTimeSelected();
        }
    }

    public static class TimePickerFragment extends android.app.DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private DatePickerFragment.DateTimeSetListener dateTimeSetListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }


        public void setDateTimeSetListener(DatePickerFragment.DateTimeSetListener dateTimeSetListener){
            this.dateTimeSetListener = dateTimeSetListener;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            WaterUsageQueryActivity.hour = hourOfDay;
            WaterUsageQueryActivity.minutes = minute;
            dateTimeSetListener.dateTimeSelected();
        }
    }

}
