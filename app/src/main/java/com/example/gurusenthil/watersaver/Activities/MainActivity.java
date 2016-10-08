package com.example.gurusenthil.watersaver.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gurusenthil.watersaver.R;
import com.example.gurusenthil.watersaver.ServerStuff.ServerRequests;
import com.example.gurusenthil.watersaver.Managers.WaterDataManager;
import com.example.gurusenthil.watersaver.DataModels.WaterRecordDataModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class MainActivity extends Activity {

    private TextView currentWaterUsageView;
    private TextView todayTotalWaterUsageView;
    final String waterUsageTopic = "waterUsage";
    final int qos = 0;
    private long lastDataReceivedTime = 0;
    long fiveSecondsInMillis = 5000;
    long oneMinuteInMillis = 60000;
    private Handler mHandler;
    private Context context;
    private Button querySpecificWaterUsageButton;
    private Button waterUsageGraphsButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        ServerRequests.init(context);

        currentWaterUsageView = (TextView) findViewById(R.id.current_water_usage_label);
        todayTotalWaterUsageView = (TextView) findViewById(R.id.today_total_water_usage_label);
        querySpecificWaterUsageButton = (Button) findViewById(R.id.specific_water_usage_button);
        querySpecificWaterUsageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent queryWaterUsageActivity = new Intent(context, WaterUsageQueryActivity.class);
                startActivity(queryWaterUsageActivity);
            }
        });
        waterUsageGraphsButton = (Button)findViewById(R.id.waterUsageGraphsButton);
        waterUsageGraphsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent waterUsageGraphs = new Intent(context, WaterUsageGraphActivity.class);
                startActivity(waterUsageGraphs);
            }
        });

        mHandler = new Handler();
        startUpdatingWaterUsageToday();


    }

    @Override
    protected void onResume() {
        super.onResume();

        connectToMQTTServer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(updateWaterUsageToday);
    }

    private void connectToMQTTServer() {
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), "tcp://10.0.0.3:1883", clientId);

        //Setting the callbacks for the MQTT client.
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.d("MQTT Client", "Lost connection to server.");
                Toast.makeText(MainActivity.this, "Lost Connection.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //Log.d("MQTT Client", "Message Arrived topic: " + topic + " Message: " + message.toString());
                if (topic.equals(waterUsageTopic)) {
                    String waterUsageString = message.toString();
                    float waterUsage = Float.parseFloat(waterUsageString);

                    NumberFormat formatter = new DecimalFormat("#0.0");

                    currentWaterUsageView.setText(formatter.format(waterUsage));

                    lastDataReceivedTime = System.currentTimeMillis();


                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if ((System.currentTimeMillis() - lastDataReceivedTime) > 1900){
                                currentWaterUsageView.setText("0.0");
                            }
                        }
                    }, fiveSecondsInMillis);


                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        //Connecting to MQTT server.
        try {
            client.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("MQTT Client", "Successfully connected to MQTT server.");
                    Toast.makeText(MainActivity.this, "Successfully connected.", Toast.LENGTH_SHORT).show();

                    try {
                        client.subscribe(waterUsageTopic, qos, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d("MQTT Client", "Successfully subscribed to mqtt server.");
                                Toast.makeText(MainActivity.this, "Successfully subscribed", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                Log.d("MQTT Client", "Could not successfully subscribe to mqtt server.");
                                Toast.makeText(MainActivity.this, "Could not subscribe.", Toast.LENGTH_SHORT).show();

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTT Client", "Could not successfully connect to MQTT server.");
                    Toast.makeText(MainActivity.this, "Could not successfully connect", Toast.LENGTH_SHORT).show();

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    Runnable updateWaterUsageToday = new Runnable() {
        @Override
        public void run() {

            try {
                DateTime now = DateTime.now(TimeZone.getDefault());
                final long toTime = now.getMilliseconds(TimeZone.getDefault())/1000;

                DateTime startOfDay = now.getStartOfDay();
                final long fromTime = startOfDay.getMilliseconds(TimeZone.getDefault())/1000;

                Log.d("testing", "fromTimeCalculated: "+fromTime+" toTimeCalculated: "+toTime);

                WaterDataManager waterDataManager = new WaterDataManager();
                waterDataManager.getWaterRecordsForTimePeriod(context, fromTime, toTime, new ServerRequests.WaterDataRequests.WaterDataRequestListener() {
                    @Override
                    public void dataReceived(ArrayList<WaterRecordDataModel> waterRecordDataModels, Integer index) {
                        double totalWaterUsageToday = 0;
                        for (WaterRecordDataModel waterRecordDataModel: waterRecordDataModels){

                            totalWaterUsageToday += waterRecordDataModel.getTotalWaterUsageOverRequiredTimePeriod();

                        }

                        NumberFormat formatter = new DecimalFormat("#0.0");
                        todayTotalWaterUsageView.setText(formatter.format(totalWaterUsageToday));

                    }
                }, null);
            }finally {
                mHandler.postDelayed(updateWaterUsageToday, oneMinuteInMillis);
            }

        }
    };
    private void startUpdatingWaterUsageToday() {

        updateWaterUsageToday.run();
    }
}
