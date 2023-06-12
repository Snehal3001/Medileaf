package com.dktes.medileaf;

import android.graphics.Bitmap;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;


public class ImageHelper {

    // This class is important to convert a image from camera and gallery into a Tensor image
    // Tensor images are used by a TensorFlow deep learning model for tasks like classification and object detection
    // We cannot pass a raw image from gallery and camera directly to a DL model
    // We need to convert the image and reshape it to 150x150 RGB image

    public TensorImage tensorImage;

    public ImageHelper(Bitmap image) {

        // Initialize a tensor image
        this.tensorImage = new TensorImage(DataType.FLOAT32);

        // the load function loads a bitmap image into a tensor image automatically
        this.tensorImage.load(image);

        // Now, we preprocess the tensor image to reshape it into a 150x150 RGB image
        this.tensorImage = getImageProcessor().process(this.tensorImage);

    }

    // This function simply returns the transformed tensor image to the calling function
    public TensorImage getTensorImage() {
        return this.tensorImage;
    }

    private ImageProcessor getImageProcessor() {
        // An image processor is the pre-processing class applied to tensor images before classifying them into appropriate class
        // Here, we apply a resize operation (ResizeOp) to resize the image into a 150x150 RGB image
        return new ImageProcessor.Builder()
                .add(new ResizeOp(150, 150, ResizeOp.ResizeMethod.BILINEAR))
                .build();
    }
}
