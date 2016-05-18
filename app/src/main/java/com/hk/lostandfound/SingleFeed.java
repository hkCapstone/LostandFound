package com.hk.lostandfound;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import java.io.InputStream;
import java.util.HashMap;

public class SingleFeed extends AppCompatActivity implements OnEditorActionListener,
        OnClickListener {

    private TextView userName;
    private TextView petName;
    private TextView breed;
    private TextView gender;
    private TextView petType;
    private TextView postDate;
    private TextView feedType;
    private TextView address;
    private String singleAddress;
    private TextView phoneNumber;
    private TextView email;
    private EditText description;
    private ImageView image;
    private Button notifyButton;
    private Button mapDirectionButton;
    private MainFeed feedUtil = new MainFeed();
    private HashMap<String, String> feedHash;
    private Feed singleFeed;
    private String input;
    private NotificationCompat.Builder notify;
    private static final int uniqueID = 7676; //the notification has to assigned a unique ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_feed);

        //Get intent extras passed from main feed
        Intent intent = getIntent();
        feedHash = (HashMap<String, String>) intent.getSerializableExtra("singleFeed");
        singleFeed = feedUtil.buildFeedObject(feedHash);

        //Initialize Components
        petName = (TextView) findViewById(R.id.sf_pet_name_id);
        userName = (TextView) findViewById(R.id.sf_user_name_id);
        breed =(TextView) findViewById(R.id.sf_breed_id);
        gender = (TextView) findViewById(R.id.sf_gender_id);
        petType = (TextView) findViewById(R.id.sf_pet_type_id);
        postDate = (TextView) findViewById(R.id.sf_post_date_id);
        feedType = (TextView) findViewById(R.id.sf_feed_type_id);
        address = (TextView) findViewById(R.id.sf_address_id);//concatenate address zip and city
        phoneNumber = (TextView) findViewById(R.id.sf_phone_number_id);
        email = (TextView) findViewById(R.id.sf_email_id);
        description = (EditText) findViewById(R.id.sf_description_text_id);
        image = (ImageView) findViewById(R.id.sf_image_id);
        notifyButton = (Button) findViewById(R.id.notifyButton);
        mapDirectionButton = (Button)findViewById(R.id.sf_mapButton);

        //Instantiate Listeners
        description.setOnEditorActionListener(this);
        notifyButton.setOnClickListener(this);
        mapDirectionButton.setOnClickListener(this);

        //Load image
        new DownloadImageTask(image).execute(singleFeed.getPhotoUrl());

        //Set TextView Text
        petName.setText(singleFeed.getPetName());
        userName.setText(singleFeed.getUserName());
        breed.setText(singleFeed.getBreed());
        gender.setText(singleFeed.getGender());
        petType.setText(singleFeed.getPetType());
        postDate.setText(singleFeed.getPostDate());
        feedType.setText(singleFeed.getFeedType());
        input = singleFeed.getAddress() + "\n" + singleFeed.getCity() + " " + singleFeed.getZip();
        singleAddress = singleFeed.getAddress() + ", " + singleFeed.getCity() + ", " + singleFeed.getZip();
        address.setText(input);
        phoneNumber.setText(singleFeed.getPhoneNumber());
        email.setText(singleFeed.getEmail());
        description.setText(singleFeed.getDescription());
        description.setEnabled(false);
        description.setInputType(InputType.TYPE_NULL);

        //Set button text
        if (singleFeed.getFeedType().equalsIgnoreCase("lost")) {
            input = "FOUND";
        } else if (singleFeed.getFeedType().equalsIgnoreCase("found")) {
            input = "CLAIM";
        }
        notifyButton.setText(input);

        //build new notification
        notify = new NotificationCompat.Builder(this);
        //remove notification once it has been visited
        notify.setAutoCancel(true);
    }
    //build alert window
    public void alertForMessage(){
        if (singleFeed.getFeedType().equalsIgnoreCase("lost")) {
            input = "Are you sure you want to tell " + singleFeed.getUserName() +
                    " that you found " + singleFeed.getPetName() + "?";
        } else if (singleFeed.getFeedType().equalsIgnoreCase("found")) {
            input = "Are you sure you want to claim  " +  singleFeed.getPetName() +
                    " from " + singleFeed.getUserName() + "?";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(input).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show().setIcon(android.R.drawable.ic_dialog_alert);
    }

    //Code for SMS message
    public void sendSMS(String phoneNo, String message){
        String SENT = "SMS Sent";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0 , new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        //when sms has been sent
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic Failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No Service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio Off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //when the SMS has been delivered
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS Delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not Delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNo, null, message, sentPI, deliveredPI);
    }

    //Code for to Notify user of pet finding or claiming message success
    public void notifyUser(){
        if (singleFeed.getFeedType().equalsIgnoreCase("lost")) {
            input = "You notified " + singleFeed.getUserName() +
                    "\nthat you found " + singleFeed.getPetName() + ".";
        } else if (singleFeed.getFeedType().equalsIgnoreCase("found")) {
            input = "You notified " +  singleFeed.getUserName() +
                    "\n you want to claim " + singleFeed.getPetName() + "." +
                    "\n Please bring proof of ownership.";
        }
        notify.setSmallIcon(R.mipmap.ic_launcher);
        notify.setTicker("Success!");
        notify.setWhen(System.currentTimeMillis());
        notify.setContentTitle("Message Sent!");
        notify.setContentText(input);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notify.setSound(alarmSound);

        //send the notify to the home screen
        Intent i = new Intent(this, MainFeed.class);
        //give the device access to perform this intent by calling pending intent
        PendingIntent pi = PendingIntent.getActivity(this, 0 , i , PendingIntent.FLAG_UPDATE_CURRENT);
        notify.setContentIntent(pi);

        //send out the notification
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notify.build());

    }

    //Edit Text Listener
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

        }
        return false;
    }

    //OnClick Listener for found/claim and map directions button
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.notifyButton:
                alertForMessage();
                break;
            case R.id.sf_mapButton:
                String petInfo = singleFeed.getPetName() + "," + singleFeed.getPetType() + "," +
                        singleFeed.getFeedType() + "," + singleFeed.getPostDate();
                Intent mapIntent = new Intent(this, Google_Map.class);
                mapIntent.putExtra("singleAddress", singleAddress);
                mapIntent.putExtra("petInfo", petInfo);
                startActivity(mapIntent);
                break;
        }
    }

    //Dialog listener for alert box
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    if (singleFeed.getFeedType().equalsIgnoreCase("lost")) {
                        input = "I found " + singleFeed.getPetName() + ". Please contact me to arrange pick up";
                    } else if (singleFeed.getFeedType().equalsIgnoreCase("found")) {
                        input = "Thank you for finding " + singleFeed.getPetName() + ". Please contact me to arrange pick up";
                    }
                    sendSMS(singleFeed.getPhoneNumber(), input);
                    notifyUser();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //Do nothing
                    break;
            }
        }
    };

    //Go back to main feed
    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(this, MainFeed.class);
        startActivity(mainIntent);
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
