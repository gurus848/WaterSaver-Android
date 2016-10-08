package com.example.gurusenthil.watersaver.DataModels;


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

    public WaterRecordDataModel(int id, long startTimeEpoch, long stopTimeEpoch, float averageWaterFlowRate, long requiredFromTime, long requiredToTime){
        this.id = id;
        this.startTimeEpoch = startTimeEpoch;
        this.stopTimeEpoch = stopTimeEpoch;
        this.averageWaterFlowRate = averageWaterFlowRate;
        if (startTimeEpoch < requiredFromTime || stopTimeEpoch > requiredToTime){
            if (startTimeEpoch < requiredFromTime && stopTimeEpoch <= requiredToTime){
                this.totalWaterUsage = getHours(requiredFromTime, stopTimeEpoch)*averageWaterFlowRate;
            }else if (startTimeEpoch >= requiredFromTime && stopTimeEpoch > requiredToTime){
                this.totalWaterUsage = getHours(startTimeEpoch, requiredToTime)*averageWaterFlowRate;
            }else if (startTimeEpoch < requiredFromTime && stopTimeEpoch > requiredToTime){
                this.totalWaterUsage = getHours(requiredFromTime, requiredToTime)*averageWaterFlowRate;
            }
        }else {
            this.totalWaterUsage = getHours(startTimeEpoch, stopTimeEpoch) * averageWaterFlowRate;
        }
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

    public double getTotalWaterUsageOverRequiredTimePeriod() {
        return totalWaterUsage;
    }

    private double getHours(long startEpoch, long toEpoch){
        return (((double)toEpoch - (double)startEpoch)/(double)3600);
    }
}
