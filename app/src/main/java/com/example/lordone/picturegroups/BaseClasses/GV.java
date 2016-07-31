package com.example.lordone.picturegroups.BaseClasses;

import android.os.Environment;

import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.ml.CvSVM;

import java.util.ArrayList;

/**
 * Created by Lord One on 6/20/2016.
 */
public class GV {
    public static ArrayList<String> fileDirs;
    public static ArrayList<String> target;
    public static JSONObject mapTarget;
    public static JSONObject ivMapTarget;
    public static String[] targetNames = null;

    public static double eps = 1e-9;
    public static double pi = Math.acos(-1);

    public static int maxDist = 3;
    public static double threshold[] = { eps, 15, 35 };

    public static int norient = 6;
    public static double iorient = 180. / norient;
    public static int ninten = 3;
    public static int npos = 4;

    public static int [][] image;
    public static int xres;
    public static int yres;

    public static int[][] inten;
    public static int[][] orient;

    public static double[][][][][][] features = new double [maxDist + 1][norient + 1][ninten + 1][norient + 1][ninten + 1][npos + 1];
    public static Mat sogi;

    public static int cnt = 0;
    public static int nCol = maxDist * norient * ninten * norient * ninten * npos;


    public static String _folderDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/pictureGroups/";
    public static String _svmSaveFile = "svm.xml";
    public static String _sogiSaveFile = "sogi.csv";
    public static String _categoryNamesSaveFile = "categoryNames.csv";
    public static String _categorySaveFile = "categories.csv";

    public static Mat testMat;
    public static String _testDir;

    public static double[][] testSogi;

    public static CvSVM svm = null;

    public static int minArea = 40000;
    public static int normalArea = 75000;
    public static int maxArea = 120000;

    public static String _tmpImageDir = _folderDir + "ImageExecutive/";
    public static String _tmpImageFile = "tmp.jpg";

    public static String _cameraTestDir = _folderDir + "CameraTest/";
    public static String _groupsDir = _folderDir + "Groups/";

    public static int trainSize = 100;
    public static ArrayList<String> _trainListDirs = null;
    public static ArrayList<String> _testListDirs = null;
    public static ArrayList<String> _trainTarget = null;
    public static ArrayList<String> _testTarget = null;

    public static int max_number_images_showed_in_static_test = 20;

}
