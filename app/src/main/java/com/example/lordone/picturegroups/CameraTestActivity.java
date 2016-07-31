package com.example.lordone.picturegroups;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.lordone.picturegroups.BaseClasses.FileIO;
import com.example.lordone.picturegroups.BaseClasses.GV;
import com.example.lordone.picturegroups.BaseClasses.ImageExecutive;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lord One on 6/26/2016.
 */
public class CameraTestActivity extends Activity {

    private static final int REQUEST_TAKE_PHOTO = 1;

    private BaseLoaderCallback mLoaderCallback_CameraTestActivity = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    new DoClassifying().execute();
                    System.gc();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    Button backButton;
    Button shootAgainButton;
    LinearLayout linearLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_test_layout);
        dispatchTakePictureIntent();

        linearLayout = (LinearLayout) findViewById(R.id.raw_test_Linear_layout);

        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        shootAgainButton = (Button) findViewById(R.id.shoot_again_button);
        shootAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
    }

    String _imageTaken;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(GV._cameraTestDir);

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        _imageTaken = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == this.RESULT_OK) {
            if (!OpenCVLoader.initDebug()) {
                Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, CameraTestActivity.this, mLoaderCallback_CameraTestActivity);
            } else {
                Log.d("OPENCV", "OpenCV library found inside package. Using it!");
                mLoaderCallback_CameraTestActivity.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }
    }

    private class DoClassifying extends AsyncTask<Void, Integer, String> {

        ProgressDialog progressDialog = new ProgressDialog(CameraTestActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Please wait!");
            progressDialog.setMessage("Loading svm...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String categoryObtained ="unclassified";
            try {
                FileIO.readSVMFromFile();
                publishProgress(0);
                GV.testMat = new Mat(1, GV.nCol, CvType.CV_32F);
                ImageExecutive.load1Image(_imageTaken);
                ImageExecutive.computeGradient();
                ImageExecutive.computeFeatures();
                ImageExecutive.normalizeFeatures_test();
                categoryObtained = GV.ivMapTarget.getString(String.valueOf((int) GV.svm.predict(GV.testMat))) + '\n';
                ImageExecutive.releaseMemory();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return categoryObtained;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setMessage("classifying...");
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                progressDialog.dismiss();

                Bitmap photo = ImageExecutive.getBitmap(_imageTaken);
                ImageView imageView = new ImageView(CameraTestActivity.this);
                imageView.setImageBitmap(photo);
                imageView.setMaxHeight(888);
                imageView.setAdjustViewBounds(true);

                TextView resultTextView = new TextView(CameraTestActivity.this);
                resultTextView.setText("predict: " + result);
                resultTextView.setGravity(Gravity.CENTER);
                resultTextView.setTextSize(20);

                linearLayout.addView(imageView);
                linearLayout.addView(resultTextView);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
