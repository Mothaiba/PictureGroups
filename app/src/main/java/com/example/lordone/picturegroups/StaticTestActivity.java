package com.example.lordone.picturegroups;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lordone.picturegroups.BaseClasses.FileIO;
import com.example.lordone.picturegroups.BaseClasses.GV;
import com.example.lordone.picturegroups.BaseClasses.ImageExecutive;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Lord One on 6/25/2016.
 */
public class StaticTestActivity extends AppCompatActivity {

    Button mainmenu_button;
    Button continue_test_button;
    TextView title;

    private BaseLoaderCallback mLoaderCallback_StaticTestActivity = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    new DoClassifying().execute();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_test_layout);
        
        title = (TextView) findViewById(R.id.title);
        title.setText("Static Test Result");

        GV._testDir = getIntent().getStringExtra("path");

        if(getIntent().hasExtra("file_name")) {
            GV.fileDirs = new ArrayList<>();
            GV.fileDirs.add(getIntent().getStringExtra("file_name"));
            System.out.print("OK");
        }
        else {
            loadImagesDirectories(GV._testDir);
        }

        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, StaticTestActivity.this, mLoaderCallback_StaticTestActivity);
        } else {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback_StaticTestActivity.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        mainmenu_button = (Button) findViewById(R.id.mainmenu_button);
        mainmenu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StaticTestActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        continue_test_button = (Button) findViewById(R.id.continue_test_button);
        continue_test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }



    void loadImagesDirectories(String path) {

        try {

            File dir = new File(path);
            String[] _fileList = dir.list();
            GV.fileDirs = new ArrayList<>();

            // _fileList: list of image file names
            if (_fileList != null) {
                for (String _file : _fileList) {
                    File file = new File(path + File.separator + _file);
                    if (!_file.startsWith(".") && file.isFile()) {
                        GV.fileDirs.add(_file);
                    }
                }
            } else {
                Toast.makeText(this, "Directory empty", Toast.LENGTH_LONG).show();
            }

            System.out.print("OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class DoClassifying extends AsyncTask<Void, Integer, String> {

        Date beginTime;
        ProgressDialog progressDialog = new ProgressDialog(StaticTestActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Please wait!");
            progressDialog.setMessage("Loading svm...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.raw_test_Linear_layout);

            String toShow = "";
            try {
                FileIO.readSVMFromFile();
                beginTime = new Date();
                int cnt = 0;
                publishProgress(cnt);
                for(final String _file : GV.fileDirs) {
                    GV.testMat = new Mat(1, GV.nCol, CvType.CV_32F);
                    ImageExecutive.load1Image(GV._testDir + File.separator + _file);
                    ImageExecutive.computeGradient();
                    ImageExecutive.computeFeatures();
                    ImageExecutive.normalizeFeatures_test();
                    final String predictCategory = GV.ivMapTarget.getString(String.valueOf((int) GV.svm.predict(GV.testMat)));

                    if(cnt < GV.max_number_images_showed_in_static_test)
                        StaticTestActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ImageView imageView = new ImageView(StaticTestActivity.this);
                                TextView textView = new TextView(StaticTestActivity.this);

                                Bitmap photo = ImageExecutive.getBitmap(GV._testDir + File.separator + _file);
                                imageView.setImageBitmap(photo);
                                imageView.setMaxHeight(400);
                                imageView.setAdjustViewBounds(true);

                                textView.setText("predict: " + predictCategory);
                                textView.setGravity(Gravity.CENTER);

                                linearLayout.addView(imageView);
                                linearLayout.addView(textView);
                            }
                        });

                    toShow += _file + ": " + predictCategory + '\n';
                    publishProgress(++cnt);
                }

                if(cnt > GV.max_number_images_showed_in_static_test)
                    StaticTestActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = new TextView(StaticTestActivity.this);
                            textView.setText("we show only " + GV.max_number_images_showed_in_static_test + " first samples!");

                            textView.setGravity(Gravity.CENTER);
                            linearLayout.addView(textView);
                        }
                    });

                return toShow;
            }
            catch (Exception e) {
                return toShow;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setMessage(String.valueOf(progress[0] + "/" + GV.fileDirs.size() + " image(s) classified!"));
        }

        @Override
        protected void onPostExecute(String result) {
            Date endFeatureTime = new Date();
            long classifyTime = endFeatureTime.getTime() - beginTime.getTime();

            AlertDialog.Builder builder = new AlertDialog.Builder(StaticTestActivity.this);

            builder.setMessage("Task finished!" + '\n' +
                    "Classifying Time: " + String.valueOf(classifyTime) + "ms" + '\n' + '\n' +
                    result);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            progressDialog.dismiss();
            AlertDialog alert = builder.create();
            alert.show();


        }
    }
}
