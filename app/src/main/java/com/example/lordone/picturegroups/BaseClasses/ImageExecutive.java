package com.example.lordone.picturegroups.BaseClasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Lord One on 6/26/2016.
 */
public class ImageExecutive {

    public static void resizeImage(String mCurrentPhotoPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, options);

        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();

        double ratio = Math.sqrt((double) GV.normalArea / (oldHeight * oldWidth));

        int newWidth = (int) (oldWidth * ratio);
        int newHeight = (int) (oldHeight * ratio);
        Bitmap resized = bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

        File fileDir = new File(GV._tmpImageDir);
        if(!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File file = new File(GV._tmpImageDir + GV._tmpImageFile);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            resized.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void rotateImage(String _imageDir) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(_imageDir, bounds);

            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm = BitmapFactory.decodeFile(_imageDir, opts);
            ExifInterface exif = new ExifInterface(_imageDir);
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Bitmap rotatedBitmap;
            if(rotationAngle != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
            }
            else
                rotatedBitmap = bm;

            File file = new File(_imageDir);
            file.delete();
            FileOutputStream out = new FileOutputStream(file);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load1Image(String _file) {

        rotateImage(_file);
        Mat rawImg = Highgui.imread(_file, 0);
        GV.xres = rawImg.rows() - 1;
        GV.yres = rawImg.cols() - 1;

        if(GV.xres * GV.yres < GV.minArea || GV.xres * GV.yres > GV.maxArea) {

            ImageExecutive.resizeImage(_file);
            rawImg = Highgui.imread(GV._tmpImageDir + GV._tmpImageFile, 0);
            GV.xres = rawImg.rows() - 1;
            GV.yres = rawImg.cols() - 1;
        }

        GV.image = new int[GV.xres + 1][GV.yres + 1];
        for (int i = 0; i <= GV.xres; i++) for (int j = 0; j <= GV.yres; j++) {
            GV.image[i][j] = (int) rawImg.get(i, j)[0];
        }
//        FileIO.writeFileInt(GV.image, GV.xres, GV.yres, "raw_image.csv");
        System.out.print("OK");
    }

    public static void computeGradient() {

        GV.inten = new int[GV.xres + 1][GV.yres + 1];
        GV.orient = new int[GV.xres + 1][GV.yres + 1];

        double vert, hori;
        double tmpinten;
        for (int i = 0; i <= GV.xres; i++) for (int j = 0; j <= GV.yres; j++) {
            if (i > 0 && i < GV.xres)
                vert = GV.image[i - 1][j] - GV.image[i + 1][j];
            else if (i == 0)
                vert = GV.image[i][j] - GV.image[i + 1][j];
            else
                vert = GV.image[i - 1][j] - GV.image[i][j];
            if (j > 0 && j < GV.yres)
                hori = GV.image[i][j + 1] - GV.image[i][j - 1];
            else if (j == 0)
                hori = GV.image[i][j + 1] - GV.image[i][j];
            else
                hori = GV.image[i][j] - GV.image[i][j - 1];

            if (hori == 0)
                GV.orient[i][j] = 1;
            else
                GV.orient[i][j] = (int) ((Math.atan(vert / hori) / GV.pi * 180 + 90) / GV.iorient) + 1;
            tmpinten = Math.sqrt(vert * vert + hori * hori);
            if (tmpinten <= GV.eps)
                GV.inten[i][j] = 0;
            else if (tmpinten <= GV.threshold[1])
                GV.inten[i][j] = 1;
            else if (tmpinten <= GV.threshold[2])
                GV.inten[i][j] = 2;
            else
                GV.inten[i][j] = 3;
        }
//        FileIO.writeFileInt(GV.orient, GV.xres, GV.yres, "orient.csv");
//        FileIO.writeFileInt(GV.inten, GV.xres, GV.yres, "intent.csv");
        System.out.print("OK");
    }

    public static void computeFeatures() {

        try{
            GV.features = new double [GV.maxDist + 1][GV.norient + 1][GV.ninten + 1][GV.norient + 1][GV.ninten + 1][GV.npos + 1];

            int left, right, up, down;
            int row, col;
            int tmporient, tmpinten;

            for (int i = 0; i <= GV.xres; i++)
                for (int j = 0; j <= GV.yres; j++) {
                    tmporient = GV.orient[i][j];
                    tmpinten = GV.inten[i][j];
                    if (tmpinten == 0) continue;
                    for (int d = 1; d <= GV.maxDist; d++) {
                        left = Math.max(0, j - d + 1);
                        right = Math.min(GV.yres, j + d - 1);
                        if (i - d >= 0) {
                            row = i - d;
                            for (int u = left; u <= right; u++)
                                GV.features[d][tmporient][tmpinten][GV.orient[row][u]][GV.inten[row][u]][1]++;
                        }
                        if (i + d <= GV.xres) {
                            row = i + d;
                            for (int u = left; u <= right; u++)
                                GV.features[d][tmporient][tmpinten][GV.orient[row][u]][GV.inten[row][u]][2]++;
                        }

                        up = Math.max(0, i - d);
                        down = Math.min(GV.xres, i + d);
                        if (j - d >= 0) {
                            col = j - d;
                            for (int v = up; v <= down; v++)
                                GV.features[d][tmporient][tmpinten][GV.orient[v][col]][GV.inten[v][col]][3]++;
                        }
                        if (j + d <= GV.yres) {
                            col = j + d;
                            for (int v = up; v <= down; v++)
                                GV.features[d][tmporient][tmpinten][GV.orient[v][col]][GV.inten[v][col]][4]++;
                        }
                    }
                }
//            FileIO.writeFileFeatures();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        System.out.print("OK");
    }

    public static void normalizeFeatures_train() {
        double sumAround;
        for (int d = 1; d <= GV.maxDist; d++) for (int o = 1; o <= GV.norient; o++) for (int i = 1; i <= GV.ninten; i++) for(int pos = 1; pos <= GV.npos; pos++) {
            sumAround = 0;
            for (int u = 1; u <= GV.norient; u++) for (int v = 1; v <= GV.ninten; v++)
                sumAround += GV.features[d][o][i][u][v][pos];
            if (sumAround > GV.eps)
                for (int u = 1; u <= GV.norient; u++) for (int v = 1; v <= GV.ninten; v++)
                    GV.features[d][o][i][u][v][pos] /= sumAround;
        }
//        FileIO.writeFileFeatures();
        int dem = 0;
        for (int d = 1; d <= GV.maxDist; d++) for (int o1 = 1; o1 <= GV.norient; o1++)
            for (int i1 = 1; i1 <= GV.ninten; i1++) for (int o2 = 1; o2 <= GV.norient; o2++)
                for (int i2 = 1; i2 <= GV.ninten; i2++) for(int pos = 1; pos <= GV.npos; pos++){
                    GV.sogi.put(GV.cnt, dem++, (float) GV.features[d][o1][i1][o2][i2][pos]);
                }
    }

    public static void normalizeFeatures_test() {
        double sumAround;
        for (int d = 1; d <= GV.maxDist; d++) for (int o = 1; o <= GV.norient; o++) for (int i = 1; i <= GV.ninten; i++) for(int pos = 1; pos <= GV.npos; pos++) {
            sumAround = 0;
            for (int u = 1; u <= GV.norient; u++) for (int v = 1; v <= GV.ninten; v++)
                sumAround += GV.features[d][o][i][u][v][pos];
            if (sumAround > GV.eps)
                for (int u = 1; u <= GV.norient; u++) for (int v = 1; v <= GV.ninten; v++)
                    GV.features[d][o][i][u][v][pos] /= sumAround;
        }

        int dem = 0;
        for (int d = 1; d <= GV.maxDist; d++) for (int o1 = 1; o1 <= GV.norient; o1++)
            for (int i1 = 1; i1 <= GV.ninten; i1++) for (int o2 = 1; o2 <= GV.norient; o2++)
                for (int i2 = 1; i2 <= GV.ninten; i2++) for(int pos = 1; pos <= GV.npos; pos++){
                    GV.testMat.put(0, dem++, (float) GV.features[d][o1][i1][o2][i2][pos]);
                }
    }

    public static Bitmap getBitmap(String _image) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap photo = BitmapFactory.decodeFile(_image, options);
        return photo;
    }

    public static void releaseMemory() {
        GV.sogi = null;
        GV.features = null;
        System.gc();
    }

}
