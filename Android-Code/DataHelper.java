package com.dktes.medileaf;

import android.util.JsonReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DataHelper {

    /****
     * * * * NOTE * * * *
     * understand the structure of data.json file in assets folder of the project before comprehending this code
     * * * * * ** * * * *
     * ****/

    // The leaf object is used to store the information of leaf we want to show on result activity
    public Leaf leaf;

    public DataHelper(JsonReader reader, String leafName) throws IOException {
        // This is a constructor of the class.
        // This constructor takes to arguments: a json reader object and a leaf name
        // Json reader is used to read all the data from the assets folder data.json and store them into a list
        // the leafName parameter is used to find a particular leaf from the list
        // When the leaf is found, its information will be stored into the 'leaf' object which will be later used by result activity
        try {
            // getLeaf function returns a list of Leaf objects
            List<Leaf> leaves = getLeaf(reader);

            // Find out the particular leaf from the list and store it into the 'leaf' object
            for (Leaf leaf: leaves) {
                if (leaf.getLeafName().equals(leafName)) {
                    this.leaf = leaf;
                    break;
                }
            }
        } catch (URISyntaxException e) {
            // If anything arises in middle, throw an exception and exit
            throw new RuntimeException(e);
        } finally {
            // you should close the json reader after reading is complete to avoid data leak and release the memory used by json reader
            reader.close();
        }
    }

    private List<Leaf> getLeaf(JsonReader reader) throws IOException, URISyntaxException {

        // A list is created to save each leaf's information separately as an object
        List<Leaf> leaves = new ArrayList<>();

        // beginArray function starts reading the JSON file
        // Note: data.json has stored the leaf information in a JSON array. So, to read that array, we should call beginArray function
        reader.beginArray();
        while (reader.hasNext()) {
            // readLeaf function reads each leaf information from the JSON array in the data.json and returns a Leaf object
            leaves.add(readLeaf(reader));
        }
        // exit from the array and return the list of leaf information to the calling function
        reader.endArray();
        return leaves;
    }

    private Leaf readLeaf(JsonReader reader) throws IOException {
        // Create separate string to read each attribute of a leaf
        String leafName = null, scientificName = null, description = null, usage = null;
        String origin = null, feature = null;

        // In a json array, we have individual objects of the leaf. that will be read one at a time
        reader.beginObject();
        while (reader.hasNext()) {
            // reader.hasNext function checks if the object is not reach to end while reading

            // 'name' stores the name of attribute of a object
            String name = reader.nextName();

            // From here, check whether what is the name of the attribute. Accordingly, assign the attribute value to its designated variable
            switch (name) {
                case "leafname":
                    leafName = reader.nextString();
                    break;
                case "sciname":
                    scientificName = reader.nextString();
                    break;
                case "description":
                    description = reader.nextString();
                    break;
                case "usage":
                    usage = reader.nextString();
                    break;
                case "origin":
                    origin = reader.nextString();
                    break;
                case "feature":
                    feature = reader.nextString();
                    break;
                default:
                    // If the name of attribute does not matches any of above, skip that attribute
                    reader.skipValue();
                    break;
            }
        }
        // close the object so that we can start reading next object
        reader.endObject();

        // Initialize and return a Leaf object to the calling function
        return new Leaf(leafName, scientificName, description, usage, origin, feature);
    }

}
