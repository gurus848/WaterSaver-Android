package com.example.gurusenthil.watersaver;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GuruSenthil on 10/6/16.
 */

public class ServerRequests {

    private static RequestQueue requestQueue;

    public static void init(Context context){
        if (requestQueue == null){  //Executed only once to set up the request queue for volley.
            requestQueue = Volley.newRequestQueue(context);
        }
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static class WaterDataRequests {

        interface WaterDataRequestListener {
            void dataReceived(ArrayList<WaterRecordDataModel> waterRecordDataModels);
        }

        public static void requestWaterRecordsForTimePeriod(Context context, long startTimeEpoch, long endTimeEpoch, final WaterDataRequestListener waterDataRequestListener){
            String URL = Constants.baseURL + Constants.waterRecordRequestURL + Constants.parameterFlag + Constants.fromTimeParameter + Constants.parameterEqualToSign + startTimeEpoch + Constants.parameterSeperatorSymbol + Constants.toTimeParameter + Constants.parameterEqualToSign + endTimeEpoch;
            Log.d("URL test", URL);
            Map<String,String> map = new HashMap<>();
            map.put(Constants.fromTimeParameter, Long.toString(startTimeEpoch));
            map.put(Constants.toTimeParameter, Long.toString(endTimeEpoch));

            JSONArrayRequest request = new JSONArrayRequest(context, Request.Method.GET, URL, map, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {

                    Log.d("testing response", response.toString());

                    ArrayList<WaterRecordDataModel> waterRecordDataModels = new ArrayList<>();
                    for (int i = 0; i<response.length(); i++){
                        try {
                            JSONObject daton = response.getJSONObject(i);

                            int id = daton.getInt(WaterRecordDataModel.idKey);
                            long startTimeEpoch = daton.getLong(WaterRecordDataModel.startTimeEpochKey);
                            long stopTimeEpoch = daton.getLong(WaterRecordDataModel.stopTimeEpochKey);
                            float averageFlowRate = (float) daton.getDouble(WaterRecordDataModel.averageFlowRateKey);
                            WaterRecordDataModel waterRecordDataModel = new WaterRecordDataModel(id, startTimeEpoch, stopTimeEpoch, averageFlowRate);
                            waterRecordDataModels.add(waterRecordDataModel);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    waterDataRequestListener.dataReceived(waterRecordDataModels);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volley error", error.toString());
                }
            });

            ServerRequests.getRequestQueue().add(request);
        }
    }


}
