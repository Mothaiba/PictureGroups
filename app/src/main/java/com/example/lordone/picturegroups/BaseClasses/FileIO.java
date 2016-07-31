package com.example.lordone.picturegroups.BaseClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import org.json.JSONObject;
import org.opencv.ml.CvSVM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;

/**
 * Created by Lord One on 6/21/2016.
 */
public class FileIO {

//    public static void writeFileInt(int[][] image, int xres, int yres, String file_name) {
//        try {
//            File fileDir = new File(GV._folderDir);
//            if (!fileDir.exists()) {
//                fileDir.mkdirs();
//            }
//
//            FileWriter writer;
//            File file = new File(fileDir, file_name);
//            writer = new FileWriter(file);
//
//            for(int i = 0; i <= xres; i++) {
//                writer.append(Integer.toString(image[i][0]));
//                for(int j = 1; j <= yres; j++) {
//                    writer.append(',' + Integer.toString(image[i][j]));
//                }
//                writer.append('\n');
//            }
//            writer.flush();
//            writer.close();
//
//        }
//        catch (Exception e) {}
//
//    }

//    public static void writeFileFeatures() {
//        try {
//            File fileDir = new File(GV._folderDir);
//            if (!fileDir.exists()) {
//                fileDir.mkdirs();
//            }
//            String file_name = "features.csv";
//
//            FileWriter writer;
//            File file = new File(fileDir, file_name);
//            writer = new FileWriter(file);
//
//            for(int d = 1; d <= GV.maxDist; d++)
//                for(int ori = 1; ori <= GV.norient; ori++)
//                    for(int in = 1; in <= GV.ninten; in++)
//                        for(int ori2 = 1; ori2 <= GV.norient; ori2++)
//                            for(int in2 = 1; in2 <= GV.ninten; in2++)
//                                for(int po = 1; po <= GV.npos; po++)
//                                    writer.append('\n' + String.format("%1$.8f", GV.features[d][ori][in][ori2][in2][po]));
//            writer.flush();
//            writer.close();
//
//        }
//        catch (Exception e) {}

//    }

    public static boolean writeSOGIToFile() {
        try {
            File fileDir = new File(GV._folderDir);
            if(!fileDir.exists()){
                fileDir.mkdirs();
            }

            FileWriter writer;

//            // save SOGI features
//            File file = new File(fileDir, GV._sogiSaveFile);
//            writer = new FileWriter(file);
//
//            for(int i = 0; i < GV.cnt; i++){
//                writer.append(String.format("%1$.12f", GV.sogi.get(i, 0)[0]));
//                for(int j = 1; j < GV.nCol; j++)
//                    writer.append("," + String.format("%1$.12f", GV.sogi.get(i, j)[0]));
//                writer.append('\n');
//            }
//            writer.flush();
//            writer.close();

            // save categories
            File target = new File(fileDir, GV._categorySaveFile);
            writer = new FileWriter(target);

            writer.append(GV._trainTarget.get(0));
            for(int i = 1; i < GV._trainTarget.size(); i++)
                writer.append("," + GV._trainTarget.get(i));

            writer.flush();
            writer.close();

            // save category name list
            File targetFile = new File(fileDir, GV._categoryNamesSaveFile);
            writer = new FileWriter(targetFile);

            writer.append(GV.targetNames[0]);
            for(int i = 1; i < GV.targetNames.length; i++){
                writer.append("," + GV.targetNames[i]);
            }

            writer.flush();
            writer.close();

            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeSVMToFile(CvSVM svm) {
        try {
            File fileDir = new File(GV._folderDir);
            if(!fileDir.exists()){
                fileDir.mkdirs();
            }

            svm.save(GV._folderDir + GV._svmSaveFile);
            writeSOGIToFile();

            System.out.print("Write SVM to File successfully!");
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.print("Cannot write SVM to File!");
            return false;
        }
    }

    public static boolean readSOGIUtils() {
        if(GV.targetNames == null) {
            try {
                String csvFile;
                String csvSplitBy = ",";
                BufferedReader br;
                String line = "";

                //=== load targetNames
                csvFile = GV._folderDir + GV._categoryNamesSaveFile;
                br = new BufferedReader(new FileReader(csvFile));
                while ((line = br.readLine()) != null) {
                    String[] targetNames = line.split(csvSplitBy);
                    GV.targetNames = targetNames.clone();
                }

                //=== pre-process target map
                GV.mapTarget = new JSONObject();
                GV.ivMapTarget = new JSONObject();
                for (int i = 0; i < GV.targetNames.length; i++) {
                    GV.mapTarget.put(GV.targetNames[i], i);
                    GV.ivMapTarget.put(String.valueOf(i), GV.targetNames[i]);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static void readSVMFromFile() {
        if (GV.svm == null && (new File(GV._folderDir + GV._svmSaveFile).exists())) {
            GV.svm = new CvSVM();
            GV.svm.load(GV._folderDir + GV._svmSaveFile);
            readSOGIUtils();
        }
    }

    public static void relocateFile(String _inDir, String _fileName, String _category) {
        File inDir = new File(_inDir);
        if(!inDir.exists())
            inDir.mkdirs();

        File outDir = new File(GV._groupsDir + _category);
        if(!outDir.exists())
            outDir.mkdirs();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(_inDir + File.separator + _fileName, options);

        File outFile = new File(GV._groupsDir + _category + File.separator + _fileName);
        if (outFile.exists())
            outFile.delete();
        try {
            FileOutputStream out = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
