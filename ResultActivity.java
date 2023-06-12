package com.dktes.medileaf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResultActivity extends AppCompatActivity {
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // All the data about the leaves is stored in data.json file in assets folder of the project
        // To access any JSON file, we need to have a JsonReader from Android's standard utility library
        JsonReader reader;
        Intent intent = getIntent();

        // The name of leaf is predicted by the Deep Learning model in the last step and passed to this activity
        // Now, we read that leaf name from the intent object and find the record related to the leaf in data.json file
        String leafName = intent.getExtras().getString("leafName");

        try {
            // Open the data.json file from the assets folder. the InputStream is the obvious way to read files in Java
            InputStream in = getApplicationContext().getAssets().open("data.json");
            // Pass the input stream to the json reader. Json reader will identify the stream and read all the data from the file
            reader = new JsonReader(new InputStreamReader(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // DataHelper is created by us. This file will read the JSON data.json file and will create a list of leaf data
        DataHelper dataHelper;
        try {
            // We initialize the data helper object and wait till the data helper get all leaf data from the data.json
            dataHelper = new DataHelper(reader, leafName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Data helper will point out the leaf data that we want to show
        // This leaf data will be shown on the result activity with the image
        Leaf leaf = dataHelper.leaf;

        // We will find image using the leaf name
        // Now, leaf name could contain dashes, spaces, and capital alphabets. We need to replace any spaces and dashes first,
        // then lowercase the leaf name and use it as a resource key.
        String resourceName = leafName.replace("\\s+-", "").toLowerCase();
        resourceName = resourceName.replace("-", "").toLowerCase();

        // In this line of code, we will use the resource name to locate the image
        @SuppressLint("DiscouragedApi")
        int resId = getResources().getIdentifier(resourceName, "drawable", getApplicationInfo().packageName);

        // Assign the image to the imageview that holds the image of the leaf.
        ImageView imageMedicine = findViewById(R.id.image_medicine);
        // the setImageDrawable function sets an image to the given image view
        // To this function, we must pass a drawable object from drawable folder of the project to show the image
        imageMedicine.setImageDrawable(ResourcesCompat.getDrawable(getResources(), resId, getTheme()));

        // Initialize all text views to show the information about the leaf
        TextView textTitle = findViewById(R.id.text_title);
        TextView textScientific = findViewById(R.id.text_scientific);
        TextView textOrigin = findViewById(R.id.text_origin);
        TextView textFeature = findViewById(R.id.text_feature);
        TextView textDesc = findViewById(R.id.text_desc);

        // Use the methods from the leaf object to get a particular information about the leaf and set it to a text view
        textTitle.setText(leaf.getLeafName());
        textScientific.setText(leaf.getScientificName());
        textOrigin.setText(leaf.getOrigin());
        textFeature.setText(leaf.getFeature());
        String desc = leaf.getDescription() + " " + leaf.getUsage();
        textDesc.setText(desc);
    }
}