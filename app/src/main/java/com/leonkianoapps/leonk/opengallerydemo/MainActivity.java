package com.leonkianoapps.leonk.opengallerydemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.os.Environment.getExternalStoragePublicDirectory;  // Add this import manually for getExternalStoragePublicDirectory in order to store photo to the public storage

//Make sure to check the manifest inorder to provide a provider authorities with your package and a xml file path


public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button galleryButton, cameraButton,savePhotoOnline;

    private static int OPEN_GALLERY = 1;
    private static int OPEN_CAMERA = 20;

    String mCurrentPhotoPath;

    CoordinatorLayout mainCoordinatorLayout;

    private String uploadURL = "http://192.168.100.8/openGalleryDemo/uploadImage.php";

    Bitmap uploadBitmap;

    int numberImage = 1;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainCoordinatorLayout = findViewById(R.id.coordinatorMainLayout);

        progressBar = findViewById(R.id.uploadingImageProgressBar);

        imageView = findViewById(R.id.galleryImageView);
        galleryButton = findViewById(R.id.openGalleryButton);
        cameraButton = findViewById(R.id.openCameraButton);

        savePhotoOnline = findViewById(R.id.saveOnlineButton);

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openGallery();

            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openCamera();
            }
        });

        savePhotoOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(imageView.getDrawable()==null){

                    Toast.makeText(getApplicationContext(),"Please take a photo or select from the gallery",Toast.LENGTH_LONG).show();

                }else {

                    uploadImage();

                }
            }
        });


    }

    //Methods to open the Gallery

    private void openGallery() {
        //Ask for permission to access/read storage for marshmallow and greater here

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   //Requesting the permission with the appropriate request code
                        OPEN_GALLERY);
            } else {

                getPhoto();     //If the permission was already granted the first time it will run the method to open the gallery intent
            }
        }


    }

    private void getPhoto() {

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, OPEN_GALLERY);  //Check onActivityResult on how to handle the photo selected
    }


    //Methods to open the Camera

    private void openCamera() {

        //Ask for permission to access the camera for marshmallow and greater here

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA},    //Requesting the permission with the appropriate request code
                        OPEN_CAMERA);
            } else {

                getCameraPhoto();  //If the permission was already granted the first time it will run the method to open the Camera intent
            }
        }


    }

    private void getCameraPhoto() {


        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //Intent to open camera


        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go

            File photoFile = null;

            try {

                photoFile = createImageFile();

            } catch (IOException ex) {
                // Error occurred while creating the File

                Toast.makeText(getApplicationContext(), "File could not be created", Toast.LENGTH_LONG).show();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                mCurrentPhotoPath = photoFile.getAbsolutePath();  //Getting the file photo path to be used later to store it in the device's photo gallery

                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        "com.leonkianoapps.leonk.opengallerydemo.fileprovider",   //must match the authorities in the manifest, use your own app package name
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, OPEN_CAMERA);   //Check onActivityResult on how to handle the photo captured
            }
        }
    }


    //Method to create the photo File

    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);  //To make the photo file public to other apps.

        //If you want the photo private to the app use getExternalFilesDir() above, but note he media scanner used to add the photo to the gallery cannot access the files because they are private to your app.

        File image = null;
        try {
            image = File.createTempFile(timeStamp, ".jpg", storageDir);

        } catch (IOException p) {

            Toast.makeText(getApplicationContext(), "Something went wrong in the files", Toast.LENGTH_LONG).show();  //You can change here
            p.printStackTrace();
        }


        return image;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == OPEN_GALLERY) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to access gallery

                getPhoto();

            } else {

                Toast.makeText(getApplicationContext(), "You have Disabled this feature", Toast.LENGTH_LONG).show();

            }
        } else if (requestCode == OPEN_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Now user should be able to access the Camera

                getCameraPhoto();

            } else {

                Toast.makeText(getApplicationContext(), "You have Disabled camera for this feature", Toast.LENGTH_LONG).show();

            }


        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //The request code is the id for the intents created and here is how check them

        if (requestCode == OPEN_GALLERY && resultCode == RESULT_OK && data != null) {  //For the gallery intent

            Uri selectedImage = data.getData();

            try {

                uploadBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                imageView.setImageBitmap(uploadBitmap);   //setting the imageView with the photo from the gallery


            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (requestCode == OPEN_CAMERA) {  //For the camera intent

             uploadBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);   //path of the photo file used to create a bitmap

            imageView.setImageBitmap(uploadBitmap);  //setting the imageView with the photo from the camera

            galleryAddPic();   // Adding the picture from the camera to the device gallery


        }
    }




    private void galleryAddPic() {   //Method to add picture taken to the phone Gallery

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        File f = new File(mCurrentPhotoPath);   //Setting the path of the photo taken by the camera

        Uri contentUri = Uri.fromFile(f);       //Creating a Uri from the file

        mediaScanIntent.setData(contentUri);

        this.sendBroadcast(mediaScanIntent);
    }

    private void saveOnline(String response) {


        progressBar.setVisibility(View.GONE);

        Snackbar snackbar = Snackbar.make(mainCoordinatorLayout,response,Snackbar.LENGTH_LONG)
                                    .setDuration(5000)
                                    .setAction("OKAY", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });
        snackbar.setActionTextColor(Color.CYAN);
        snackbar.show();
    }

    private void uploadImage(){

        progressBar.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //Server response is in form of a JsonObject

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    String serveResponse = jsonObject.getString("response");

                    saveOnline(serveResponse);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) //Request Body
        {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

               Map<String,String> params = new HashMap<>();

               String imageName = "image"+String.valueOf(numberImage);
               numberImage++;


                params.put("name",imageName);
                params.put("image",imageToString(uploadBitmap));

                return params;
            }
        } ;

        MySingleton.getInstance(MainActivity.this).addToRequest(stringRequest);

    }

    //Converting image bitmap into string

    private String imageToString(Bitmap bitmap){

        ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();



        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }

}
