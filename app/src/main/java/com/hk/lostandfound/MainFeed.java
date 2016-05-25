package com.hk.lostandfound;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MainFeed extends AppCompatActivity implements OnItemClickListener, OnClickListener {

    private String picUrl = "http://ec2-54-191-44-221.us-west-2.compute.amazonaws.com/images/";
    private ArrayList<HashMap<String, String>> feedList = new ArrayList<HashMap<String, String>>();
    private ArrayList<String> addresses = new ArrayList();
    private Button lostButton;
    private Button foundButton;
    private Button allButton;
    private Button mapButton;
    private ListView list;
    private final String LOST = "lost";
    private final String FOUND = "found";
    private final String ALL = "";
    private DBUtil dbUtil = new DBUtil();
    private ArrayAdapter<HashMap<String, String>> adapter;

    //Default Constructor
    public MainFeed(){}

    //On Create Method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_feed);

        //Implement initial app components to view
        if (feedList != null) {
            feedList.clear();
            feedList.trimToSize();
            feedList = null;
        }
        getData(ALL);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        buildFeed();
        list.setOnItemClickListener(this);

        //Implement the rest of the app components
        lostButton = (Button) findViewById(R.id.lostButton);
        foundButton = (Button) findViewById(R.id.foundButton);
        allButton = (Button) findViewById(R.id.allButton);
        mapButton = (Button) findViewById(R.id.mapButton);

        //Listeners
        lostButton.setOnClickListener(this);
        foundButton.setOnClickListener(this);
        allButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity_ui, menu);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.item1:
                Intent intent = new Intent(this, UserInfo.class);
                this.startActivity(intent);
                return super.onOptionsItemSelected(item);

            case R.id.item2:

                return super.onOptionsItemSelected(item);

        }
        return true;
    }

    //Start data loading
    public void getData(String searchType) {
        boolean finished = false;
        /*
        * To get lost feeds pass string lost
        * To get found feeds pass string found
        * To get all feeds pass null string
        * */
        feedList = dbUtil.LoadFeeds(searchType);
        while (!finished) {
            if (feedList != null) {
                finished = true;
            }
        }
    }

    //Start building list view with MyListAdapter Class
    public void buildFeed() {
        adapter = new MyListAdapter();
        list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
    }

    //Parse Date from database
    public String parseDate(String date) {
        char ch;
        int space = 32;
        String year;
        String month;
        String day;
        String hyphen;
        String newDate = "";
        for (int i = 0; i < date.length(); i++) {
            ch = date.charAt(i);
            if (ch != space) {
                newDate += ch;
            } else if (space == 32) {
                break;
            }
        }//2016-05-02
        newDate += " ";
        year = newDate.substring(0, 4);
        month = newDate.substring(5, 7);
        day = newDate.substring(7, 10);
        hyphen = newDate.substring(4, 5);
        newDate = month + day + hyphen + year;
        return newDate;
    }

    //Capitalize first letter of found or lost type
    public String capitalize(String input) {
        String output = input.substring(0, 1).toUpperCase() + input.substring(1);
        return output;
    }

    //Build feed object
    public Feed buildFeedObject(HashMap<String, String> feedHash) {

        Feed newFeed = new Feed();
        newFeed.setEmail(feedHash.get("email"));
        newFeed.setPhotoUrl(picUrl + feedHash.get("photoName"));
        newFeed.setPetType(feedHash.get("petType"));
        newFeed.setPetName(capitalize(feedHash.get("petName")));
        newFeed.setBreed(feedHash.get("breed"));
        newFeed.setFeedType(capitalize(feedHash.get("feedType")));
        newFeed.setPostDate(parseDate(feedHash.get("postDate")));
        newFeed.setUserName(capitalize(feedHash.get("userName")));
        newFeed.setAddress(feedHash.get("address"));
        newFeed.setCity(capitalize(feedHash.get("city")));
        newFeed.setZip(feedHash.get("zip"));
        newFeed.setPhoneNumber(feedHash.get("phoneNumber"));
        newFeed.setDescription(feedHash.get("description"));
        newFeed.setGender(feedHash.get("gender"));
        return newFeed;
    }

    //On Item Click Event Handler for list view
    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Intent singleIntent = new Intent(this, SingleFeed.class);
        singleIntent.putExtra("singleFeed", feedList.get(position));
        startActivity(singleIntent);
    }

    //On Click Event Handler for buttons
    @Override
    public void onClick(View v) {
        boolean startMap = false;
        switch (v.getId()) {
            case R.id.lostButton:
                feedList.clear();
                feedList.trimToSize();
                feedList = null;
                getData(LOST);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.foundButton:
                feedList.clear();
                feedList.trimToSize();
                feedList = null;
                getData(FOUND);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            case R.id.allButton:
                feedList.clear();
                feedList.trimToSize();
                feedList = null;
                getData(ALL);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.mapButton:
                Intent mapIntent = new Intent(this, Google_Map.class);
                mapIntent.putExtra("feedList", feedList);
                startActivity(mapIntent);
                startMap = true;
                break;
        }
        if (!startMap)
        adapter.notifyDataSetChanged();
    }


    //Exit app
    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    //Class to build the list view
    private class MyListAdapter extends ArrayAdapter<HashMap<String, String>> {

        private String input;

        public MyListAdapter() {
            super(MainFeed.this, R.layout.list_view_layout, feedList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //make sure we have a view to work with (may have been given null)
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_view_layout, parent, false);
            }
            // find the feed to work with
            Feed currentFeed = buildFeedObject(feedList.get(position));

            //ImageView
            ImageView thumbnail = (ImageView) itemView.findViewById(R.id.thumbnailListView);

            new DownloadImageTask(thumbnail).execute(currentFeed.getPhotoUrl());

            // TextViews
            TextView petNameText = (TextView) itemView.findViewById(R.id.petNameLabel);
            input = "Pet: " + currentFeed.getPetName();
            petNameText.setText(input);

            TextView userNameText = (TextView) itemView.findViewById(R.id.ownerNameLable);
            if (currentFeed.getFeedType().equalsIgnoreCase("lost")) {
                input = "Owner: " + currentFeed.getUserName();
            } else if (currentFeed.getFeedType().equalsIgnoreCase("found")) {
                input = "Finder: " + currentFeed.getUserName();
            }
            userNameText.setText(input);

            TextView petTypeText = (TextView) itemView.findViewById(R.id.typeLabel);
            input = "Type: " + currentFeed.getFeedType() + " " + currentFeed.getPetType();
            petTypeText.setText(input);

            TextView contactInfoText = (TextView) itemView.findViewById(R.id.contactLabel);
            input = "Email: " + currentFeed.getEmail();
            contactInfoText.setText(input);

            TextView postDate = (TextView) itemView.findViewById(R.id.postDateLabel);
            postDate.setText(currentFeed.getPostDate());

            return itemView;
        }
    }

    //Class to load pictures
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
            bmImage.setVisibility(View.INVISIBLE);
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
            bmImage.setVisibility(View.VISIBLE);
        }
    }
}





