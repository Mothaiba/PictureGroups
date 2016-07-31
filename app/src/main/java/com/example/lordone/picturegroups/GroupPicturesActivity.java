package com.example.lordone.picturegroups;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lordone.picturegroups.BaseClasses.FileIO;
import com.example.lordone.picturegroups.BaseClasses.GV;
import com.example.lordone.picturegroups.BaseClasses.ImageExecutive;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lord One on 6/26/2016.
 */



public class GroupPicturesActivity extends AppCompatActivity {

    private BaseLoaderCallback mLoaderCallback_GroupPicturesActivity = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    new DoGrouping().execute();

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

        GV._testDir = getIntent().getStringExtra("path");

        loadImagesDirectories(GV._testDir);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, GroupPicturesActivity.this, mLoaderCallback_GroupPicturesActivity);
        } else {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback_GroupPicturesActivity.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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

    private class DoGrouping extends AsyncTask<Void, Integer, String> {

        ProgressDialog progressDialog = new ProgressDialog(GroupPicturesActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Please wait!");
            progressDialog.setMessage("Loading svm...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String toReturn = "";
            String toGo = "";
            int cnt = 0;

            JSONObject grouped = new JSONObject();
            try {
                Iterator<String> categories = GV.mapTarget.keys();
                while (categories.hasNext()) {
                    grouped.put((String) categories.next(), 0);
                }
            }
            catch (Exception e) {}

            try {
                FileIO.readSVMFromFile();
                publishProgress(cnt);
                for(String _file : GV.fileDirs) {
                    GV.testMat = new Mat(1, GV.nCol, CvType.CV_32F);
                    ImageExecutive.load1Image(GV._testDir + File.separator + _file);
                    ImageExecutive.computeGradient();
                    ImageExecutive.computeFeatures();
                    ImageExecutive.normalizeFeatures_test();
                    toGo = GV.ivMapTarget.getString(String.valueOf((int) GV.svm.predict(GV.testMat)));
                    FileIO.relocateFile(GV._testDir, _file, toGo);

                    grouped.put(toGo, grouped.getInt(toGo) + 1);

                    publishProgress(++cnt);
                }

                Iterator categories = grouped.keys();
                while(categories.hasNext()) {
                    String category = (String) categories.next();
                    int n_grouped = grouped.getInt(category);
                    if(n_grouped == 1)
                        toReturn += n_grouped + " picture copied to folder " + category.toUpperCase() + "\n";
                    else if(n_grouped > 1)
                        toReturn += n_grouped + " pictures copied to folder " + category.toUpperCase() + "\n";
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return toReturn;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setMessage(String.valueOf(progress[0] + "/" + GV.fileDirs.size() + " image(s) processed!"));
        }

        @Override
        protected void onPostExecute(String result) {

            AlertDialog.Builder builder = new AlertDialog.Builder(GroupPicturesActivity.this);

            builder.setMessage("Task completed!" + '\n' + result);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(GroupPicturesActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });
            progressDialog.dismiss();
            AlertDialog alert = builder.create();
            alert.show();

        }
    }
}
