package com.hack.he.api;

/**
 * Created by PreethiMaharajan on 10/4/2015.
 */

import org.json.JSONObject;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class LatLngAPI {

    private static final String TAG = PlaceAPI.class.getSimpleName();

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_DETAIL = "/details";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyBJVlcb2RRxBKzFB-EdcJGjmkg-AABdYZI";

    public ArrayList<Double> findLatLng (String input) {
        ArrayList<Double> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_DETAIL + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&placeid=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject result = jsonObj.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");
            System.out.println("la chaine Json "+result);
            Double longitude  = result.getDouble("lng");
            Double latitude =  result.getDouble("lat");
            System.out.println("longitude et latitude "+ longitude+latitude);
            resultList.add(result.getDouble("lat"));
            resultList.add(result.getDouble("lng"));

        } catch (JSONException e) {
            Log.e(TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }
}
