package com.example.lordone.picturegroups;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lordone.picturegroups.AsynClasses.DoClassifying;
import com.example.lordone.picturegroups.BaseClasses.GV;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by Lord One on 6/25/2016.
 */
public class TestAccuracyActivity extends AppCompatActivity {

    Button mainmenu_button;
    Button continue_test_button;
    TextView title;

    private BaseLoaderCallback mLoaderCallback_TestAccuracyActivity = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    new DoClassifying(TestAccuracyActivity.this).execute();

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
        title.setText("Test Accuracy Result");

        GV._testDir = getIntent().getStringExtra("path");
        loadImagesDirectories(GV._testDir);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, TestAccuracyActivity.this, mLoaderCallback_TestAccuracyActivity);
        } else {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback_TestAccuracyActivity.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        mainmenu_button = (Button) findViewById(R.id.mainmenu_button);
        mainmenu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestAccuracyActivity.this, MainActivity.class);
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
            String[] _dirList = dir.list();

            GV._testListDirs = new ArrayList<String>();
            GV._testTarget = new ArrayList<String>();

            int cnt = 0;

            // _dirList: list of folders that contain images
            if (_dirList != null) {
                for (String _subDir : _dirList) {
                    File subDir = new File(path + File.separator + _subDir);
                    // for each category, read all of its images
                    if (!_subDir.startsWith(".") && subDir.isDirectory()) {

                        String[] _files = subDir.list();
                        for (String _file : _files) {
                            String _filePath = path + File.separator + _subDir + File.separator + _file;
                            File file = new File(_filePath);
                            // for each individual image
                            if (!_file.startsWith(".") && file.isFile()) {
                                GV._testListDirs.add(_filePath);
                                GV._testTarget.add(_subDir);
                            }
                        }

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

}
