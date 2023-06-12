package com.dktes.medileaf;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    // This is the parent activity
    // It contains only two but major operations: capture image and upload image
    // Capture image allows a user to click a image of the leaf from their mobile camera
    // Upload image allows a user to upload image of the leaf from their mobile gallery

    // The 'image' variable is used to store the captured or uploaded image by the user
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Set the message of the day to text_message
        String message_of_day = Utility.getMessage();
        TextView textMessage = findViewById(R.id.text_message);
        textMessage.setText(message_of_day);

        // Initialize the buttons for capturing and uploading
        FloatingActionButton actionCapture = findViewById(R.id.action_capture);
        FloatingActionButton actionUpload = findViewById(R.id.action_upload);

        // This intent will be used to jump to the result activity of our app
        Intent resultActivity = new Intent(getApplicationContext(), ResultActivity.class);

        // Create actions for capturing and uploading an image
        // This action will be calling a different app to capture or upload an image
        // Hence, it will be evaluated if the different app returned an image or not

        // ActivityResultContracts.StartActivityForResult()
        // this line of code means that there is a contract between two app - Medileaf and gallery or camera
        // this contract tells the other app that we want some result from you
        // camera and gallery are obvious tools to load and share images and videos. In our case, its an image
        // So, when we call this contract, the other app will launch
        ActivityResultLauncher<Intent> captureOrUpload = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            // If the results are returned accurately and the other app is closed properly, we will begin loading the image
            if (result.getResultCode() == Activity.RESULT_OK) {
                // the other app returns an intent object which contains the image data
                Intent data = result.getData();
                // we need to make sure that data is not null
                assert data != null;
                try {
                    // get the extra content of the intent and find the key 'data' which represents an image
                    // this will only possible when user captures an image with camera
                    this.image = (Bitmap) data.getExtras().get("data");
                }
                catch(NullPointerException ex) {
                    // a null pointer exception will occur if the user has uploaded it from gallery
                    // in that case, we will get the path of the image from gallery app which will later used to load image using GetFromUri method
                    this.image = getFromUri(data.getData(), this);
                }

                // A new separate method is created which will call the ClassificationHelper and return the leaf name
                String leafName = classifyImage(this);

                // We will send this leaf name to the result activity
                resultActivity.putExtra("leafName", leafName);
                startActivity(resultActivity);

            } else {
                // If the results are not returned, a Toast will be displayed to avoid crashing of app
                Toast.makeText(this, "Cannot get image at this moment", Toast.LENGTH_LONG).show();
                this.image = null;
            }
        });


        // The capture button will load image from camera
        // To do so, we have a ACTION_IMAGE_CAPTURE which tells a new intent to find all the apps that can capture images for us
        // When a user select a app, it will redirect to its camera mode
        actionCapture.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            // we pass this intent to our action previously defined
            // the action will now initiate a contract between camera apps and Medileaf and start the selected camera app
            captureOrUpload.launch(intent);
        });

        // the upload image button will load image from gallery
        // To do so, we have ACTION_PICK which will tell the Medileaf that we are picking up a random file from the storage
        actionUpload.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);

            // We additionally tell the intent what type of data we are intending to load
            // In our case, its a image data we want to load hence we passing an EXTERNAL_CONTENT_URI from Media class
            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // The action will not initiate a contract between gallery apps and Medileaf and start the selected gallery app
            captureOrUpload.launch(intent);
        });
    }

    private Bitmap getFromUri(Uri uri, Activity activity) {
        // gallery app will surely return the path to the image that the user select
        // this method's job is to load the image from the given path
        try {
            // This is way simpler
            // The MediaStore has functionality to get a bitmap image from the given URI
            // the getContentResolver() identifies from which source the data has arrived into our app. Ex, cloud apps, gallery or external pen drive
            // If the resolver cannot identify the source, the image loading fails
            return MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while loading the image from gallery");
        }
        return null;
    }

    private String classifyImage(Activity activity) {
        // This is a supporting method that handles all the classification stuff
        String result = "";

        // Now we first convert the image from gallery or camera to a tensor image
        ImageHelper imageHelper = new ImageHelper(this.image);
        try {
            // here, the classification helper will get an image and an activity which will be used as a resource to classify it
            ClassificationHelper classificationHelper = new ClassificationHelper(activity, imageHelper.getTensorImage());
            // the results will contain a single string that is the name of the leaf that we want to find
            result = classificationHelper.classify();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage() + " -- Exception in classifyImage() method");
        }
        // return the name of leaf
        return result;
    }
}