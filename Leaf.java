package com.dktes.medileaf;
import java.io.Serializable;

public class Leaf implements Serializable {

    // The Leaf class is used to store information about a single leaf in the data.json file
    // This is important because we should not read JSON files as it is.
    // Storing the JSON data into a proper serializable object is useful to get and set data properly
    // hence, we can avoid the data leak and securely store the data using Java encapsulation technique

    private final String leafName;
    private final String scientificName;
    private final String description;
    private final String usage;
    private final String origin;
    private final String feature;

    public Leaf(String leafName, String scientificName, String description, String usage, String origin, String feature) {
        this.leafName = leafName;
        this.scientificName = scientificName;
        this.description = description;
        this.usage = usage;
        this.origin = origin;
        this.feature = feature;
    }

    public String getLeafName() {
        return leafName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public String getOrigin() {
        return origin;
    }

    public String getFeature() {
        return feature;
    }

}
