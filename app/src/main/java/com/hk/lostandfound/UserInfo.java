package com.hk.lostandfound;

import com.hk.lostandfound.AndroidMultiPartEntity.ProgressListener;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by George and David on 5/10/2016.
 */
public class UserInfo extends Activity implements OnItemSelectedListener {

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    // LogCat tag
    private static final String TAG = UserInfo.class.getSimpleName();
    private EditText emailEditText;
    private ImageView ivImage;
    private String userChoosenTask;
    private EditText etUserName;
    private EditText etAddress;
    private EditText etCity;
    private EditText etZip;
    private EditText etEmail;
    private EditText etPetName;
    private EditText etPetType;
    private EditText etBreed;
    private EditText etPhoneNumber;
    private EditText etPetDescription;
    private Spinner spinner;
    private String strUserName;
    private String strAddress;
    private String strCity;
    private String strZip;
    private String strEmail;
    private String strPetName;
    private String strPetType;
    private String strPhoneNumber;
    private String strBreed;
    private String strPetDescription;
    private String photoName;
    private boolean postData;
    private RadioGroup radioGroup;
    private DBUtil dbUtil = new DBUtil();
    private CameraUtil cameraUtil = new CameraUtil();
    private Uri fileUri;
    private String filePath;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private ProgressBar progressBar;
    private TextView txtPercentage;
    long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_form);

        //Make auto focus edittext false work around invisible
        AutoCompleteTextView noEditTextFocus = (AutoCompleteTextView) findViewById(R.id.autotext);
        noEditTextFocus.setVisibility(View.INVISIBLE);

        // lost and found spinner drop down and radio button group
        spinner =(Spinner) findViewById(R.id.Lost_found_spinner_id);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        spinner.setOnItemSelectedListener(this);

        //Set spinner indexes
        List<String> categories = new ArrayList<String>();
        categories.add("Select");
        categories.add("Lost");
        categories.add("Found");

        // adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        //set adapter for layout
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);

        emailEditText =(EditText)findViewById(R.id.Email_editText_id);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        final Button buttonOne = (Button) findViewById(R.id.add_Photo_button_id);
        final Button buttonTwo = (Button) findViewById(R.id.post_info_button_id );

        //Start photo selection
        buttonOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);

        if(getIntent().getStringExtra("filePath") != null) {
            filePath = getIntent().getStringExtra("filePath");
        }

        //Validate entry
        buttonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postData = true;

                strEmail = emailEditText.getText().toString().toLowerCase();
                if(!isValidEmail(strEmail)){
                    emailEditText.setError("Invalid Email");
                    postData = false;
                }
                strUserName = etUserName.getText().toString().toLowerCase();
                if(TextUtils.isEmpty(strUserName)) {
                    etUserName.setError("Please Enter Name");
                    postData = false;
                }
                strAddress = etAddress.getText().toString();
                if(TextUtils.isEmpty(strAddress)) {
                    etAddress.setError("Please Enter Street Address");
                    postData = false;
                }
                strCity = etCity.getText().toString().toLowerCase();
                if(TextUtils.isEmpty(strCity)) {
                    etCity.setError("Please Enter City Name");
                    postData = false;
                }
                strZip = etZip.getText().toString();
                if (strZip.length() > 5)
                    postData = false;
                etZip.setError("Please Enter Valid Zip Code");
                if(TextUtils.isEmpty(strZip)) {
                    etZip.setError("Please Enter Zip Code");
                    postData = false;
                }
                strPetName = etPetName.getText().toString().toLowerCase();
                if(TextUtils.isEmpty(strPetName)) {
                    etPetName.setError("Please Enter Your Pets Name");
                    postData = false;
                }
                strPetType = etPetType.getText().toString();
                if(TextUtils.isEmpty(strPetType)) {
                    etPetType.setError("Please Enter The Type Of Pet i.e \"Dog\"");
                }
                strBreed = etBreed.getText().toString();
                if (TextUtils.isEmpty(strBreed)){
                    etBreed.setError("Please Enter Breed Of Pet");
                    postData = false;
                }
                strPetDescription = etPetDescription.getText().toString();
                if(TextUtils.isEmpty(strPetDescription)) {
                    etPetDescription.setError("Please Enter A Description");
                    postData = false;
                }
                strPhoneNumber = etPhoneNumber.getText().toString();
                if (strPhoneNumber.length() != 7 || strPhoneNumber.length() != 10 || strPhoneNumber.length() != 11) {
                    etPhoneNumber.setError("Please Enter A Valid Phone Number");
                }
                if(TextUtils.isEmpty(strPhoneNumber)) {
                    etPhoneNumber.setError("Please Enter A Valid Phone Number");
                    postData = false;
                }
                if (photoName == null){
                    postData = false;
                    Toast.makeText(ApplicationContextProvider.getContext(), "Please Add Or Take A Photo Of The Pet", Toast.LENGTH_LONG).show();
                }
                String feedType = spinner.getSelectedItem().toString();
                if (feedType.equalsIgnoreCase("select")){
                    Toast.makeText(ApplicationContextProvider.getContext(), "Please Select Feed Type, Lost Or Found Pet", Toast.LENGTH_LONG).show();
                    postData = false;
                }
                int id  = radioGroup.getCheckedRadioButtonId();
                View radioButton = radioGroup.findViewById(id);
                int radioId = radioGroup.indexOfChild(radioButton);
                RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
                String gender = (String) btn.getText();

                if(postData){
                    dbUtil.uploadData(feedType, strUserName, strAddress ,strCity, strZip, strPhoneNumber,
                            strEmail, strPetName, strPetType, strBreed, gender, strPetDescription, photoName );
                    try{
                        Thread.sleep(5000);
                    }catch (Exception e){

                    }
                    updateMainFeed();
                }
            }
        });

        etUserName = (EditText) findViewById(R.id.Name_edittext_id);
        strUserName = etUserName.getText().toString();

        //Start text entery checks for error messages
        if(TextUtils.isEmpty(strUserName)) {
            etUserName.setError("Please Enter Name");

        }
        etAddress = (EditText) findViewById(R.id.Address_edittext_id);
        strAddress = etAddress.getText().toString();

        if(TextUtils.isEmpty(strAddress)) {
            etAddress.setError("Please Enter Street Address");

        }
        etCity = (EditText) findViewById(R.id.City_edittext_id);
        strCity = etCity.getText().toString();

        if(TextUtils.isEmpty(strCity)) {
            etCity.setError("Please Enter City Name");

        }
        etZip = (EditText) findViewById(R.id.Zip_edittext_id);
        strZip = etZip.getText().toString();

        if(TextUtils.isEmpty(strZip)) {
            etZip.setError("Please Enter Zip Code");

        }
        etEmail = (EditText) findViewById(R.id.Email_editText_id);
        strEmail = etEmail.getText().toString();

        if(TextUtils.isEmpty(strEmail)) {
            etEmail.setError("Please Enter Email Address");

        }
        etPetName = (EditText) findViewById(R.id.PetName_edittext_id);
        strPetName = etPetName.getText().toString();

        if(TextUtils.isEmpty(strPetName)) {
            etPetName.setError("Please Enter Your Pets Name");

        }
        etPetType = (EditText) findViewById(R.id.Pet_Type_editText_id);
        strPetType = etPetType.getText().toString();

        if(TextUtils.isEmpty(strPetType)) {
            etPetType.setError("Please Enter The Type Of Pet i.e \"Dog\"");
        }
        etBreed = (EditText) findViewById(R.id.breed_edittext_id);
        strBreed = etBreed.getText().toString();

        if (TextUtils.isEmpty(strBreed)){
            etBreed.setError("Please Enter Breed Of Pet");
        }
        etPhoneNumber = (EditText) findViewById(R.id.Phone_edittext_id);

        strPhoneNumber = etPhoneNumber.getText().toString();

        if(TextUtils.isEmpty(strPhoneNumber)) {
            etPhoneNumber.setError("Please Enter A Valid Phone Number");
            postData = false;
        }
        etPetDescription = (EditText) findViewById(R.id.description_0f_pet_edittext_id);
        strPetDescription = etPetDescription.getText().toString();

        if(TextUtils.isEmpty(strPetDescription)) {
            etPetDescription.setError("Please Enter A Description");
            return;
        }

    }

    //Call main feed after data insert
    public void updateMainFeed(){
        Intent mainIntent = new Intent(this, MainFeed.class);
        startActivity(mainIntent);
    }
    //Check for valid email
    private boolean isValidEmail(String email){
        String EMAIL_PATTERN = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //On item select listener for spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        if(!item.equalsIgnoreCase("Select"))
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    ///////////////////////////////////////////////
    //Start photo selection from camera or gallery
    //////////////////////////////////////////////
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CameraUtil.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Gallery"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }
    /////////////////////////////////
    //Build selection box for camera
    /////////////////////////////////
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfo.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=cameraUtil.checkPermission(UserInfo.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask ="Choose from Gallery";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    //////////////////////
    // /Intent selections
    /////////////////////
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
    ////////////////////////////////////////////
    //What to do after photo intent is finished
    ////////////////////////////////////////////
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }
    ////////////////////////////
    //What to do after camera shot
    ////////////////////////////
    private void onCaptureImageResult(Intent data) {
        Display d = getWindowManager().getDefaultDisplay();
        filePath = fileUri.getPath();
        ivImage.requestFocus();
        BitmapFactory.Options options = new BitmapFactory.Options();

        // down sizing image as it throws OutOfMemory Exception for larger
        // images
        //options.inSampleSize = 8;

        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        int width = bm.getWidth();
        int height = bm.getHeight();
        if(width > height) {
            bm = rotateBitmap(bm);
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 90, stream);
        bm = getResizedBitmap(bm, (height/4), (width/4));
        ivImage.setImageBitmap(bm);
        new UploadFileToServer().execute();
    }
    ///////////////////////////////////////////////
    //What to do after photo selection from gallery
    ///////////////////////////////////////////////
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Display d = getWindowManager().getDefaultDisplay();
        ivImage.requestFocus();
        Bitmap bm=null;
        if (data != null) {
            try {
                filePath = getRealPathFromURI(this, data.getData());
                photoName = filePath.substring(filePath.lastIndexOf("/")+1);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                bm.compress(Bitmap.CompressFormat.PNG, 90, stream);
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }
        int width = bm.getWidth();
        int height = bm.getHeight();
        if(width > height) {
            bm = rotateBitmap(bm);
        }
        bm = getResizedBitmap(bm, (height/4), (width/4));
        ivImage.setImageBitmap(bm);
        new UploadFileToServer().execute();
    }

    /**
     * ------------ Helper Methods ----------------------
     * */

    ////////////////
    //Rotate bitmap
    ///////////////
    public Bitmap rotateBitmap(Bitmap bm){
        Display d = getWindowManager().getDefaultDisplay();
        // find the width and height of the screen:
        int x = d.getWidth();
        int y = d.getHeight();

        // scale it to fit the screen, x and y swapped because my image is wider than it is tall
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm, y, x, true);

        // create a matrix object
        Matrix matrix = new Matrix();
        matrix.postRotate(90); // clockwise by 90 degrees

        // create a new bitmap from the original using the matrix to transform the result
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(),
                scaledBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }
    /////////////////////////////////
    //Resize bitmap if it is to large
    /////////////////////////////////
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    /**
     * Creating file uri to store image
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image file
     */
    private File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
            photoName = timeStamp+ ".jpg";
        } else {
            return null;
        }
        return mediaFile;
    }
    /////////////////////////////////////////////////////////////////////////////
    //Class for asyncTask to upload to server
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);


            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);
            progressBar.setVisibility(View.INVISIBLE);
            txtPercentage.setText("Upload Successful!");
            // showing the server response in an alert dialog
            //showAlert(result);
            super.onPostExecute(result);
        }

    }
    public static String getRealPathFromURI(Context context, Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}