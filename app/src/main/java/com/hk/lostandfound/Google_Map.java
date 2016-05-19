package com.hk.lostandfound;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class Google_Map extends AppCompatActivity {

    private GoogleMap map;
    private MainFeed feedUtil = new MainFeed();
    private ArrayList<HashMap<String, String>> feeds = new ArrayList<HashMap<String, String>>();
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private static final LatLng SAN_DIEGO = new LatLng(32.7157,-117.1611);
    private String url = "http://maps.googleapis.com/maps/api/geocode/json?address=";
    //private String key = "&key=AIzaSyAHW9p6Y3r32q6zQzLTuWBM2z1aUc6Somk";
    private String address;
    private String[] petInfo;
    private String longLatUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        //Set map to fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        //Get intent extras passed from main feed if not null, else single address
        feeds = null;
        Intent intent = getIntent();
        try{
            feeds = (ArrayList<HashMap<String, String>>) intent.getSerializableExtra("feedList");
        }catch (NullPointerException npe) {
            //Do nothing        }
        }
        if (feeds != null){
            for(int i = 0; i < feeds.size(); i++){
                Feed feed = feedUtil.buildFeedObject(feeds.get(i));
                address = feed.getAddress() + ", " + feed.getCity() + ", " + feed.getZip();
                address = replaceSpaceWithPlus(address);
                String feedType = feed.getFeedType();
                String petType = feed.getPetType();
                String postDate = feed.getPostDate();
                String petName = feed.getPetName();
                longLatUrl = url+address;//+key;
                new GetJSONLongLat(feedType, petType, postDate, petName).execute();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Bundle extras = getIntent().getExtras();
            address = extras.getString("singleAddress");
            address = replaceSpaceWithPlus(address);
            String data;
            data = extras.getString("petInfo");
            petInfo = data.split(",");
            String petName = petInfo[0];
            String petType = petInfo[1];
            String feedType = petInfo[2];
            String postDate = petInfo[3];
            longLatUrl = url+address;//+key;
            new GetJSONLongLat(feedType, petType, postDate, petName).execute();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SAN_DIEGO, 11));
    }

    //Method for URL request
    public String getLatLongByURL(String requestURL) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                response = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    //Replace spaces with + for address in url
    public String replaceSpaceWithPlus(String address){
        StringBuilder newAddress = new StringBuilder(address);
        char index;
        char space = 32;
        char plus = 43;
        for (int i = 0; i < newAddress.length(); i++){
            index = newAddress.charAt(i);
            if (index == space){
                newAddress.setCharAt(i, plus);
            }
        }
        address = "";
        for (int i = 0; i < newAddress.length(); i++){
            address += newAddress.charAt(i);
        }
        return address;
    }

    //Go back to main feed
    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(this, MainFeed.class);
        startActivity(mainIntent);
    }

    //Class to get JSON data and set the lat and lng to build a marker
    private class GetJSONLongLat extends AsyncTask<String, Void, String[]> {

        ProgressDialog dialog = new ProgressDialog(Google_Map.this);
        String feedType;
        String petType;
        String postDate;
        String petName;

       //Default constructor
       public GetJSONLongLat(String feedType, String petType, String postDate, String petName){
           this.feedType = feedType;
           this.petType = petType;
           this.postDate = postDate;
           this.petName = petName;

       }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Please wait...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            String response;

            try {
                response = getLatLongByURL(longLatUrl);
                Log.d("response",""+response);
                return new String[]{response};
            } catch (Exception e) {
                return new String[]{"error"};
            }
        }

        @Override
        protected void onPostExecute(String ... result) {

            try {
                JSONObject jsonObject = new JSONObject(result[0]);

                 double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                      .getJSONObject("geometry").getJSONObject("location")
                      .getDouble("lng");

                 double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                      .getJSONObject("geometry").getJSONObject("location")
                       .getDouble("lat");

                Log.d("latitude", "" + lat);
                Log.d("longitude", "" + lng);

                LatLng latLng = new LatLng(lat, lng);
                String title = feedType + " " + petType + " " + petName;
                String snippet = postDate;
                map.addMarker((new MarkerOptions().position(latLng).title(title).snippet(snippet)));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
}
