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
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Lord One on 6/8/2016.
 */

public class TrainActivity extends AppCompatActivity {

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    new DoFeaturing().execute();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String selectedPath = getIntent().getStringExtra("path");

        loadImagesDirectories(selectedPath);
        //TODO: featuring, get 100% images of each category, run svm, save svm to file,...
        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, TrainActivity.this, mLoaderCallback);
        } else {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }

    void loadImagesDirectories(String path) {
        try {
            File dir = new File(path);
            String[] _dirList = dir.list();
            GV.targetNames = _dirList.clone();
            GV.mapTarget = new JSONObject();
            GV.ivMapTarget = new JSONObject();
            for (int i = 0; i < GV.targetNames.length; i++) {
                GV.mapTarget.put(GV.targetNames[i], i);
                GV.ivMapTarget.put(String.valueOf(i), GV.targetNames[i]);
            }

            GV.fileDirs = new ArrayList<String>();
            GV._trainTarget = new ArrayList<String>();

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
                                GV.fileDirs.add(_filePath);
                                GV._trainTarget.add(_subDir);
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

    public void trainSVMFunction(int n_images, long feature_time) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading SVM");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
        try {
            int i, j;

            CvSVMParams params = new CvSVMParams();
            params.set_svm_type(CvSVM.C_SVC);
            params.set_kernel_type(CvSVM.LINEAR);
//            params.set_term_crit(new TermCriteria(TermCriteria.EPS, 100, 1e-6));

            CvSVM svm = new CvSVM();

            Mat label = new Mat(1, GV._trainTarget.size(), CvType.CV_32S);

            for (i = 0; i < GV._trainTarget.size(); i++) {
                label.put(0, i, GV.mapTarget.getInt(GV._trainTarget.get(i)));
            }

            Date begTime = new Date();
            svm.train(GV.sogi, label);
            Date endTime = new Date();
            long trainingTime = (endTime.getTime() - begTime.getTime()) / 1000;

            FileIO.writeSVMToFile(svm);
            ImageExecutive.releaseMemory();

            AlertDialog.Builder builder= new AlertDialog.Builder(this);

            builder.setMessage("Processed successfully!" + '\n' +
                    n_images + " image(s) featured!" + '\n' +
                    "Featuring Time: " + feature_time + 's' + '\n' +
                    "SVM training time: " + trainingTime + "s");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent = new Intent(TrainActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    private class DoFeaturing extends AsyncTask<Void, Integer, Integer> {

        Date beginTime = new Date();
        ProgressDialog progressDialog = new ProgressDialog(TrainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Images are being featured!");
            progressDialog.setMessage("0 image featured");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                GV.svm = null; // release memory
                GV.sogi = new Mat(GV._trainTarget.size(), GV.nCol, CvType.CV_32F);
                GV.cnt = 0;
                for(String _file : GV.fileDirs) {
                    ImageExecutive.load1Image(_file);
                    ImageExecutive.computeGradient();
                    ImageExecutive.computeFeatures();
                    ImageExecutive.normalizeFeatures_train();
                    ++ GV.cnt;
                    publishProgress(GV.cnt);
                }
                return GV.cnt;
            }
            catch (Exception e) {
                return - GV.cnt;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setMessage(String.valueOf(progress[0] + "/" + GV.fileDirs.size() + " image(s) featured!"));
//            if(progress[0] == GV.fileDirs.size())
//                progressDialog.setMessage("Writing features onto SD Card!");
        }

        @Override
        protected void onPostExecute(Integer result) {
            Date endFeatureTime = new Date();
            long featureTime = (endFeatureTime.getTime() - beginTime.getTime() ) / 1000;

            AlertDialog.Builder builder = new AlertDialog.Builder(TrainActivity.this);
            if(result >= 0) {

                progressDialog.dismiss();
                trainSVMFunction(result, featureTime);

            }
            else {
                builder.setMessage("Unsuccessful attempt!");
                progressDialog.dismiss();
                AlertDialog alert = builder.create();
                alert.show();
            }

        }
    }

}