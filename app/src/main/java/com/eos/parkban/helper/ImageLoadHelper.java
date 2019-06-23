package com.eos.parkban.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import com.eos.parkban.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class ImageLoadHelper {

    private static final String TAG = "ImageLoadHelper";
    public static final int REQUEST_IMAGE_WIDTH = 640;

    private static ImageLoadHelper instance;

    private ImageLoadHelper() {
    }

    public static ImageLoadHelper getInstance() {
        if (instance == null) {
            instance = new ImageLoadHelper();
        }
        return instance;
    }

    public Bitmap loadImage(Context context, String imageFilePath) {
        Bitmap orgImg = null;
        Bitmap cropImg = null;

        if (imageFilePath.contains("/")) {
            orgImg = loadBitmapFromFile(imageFilePath, REQUEST_IMAGE_WIDTH, REQUEST_IMAGE_WIDTH);
        } else {
            orgImg = loadPhotoFromPrivateFile(context, imageFilePath);
        }

        if (orgImg != null)
            cropImg = cropCenterBitmap(orgImg);

        return cropImg;
        // return orgImg;
    }

    public static Bitmap cropCenterBitmap(Bitmap sourceBitmap) {
        if (sourceBitmap.getWidth() >= sourceBitmap.getHeight()) {
            return Bitmap.createBitmap(sourceBitmap,
                    sourceBitmap.getWidth() / 2 - sourceBitmap.getHeight() / 2, 0,
                    sourceBitmap.getHeight(), sourceBitmap.getHeight());
        } else {
            return Bitmap.createBitmap(sourceBitmap,
                    0, sourceBitmap.getHeight() / 2 - sourceBitmap.getWidth() / 2,
                    sourceBitmap.getWidth(), sourceBitmap.getWidth()
            );
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            while ((height / inSampleSize) >= reqHeight
                    && (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    public static Bitmap loadBitmapFromFile(String fileName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        Bitmap result = BitmapFactory.decodeFile(fileName, options);

        try {
            ExifInterface exif = new ExifInterface(fileName);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
                result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in read Photo ExifInterface", e);
        }
        return result;
    }

    public static Bitmap loadPhotoFromPrivateFile(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            Bitmap photo = BitmapFactory.decodeStream(fis);
            fis.close();
            return photo;
        } catch (Exception e) {
            Log.d(TAG, "Error in loadPhotoFromPrivateFile", e);
        }
        return null;
    }

    public Bitmap convertFileToBitmap(Context context , String fileName){
        File storageDir =context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outFile = new File(storageDir, fileName);
        String absolutePath = outFile.getAbsolutePath();
        Bitmap image = getInstance().loadImage(context, absolutePath);
        return image;
    }

    public int[] convertBitmapToIntArray(Bitmap image){

//        Mat imIn = new Mat();
//        Utils.bitmapToMat(image, imIn);
//        Imgproc.resize(imIn, imIn, new Size(400, imIn.rows() * 400 / imIn.cols()), 0, 0, Imgproc.INTER_CUBIC);
//        Bitmap finalBitmap = Bitmap.createBitmap(imIn.cols(), imIn.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imIn,finalBitmap);

        byte[] byteArray = null;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, 300, 200, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 10, stream);
        byteArray = stream.toByteArray();

        //Convert Byte Array To Int Array
        int[] ret = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            ret[i] = byteArray[i] & 0xff; // Range 0 to 255, not -128 to 127
        }

        return ret;
    }

    public int[] convertImageFileToIntArray(Context context , String fileName){
        byte[] byteArray = null;
        File storageDir =context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outFile = new File(storageDir, fileName);
        String absolutePath = outFile.getAbsolutePath();
        Bitmap image = getInstance().loadImage(context, absolutePath);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, 300, 200, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 10, stream);
        byteArray = stream.toByteArray();

        //Convert Byte Array To Int Array
        int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = byteArray[i] & 0xff; // Range 0 to 255, not -128 to 127
        }

        return intArray;
    }

    public Bitmap convertDrawableToBitmap(Context context ,int drawableID){
        return BitmapFactory.decodeResource(context.getResources(),
                drawableID);
    }

}
