package com.example.lordone.picturegroups;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.lordone.picturegroups.BaseClasses.FileIO;
import com.example.lordone.picturegroups.BaseClasses.GV;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ImageButton mTrainButton;
    ImageButton mTrain100RandomButton;
    ImageButton mStaticTestButton;
    ImageButton mCameraTestButton;
    ImageButton mGroupButton;
    ImageButton mTestAccuracyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTrainButton = (ImageButton) findViewById(R.id.trainButton);
        mTrainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Swing).duration(500).playOn(mTrainButton);
                Intent intent = new Intent(MainActivity.this, FileBrowserActivity.class);
                intent.putExtra("function", FileBrowserActivity.train_func);
                startActivity(intent);
            }
        });

        mTrain100RandomButton = (ImageButton) findViewById(R.id.train100RandomButton);
        mTrain100RandomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Swing).duration(500).playOn(mTrain100RandomButton);
                Intent intent = new Intent(MainActivity.this, FileBrowserActivity.class);
                intent.putExtra("function", FileBrowserActivity.train_100_func);
                startActivity(intent);
            }
        });



        mStaticTestButton = (ImageButton) findViewById(R.id.staticTestButton);
        mStaticTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GV.svm != null) {
                    YoYo.with(Techniques.Swing).duration(500).playOn(mStaticTestButton);
                    Intent intent = new Intent(MainActivity.this, FileBrowserActivity.class);
                    intent.putExtra("function", FileBrowserActivity.static_test_func);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "You must train first!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mCameraTestButton = (ImageButton) findViewById(R.id.cameraTestButton);
        mCameraTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GV.svm != null) {
                    YoYo.with(Techniques.Swing).duration(500).playOn(mCameraTestButton);
                    Intent intent = new Intent(MainActivity.this, CameraTestActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "You must train first!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mTestAccuracyButton = (ImageButton) findViewById(R.id.testAccuracyButton);
        mTestAccuracyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GV.svm != null) {
                    YoYo.with(Techniques.Swing).duration(500).playOn(mTestAccuracyButton);
                    Intent intent = new Intent(MainActivity.this, FileBrowserActivity.class);
                    intent.putExtra("function", FileBrowserActivity.test_accuracy_func);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "You must train first!", Toast.LENGTH_LONG).show();
                }
            }
        });

        mGroupButton = (ImageButton) findViewById(R.id.groupButton);
        mGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GV.svm != null) {
                    YoYo.with(Techniques.Swing).duration(500).playOn(mGroupButton);
                    Intent intent = new Intent(MainActivity.this, FileBrowserActivity.class);
                    intent.putExtra("function", FileBrowserActivity.group_picture_func);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(MainActivity.this, "You must train first!", Toast.LENGTH_LONG).show();
                }
            }
        });

//        new InitLoadSVMThread(this).start();
        new InitLoadSVM().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}

class InitLoadSVM extends AsyncTask<Activity, Void, Void> {
    Activity activity;
    private BaseLoaderCallback mLoaderCallback_loadSVM = new BaseLoaderCallback(activity) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    FileIO.readSVMFromFile();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

//    @Override
//    protected void onPreExecute() {
//
//    }

    @Override
    public Void doInBackground(Activity... _activity) {
        activity = _activity[0];

        if(GV.svm != null)
            return null;

        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, activity, mLoaderCallback_loadSVM);
        } else {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback_loadSVM.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        return null;
    }
}


//class InitLoadSVMThread extends Thread {
//    Activity activity;
//    private BaseLoaderCallback mLoaderCallback_loadSVM = new BaseLoaderCallback(activity) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status) {
//                case LoaderCallbackInterface.SUCCESS:
//                {
//                    Log.i("OPENCV", "OpenCV loaded successfully");
//                    FileIO.readSVMFromFile();
//
//                } break;
//                default:
//                {
//                    super.onManagerConnected(status);
//                } break;
//            }
//        }
//    };
//
//    public InitLoadSVMThread(Activity _activity) {
//        activity = _activity;
//    }
//
//    public void run() {
//        if(GV.svm != null)
//            return;
//        if (!OpenCVLoader.initDebug()) {
//            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, activity, mLoaderCallback_loadSVM);
//        } else {
//            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
//            mLoaderCallback_loadSVM.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }
//    }
//}