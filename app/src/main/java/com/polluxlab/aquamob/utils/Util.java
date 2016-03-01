package com.polluxlab.aquamob.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.polluxlab.aquamob.BuildConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Util class for global helping utility methods
 * Created by ARGHA K ROY on 5/7/2015.
 */
public class Util {

    /**
     * Checking for all possible internet providers
     * **/
    public static boolean isConnectedToInternet(Context con){
        ConnectivityManager connectivity = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public static void showNoInternetDialog(final Context con) {
        AlertDialog.Builder build=new AlertDialog.Builder(con);
        build.setTitle("No Internet");
        build.setMessage("Internet is not available. Please check your connection");
        build.setCancelable(true);
        build.setPositiveButton("Settings", new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                con.startActivity(intent);
            }
        });

        build.setNegativeButton("Cancel", new Dialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert=build.create();
        alert.show();
    }

    public static boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static void showToast(Context con,String message){
        Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
    }

    public static void printDebug(String key,String message){
        if(BuildConfig.DEBUG){
            Log.d(AppConst.DEBUG_KEY, key + " - " + message);
        }
    }

    public static boolean isGPSOn(Context context){
        return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled("gps");
    }

    public static ProgressDialog getProgressDialog(Context context,String message){
        ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd'/'MM'/'yy hh':'mm aa", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    public static String getCaptureTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd'/'MM'/'yy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "hh':'mm aa", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void appendLog(String text){
        File logFile = new File("sdcard/lobster_exception.log");
        if (!logFile.exists()){
            try{
                boolean created=logFile.createNewFile();
                if(created){
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                    buf.append(text);
                    buf.newLine();
                    buf.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static boolean deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            return deleteDir(dir);
        } catch (Exception e) {
            Util.printDebug("Dir delete excep",e.getMessage());
            return false;
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile())
            return dir.delete();
        else {
            return false;
        }
    }
}
