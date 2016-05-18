package com.hk.lostandfound; /**
 * Created by David on 5/3/2016.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBUtil {

    //connection status
    boolean connected = false;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();

    // url to get all products list
    private static String url = "";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_FEEDS = "feeds";
    private static final String TAG_FEED_ID = "feed_id";
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_PET_ID = "pet_id";
    private static final String TAG_PET_NAME = "petName";
    private static final String TAG_FEED_TYPE = "feedType";
    private static final String TAG_POST_DATE = "postDate";
    private static final String TAG_USER_NAME = "userName";
    private static final String TAG_ADDRESS = "address";
    private static final String TAG_CITY = "city";
    private static final String TAG_ZIP = "zip" ;
    private static final String TAG_PHONE_NUMBER = "phoneNumber";
    private static final String TAG_EMAIL = "email";
    private static final String TAG_PET_TYPE = "petType";
    private static final String TAG_BREED = "breed";
    private static final String TAG_GENDER = "gender";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_PHOTO_NAME = "photoName";

    // products JSONArray
    JSONArray feeds = null;

    public ArrayList<HashMap<String, String>> LoadFeeds(String feedType)  {
        url = "http://ec2-54-201-139-202.us-west-2.compute.amazonaws.com/get_all_" + feedType + ".php";

      new loadFeedphp().execute();
        return feedList;
    }
    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class loadFeedphp extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Do nothing
        }

        protected String doInBackground(String... args) {
            /**
             * getting All feeds from url
             * */
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            connected = false;
            JSONObject json = jParser.makeHttpRequest(url, "GET", params);
            // Check your log cat for JSON reponse
            Log.d("Database Return: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    connected = true;
                    // feeds found
                    // Getting Array of feeds
                    feeds = json.getJSONArray(TAG_FEEDS);

                    // looping through All Products
                    for (int i = 0; i < feeds.length(); i++) {
                        JSONObject c = feeds.getJSONObject(i);

                        // Storing each json item in variable
                        String feed_id = c.getString(TAG_FEED_ID);
                        String user_id = c.getString(TAG_USER_ID);
                        String pet_id = c.getString(TAG_PET_ID);
                        String feedType = c.getString(TAG_FEED_TYPE);
                        String postDate = c.getString(TAG_POST_DATE);
                        String userName = c.getString(TAG_USER_NAME);
                        String address = c.getString(TAG_ADDRESS);
                        String city = c.getString(TAG_CITY);
                        String zip = c.getString(TAG_ZIP);
                        String phoneNumber = c.getString(TAG_PHONE_NUMBER);
                        String email = c.getString(TAG_EMAIL);
                        String petType = c.getString(TAG_PET_TYPE);
                        String petName = c.getString(TAG_PET_NAME);
                        String breed = c.getString(TAG_BREED);
                        String gender = c.getString(TAG_GENDER);
                        String description = c.getString(TAG_DESCRIPTION);
                        String photoName = c.getString(TAG_PHOTO_NAME);


                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_FEED_ID, feed_id);
                        map.put(TAG_USER_ID, user_id);
                        map.put(TAG_PET_ID, pet_id);
                        map.put(TAG_FEED_TYPE, feedType);
                        map.put(TAG_POST_DATE, postDate);
                        map.put(TAG_USER_NAME, userName);
                        map.put(TAG_ADDRESS, address);
                        map.put(TAG_CITY, city);
                        map.put(TAG_ZIP, zip);
                        map.put(TAG_PHONE_NUMBER, phoneNumber);
                        map.put(TAG_EMAIL, email);
                        map.put(TAG_PET_TYPE, petType);
                        map.put(TAG_PET_NAME, petName);
                        map.put(TAG_BREED, breed);
                        map.put(TAG_GENDER, gender);
                        map.put(TAG_DESCRIPTION, description);
                        map.put(TAG_PHOTO_NAME, photoName);

                        // adding HashList to ArrayList
                        feedList.add(map);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {
            if (connected)
                Toast.makeText(ApplicationContextProvider.getContext(), "Data Loading", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(ApplicationContextProvider.getContext(),"Error Loading Data", Toast.LENGTH_SHORT ).show();

        }
    }
}