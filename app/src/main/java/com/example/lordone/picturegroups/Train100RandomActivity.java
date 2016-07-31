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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.example.lordone.picturegroups.AsynClasses.DoClassifying;
import com.example.lordone.picturegroups.BaseClasses.FileIO;
import com.example.lordone.picturegroups.BaseClasses.GV;
import com.example.lordone.picturegroups.BaseClasses.ImageExecutive;

/**
 * Created by Lord One on 6/8/2016.
 */

public class Train100RandomActivity extends AppCompatActivity {

    Button mainmenu_button;
    Button continue_test_button;
    TextView title;

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
        setContentView(R.layout.normal_test_layout);

        title = (TextView) findViewById(R.id.title);
        title.setText("Test Result");

        String selectedPath = getIntent().getStringExtra("path");
        System.out.print(selectedPath);

        loadImagesDirectories(selectedPath);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OPENCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, Train100RandomActivity.this, mLoaderCallback);
        } else {
            Log.d("OPENCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        mainmenu_button = (Button) findViewById(R.id.mainmenu_button);
        mainmenu_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Train100RandomActivity.this, MainActivity.class);
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
            GV.targetNames = _dirList.clone();
            GV.mapTarget = new JSONObject();
            GV.ivMapTarget = new JSONObject();
            for (int i = 0; i < GV.targetNames.length; i++) {
                GV.mapTarget.put(GV.targetNames[i], i);
                GV.ivMapTarget.put(String.valueOf(i), GV.targetNames[i]);
            }

            GV._trainListDirs = new ArrayList<>();
            GV._testListDirs = new ArrayList<>();
            GV._trainTarget = new ArrayList<>();
            GV._testTarget = new ArrayList<>();

            // _dirList: list of folders that contain images
            if (_dirList != null) {
                for (String _subDir : _dirList) {
                    File subDir = new File(path + File.separator + _subDir);
                    // for each category, read all of its images
                    if (!_subDir.startsWith(".") && subDir.isDirectory()) {

                        String[] _files = subDir.list();

                        List<String> list = new ArrayList<>(Arrays.asList(_files));
                        Collections.shuffle(list);
                        for(int i = 0; i < GV.trainSize; i++) {
                            GV._trainListDirs.add(path + File.separator + _subDir + File.separator + list.get(i));
                            GV._trainTarget.add(_subDir);
                        }
                        for(int i = GV.trainSize; i < list.size(); i++) {
                            GV._testListDirs.add(path + File.separator + _subDir + File.separator + list.get(i));
                            GV._testTarget.add(_subDir);
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

    private class DoFeaturing extends AsyncTask<Void, Integer, Integer> {

        Date beginTime = new Date();
        ProgressDialog progressDialog = new ProgressDialog(Train100RandomActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(Train100RandomActivity.this);
        AlertDialog.Builder builder2 = new AlertDialog.Builder(Train100RandomActivity.this);
        AlertDialog alert;
        AlertDialog alert2;

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
                for(String _file : GV._trainListDirs) {
                    ImageExecutive.load1Image(_file);
                    ImageExecutive.computeGradient();
                    ImageExecutive.computeFeatures();
                    ImageExecutive.normalizeFeatures_train();
                    ++GV.cnt;
                    publishProgress(GV.cnt);
                }

                Date endFeatureTime = new Date();
                long featureTime = (endFeatureTime.getTime() - beginTime.getTime()) / 1000;
                builder.setMessage("Processed successfully!" + '\n' +
                        GV.cnt + " image(s) featured!" + '\n' +
                        "Featuring Time: " + featureTime + 's');

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                Train100RandomActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert = builder.create();
                        alert.show();
                    }
                });


                trainSVMFunction();
                Date endTrainSVMTime = new Date();
                int trainSVMTime = (int) (endTrainSVMTime.getTime() - endFeatureTime.getTime()) / 1000;
                publishProgress(-trainSVMTime);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return GV.cnt;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            if(progress[0] > 0) {
                progressDialog.setMessage(String.valueOf(progress[0] + "/" + GV._trainListDirs.size() + " image(s) featured!"));
                if (progress[0] == GV._trainListDirs.size()) {
                    progressDialog.setTitle("Training SVM");
                    progressDialog.setMessage("Please wait...");
                }
            }
            else {
                builder2.setMessage("SVM training time: " + (-progress[0]) + "s");
                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert2 = builder2.create();
                alert2.show();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {

            progressDialog.dismiss();
            new DoClassifying(Train100RandomActivity.this).execute();

        }

        public void trainSVMFunction() {
            try {
                int i, j;

                CvSVMParams params = new CvSVMParams();
                params.set_svm_type(CvSVM.C_SVC);
                params.set_kernel_type(CvSVM.LINEAR);
//                params.set_term_crit(new TermCriteria(TermCriteria.EPS, 200, 1e-8));

                GV.svm = new CvSVM();

                Mat label = new Mat(1, GV._trainTarget.size(), CvType.CV_32S);

                for (i = 0; i < GV._trainTarget.size(); i++) {
                    label.put(0, i, GV.mapTarget.getInt(GV._trainTarget.get(i)));
                }

                GV.svm.train(GV.sogi, label);

                FileIO.writeSVMToFile(GV.svm);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}