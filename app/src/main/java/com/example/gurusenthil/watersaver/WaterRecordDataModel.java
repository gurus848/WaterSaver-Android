package com.example.gurusenthil.watersaver;

import android.util.Log;

/**
 * Created by GuruSenthil on 10/6/16.
 */

public class WaterRecordDataModel {

    private int id;
    private long startTimeEpoch;
    private long stopTimeEpoch;
    private float averageWaterFlowRate;
    private double totalWaterUsage;

    //constant for json model received
    public static final String idKey = "id";
    public static final String startTimeEpochKey = "startTimeEpoch";
    public static final String stopTimeEpochKey = "stopTimeEpoch";
    public static final String averageFlowRateKey = "averageWaterFlowRate";

    public WaterRecordDataModel(int id, long startTimeEpoch, long stopTimeEpoch, float averageWaterFlowRate){
        this.id = id;
        this.startTimeEpoch = startTimeEpoch;
        this.stopTimeEpoch = stopTimeEpoch;
        this.averageWaterFlowRate = averageWaterFlowRate;
        this.totalWaterUsage = (((double)stopTimeEpoch - (double)startTimeEpoch)/(double)3600)*averageWaterFlowRate;
    }

    public int getId() {
        return id;
    }

    public long getStartTimeEpoch() {
        return startTimeEpoch;
    }

    public long getStopTimeEpoch() {
        return stopTimeEpoch;
    }

    public float getAverageWaterFlowRate() {
        return averageWaterFlowRate;
    }

    public double getTotalWaterUsage() {
        return totalWaterUsage;
    }
}
