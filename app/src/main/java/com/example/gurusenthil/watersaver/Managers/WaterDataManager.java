package com.example.gurusenthil.watersaver.Managers;

import android.content.Context;

import com.example.gurusenthil.watersaver.ServerStuff.ServerRequests;

/**
 * Created by GuruSenthil on 10/7/16.
 */

public class WaterDataManager {
    private static WaterDataManager waterDataManager;
    public static WaterDataManager getWaterDataManager() {
        if (waterDataManager == null){
            waterDataManager = new WaterDataManager();
        }
        return waterDataManager;
    }

    public void getWaterRecordsForTimePeriod(Context context, long fromTimeEpoch, long toTimeEpoch, ServerRequests.WaterDataRequests.WaterDataRequestListener waterDataRequestListener, Integer index){
        ServerRequests.WaterDataRequests.requestWaterRecordsForTimePeriod(context, fromTimeEpoch, toTimeEpoch, waterDataRequestListener, index);
    }
}
