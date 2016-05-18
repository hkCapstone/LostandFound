package com.hk.lostandfound;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by 660162328 on 5/10/2016.
 */
public class UserInfo extends AppCompatActivity implements OnItemSelectedListener {

    private EditText emailEditText;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
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
    private EditText etPetDescription;
    private Spinner spinner;
    private String strUserName;
    private String strAddress;
    private String strCity;
    private String strZip;
    private String strEmail;
    private String strPetName;
    private String strPetType;
    private String strBreed;
    private String strPetDescription;
    private String photoName;
    private boolean postData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_form);

        // lost and found spinner drop down
        spinner =(Spinner) findViewById(R.id.Lost_found_spinner_id);
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<String>();
        categories.add("Select");
        categories.add("Lost");
        categories.add("Found");
        // adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // dropdown layout style - list view with radio buttons
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
        emailEditText =(EditText)findViewById(R.id.Email_editText_id);
        final Button buttonOne = (Button) findViewById(R.id.add_Photo_button_id);
        final Button buttonTwo = (Button) findViewById(R.id.post_info_button_id );

        buttonOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);


        buttonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailEditText.getText().toString();
                if(!isValidEmail(email)){
                    emailEditText.setError("Invalid Email");
                }

            }
        });


        etUserName = (EditText) findViewById(R.id.Name_edittext_id);
        strUserName = etUserName.getText().toString();

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
        etPetDescription = (EditText) findViewById(R.id.description_0f_pet_edittext_id);
        strPetDescription = etPetDescription.getText().toString();

        if(TextUtils.isEmpty(strPetDescription)) {
            etPetDescription.setError("Please Enter A Description");
            return;
        }

    }

    //Check for valid email
    private boolean isValidEmail(String email){
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\\\+]+(\\\\.[_A-Za-z0-9-]+)*@"
                +"[A-Za-z0-9-]+(\\\\.[A-Za-z0-9]+)*(\\\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

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
///// camera code

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CameraUtil.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserInfo.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=CameraUtil.checkPermission(UserInfo.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

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
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        photoName = System.currentTimeMillis() + ".jpg";
        File destination = new File(Environment.getExternalStorageDirectory(), photoName);

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ivImage.setImageBitmap(thumbnail);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                photoName = bm.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
    }
}