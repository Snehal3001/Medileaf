package com.dktes.medileaf;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;



public class ClassificationHelper {

    // This class is crucial. Here, we import the Tensorflow deep learning model and use it to classify the image
    // this tensor image is feed to the DL model which will result into classification
    private final TensorImage image;

    // All tensorflow models that are used on mobile are stored as a 'tensorflow lite' model (tflite model)
    // Each tensorflow lite model is stored in binary format which needed to be interpreted
    // All weights, learnings, and network structure is encoded in binary hence an interpreter of it is necessary
    // The following interpreter understands a tflite model and can work as similar as a tensorflow model
    private final Interpreter model;

    // The following activity is for reference to return the results
    private final Activity activity;

    public ClassificationHelper(Activity activity, TensorImage image) throws IOException {

        // Initialize the two major variables of the class
        this.activity = activity;
        this.image = image;

        // load the name of the model
        String modelPath = "medinet.tflite";

        /*
        * AssetFileDescriptor is a class that provides access to a file in the application's assets or resources directory.
        * It is a file descriptor that provides additional information about the file, such as its length, offset, and type.
        *
        * we will get the assets associated with our current activity and will open the modelPath file
        * openFd is used to open a new file descriptor for a given file, in our case it is medinet.tflite
        * */
        try (AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
             // As usual, to read a file we should use a file input stream and pass the file descriptor to the parameter
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {

            /* FileChannel is a class that represents a channel for reading, writing, or manipulating a file.
            It is used to read and write data to a file, and also provides additional functionality such as mapping a region of the file
            into memory.
             */
            FileChannel fileChannel = inputStream.getChannel();
            // find where the file starts
            long startOffset = fileDescriptor.getStartOffset();
            // find what is the length of the file
            long declareLength = fileDescriptor.getDeclaredLength();

            // use a file channel, provide it to the interpreter to initialize a new model
            // the map() function loads the medinet.tflite into the memory and declare it as READ ONLY variable
            // When the file is loaded, the map() function returns a byte buffer or a byte array which contains model weights and connections
            // When the interpreter class read it, it will create the model from this byte array
            this.model = new Interpreter(fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declareLength));

        } catch (IOException e) {
            throw new IOException(e.getMessage() + " -- occurred when loading the model in ClassificationHelper constructor");
        }

    }

    public String classify() {

        // At end of each neural network, we get probabilities which represents the likelihood of a image belonging to certain class
        // If a node has higher probability at end, the image will belong to that class

        // A tensor buffer is used to store those probabilities but we need to tell it how many classes are there in the last layer of model
        // In our case, there are 30. The Float32 type represents probability in range of 0 to 1 in proper manner
        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, 30}, DataType.FLOAT32);

        // The classification happens exactly at this point!!!
        // the tensor image and its buffer is passed to the model to execute and classify it
        // When the classification is completed, the results are stored in the probability buffer that we created
        model.run(this.image.getBuffer(), probabilityBuffer.getBuffer());

        // Now, we call a function getLabels to find the name of each class with its probability
        Map<String, Float> results = getLabels(probabilityBuffer);

        // bestResult() function returns the name of class which has highest probability
        return this.bestResult(results);
    }
    private Map<String, Float> getLabels(TensorBuffer probabilityBuffer) {

        // This list stores the names of classes from the labels.txt in the assets folder
        List<String> associatedAxisLabels = null;

        try {
            // Initialize the name of file in which labels of each class are stored
            String labelsPath = "labels.txt";
            // There is a built-in function called loadLabels() to load the list of labels into our program
            // We need to pass the target activity name which contains those assets and the name of the assets
            associatedAxisLabels = FileUtil.loadLabels(this.activity, labelsPath);
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }

        // We need to normalize the probabilities because they occur in range of 0 to 255.
        // The probabilities will be converted to 0 to 1 range
        TensorProcessor probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

        // this map stores a list of pair of class label and its probability
        Map<String, Float> floatMap = null;
        if (null != associatedAxisLabels) {
            // TensorLabel is a built-in tensorflow label processing class which will map the labels with their target probabilities
            TensorLabel labels = new TensorLabel(associatedAxisLabels, probabilityProcessor.process(probabilityBuffer));

            // getMapWithFloatValue() returns a map of string and float with labels and probabilities in it.
            floatMap = labels.getMapWithFloatValue();
        }
        return floatMap;
    }
    private String bestResult(Map<String, Float> results) {
        // This method finds the name of class or label which has highest probability through simple comparison
        // The following variable currentMax is used to compare probabilities and store the max probability in the map
        double currentMax = 0.0;

        // This variable will be used to find the name of class associated with a given probability
        String maxLabel = "";

        for (Map.Entry<String, Float> mapElement : results.entrySet()) {
            // getValue returns the probability for a class
            Float prob = mapElement.getValue();
            if (prob > currentMax) {
                currentMax = prob;
                // getKey returns the name of the class
                maxLabel = mapElement.getKey();
            }
        }

        return maxLabel;
    }
}
