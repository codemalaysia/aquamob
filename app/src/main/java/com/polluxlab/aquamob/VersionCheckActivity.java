package com.polluxlab.aquamob;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.polluxlab.aquamob.helpers.DBHelper;
import com.polluxlab.aquamob.helpers.HTTPHelper;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.utils.AppConst;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.aquamob.utils.Version;
import com.polluxlab.utils.ReleaseDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import cz.msebera.android.httpclient.Header;

/**
 * This activity check for latest version and download the file
 * Created by ARGHA K ROY on 6/9/2015.
 */
public class VersionCheckActivity extends Activity implements OnClickListener {

    AsyncHttpClient httpClient;
    ProgressDialog progressDialog;
    ProgressBar horizonProgressBar;
    TextView progressText,infoTextView;
    LinearLayout checkUpdateBtnLayout,downloadProgressLayout;
    Button checkUpdateBtn,skipUpdateBtn;

    boolean enterApp=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version_check_layout);
        horizonProgressBar= (ProgressBar) findViewById(R.id.downloadProgressBar);
        progressText= (TextView) findViewById(R.id.downloadProgressText);
        infoTextView= (TextView) findViewById(R.id.versionCheckInfoTv);
        checkUpdateBtnLayout= (LinearLayout) findViewById(R.id.versionCheckCheckUpdateBtnsLayout);
        downloadProgressLayout= (LinearLayout) findViewById(R.id.versionCheckDownloadProgressLayout);
        checkUpdateBtn= (Button) findViewById(R.id.checkUpdateBtn);
        skipUpdateBtn= (Button) findViewById(R.id.skipUpdateBtn);

        skipUpdateBtn.setOnClickListener(this);
        checkUpdateBtn.setOnClickListener(this);

        progressDialog= Util.getProgressDialog(this, "Loading. Please wait...");
        httpClient= HTTPHelper.getParseHTTPClient();

        if(getIntent().getBooleanExtra("update",false)){
            updateUIForManualUpdate();
            checkWhichAppToUpdate();
            return;
        }

        if(ReleaseDetails.type== ReleaseDetails.Type.MASTER){
            infoTextView.setText("Checking For Updates.");
            checkUpdateBtnLayout.setVisibility(View.GONE);
            if(Util.isConnectedToInternet(this)){
                enterIntoApp();
            }else enterIntoApp();
        }else if(ReleaseDetails.type== ReleaseDetails.Type.DEVELOPER) {
            checkUpdateBtnLayout.setVisibility(View.VISIBLE);
            checkUpdateBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkForUpdate(false);
                }
            });
        }

    }

    private void checkWhichAppToUpdate() {
        if(Util.isConnectedToInternet(this)){
            if(getIntent().getStringExtra("type").equalsIgnoreCase(ReleaseDetails.Type.MASTER.toString())) checkForUpdate(true);
            else if(getIntent().getStringExtra("type").equalsIgnoreCase(ReleaseDetails.Type.DEVELOPER.toString())) checkForUpdate(false);
        }else{
            Util.showNoInternetDialog(this);
        }
    }

    private void updateUIForManualUpdate() {
        if(Util.isConnectedToInternet(this))infoTextView.setText("You are online");
        else infoTextView.setText("You are offline");
    }

    private void checkForUpdate(boolean master) {
        JSONObject object=new JSONObject();
        RequestParams params=new RequestParams();
        try {
            if(master)
                object.put("type","master");
            else
                object.put("type","dev");

            params.add("where",object.toString());
            params.add("order","-buildNumber");
        } catch (JSONException e) {
            e.printStackTrace();
            Util.printDebug("Json Exception",e.getMessage());
        }
        httpClient.get("https://api.parse.com/1/classes/AquamobApps", params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Util.printDebug("Parse Response", response.toString());
                try {
                    if (response.getJSONArray("results").length() > 0) {
                        JSONObject latestVersionObject = response.getJSONArray("results").getJSONObject(0);
                        String buildNumber = latestVersionObject.getString("buildNumber");
                        Util.printDebug("Latest Version", buildNumber);

                        String major = latestVersionObject.getString("major");
                        String minor = latestVersionObject.getString("minor");

                        String fileUrl = latestVersionObject.getJSONObject("apk").getString("url");
                        Util.printDebug("Latest Version File url", fileUrl);

                        Util.printDebug("Version Info", major + "." + minor + "." + buildNumber + "- Current " + Version.MAJOR + "." + Version.MINOR + "." + Version.BUILD_VERSION);

                        String versionCompare = "";
                        if (Version.BUILD_VERSION.equals("trunk-SNAPSHOT")) {
                            versionCompare = "trunk-snapshot";
                        } else if (Integer.parseInt(major) > Integer.parseInt(Version.MAJOR)) {
                            versionCompare = "major greater";
                        } else if (Integer.parseInt(major) == Integer.parseInt(Version.MAJOR)) {
                            versionCompare = "major equals";
                            if (Integer.parseInt(minor) > Integer.parseInt(Version.MINOR)) {
                                versionCompare = "minor greater";
                            } else if (Integer.parseInt(minor) == Integer.parseInt(Version.MINOR)) {
                                if (buildNumber.compareTo(Version.BUILD_VERSION) > 0)
                                    versionCompare = "build greater";
                                else {
                                    versionCompare = "build equals";
                                    enterApp = true;
                                }
                            } else enterApp = true;
                        } else enterApp = true;

                        Util.printDebug("Compare", versionCompare);
                        if (enterApp)
                            enterIntoApp();
                        else{
                            progressDialog.dismiss();
                            showAppUpdateConfirmDialog(fileUrl);
                        }
                    } else enterIntoApp();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("json error", e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.printDebug("Parse Error Response", responseString);
                progressDialog.dismiss();
            }
        });
    }

    private void showAppUpdateConfirmDialog(final String fileUrl){
        Util.printDebug("Inside confirm",fileUrl);
        DBHelper dbHelper=new DBHelper(this);
        if(dbHelper.hasSavedFormsData()){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("New Update Found");
            builder.setMessage("You have saved data that is not yet sync, \n" +
                    "updating will cause you to lose this saved data. \n" +
                    "Press cancel to go to sync data first or \n" +
                    "Press ok to ignore and continue to update losing sync data");
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startActivity(new Intent(VersionCheckActivity.this,LoginActivity.class));
                    finish();
                }
            }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    downloadParseApp(fileUrl);
                }
            });
            Dialog dialog=builder.create();
            dialog.show();
        }else{
            downloadParseApp(fileUrl);
        }
    }

    private void deleteAppCache() {
        this.deleteDatabase(AppConst.DB_NAME);
        new PrefHelper(this).clearAllPref();
        if (!Util.deleteCache(this)) {
            Util.showToast(this, "Cache files could not delete");
        }
    }

    private void enterIntoApp() {
        Util.printDebug("Entering app", "");
        startActivity(new Intent(VersionCheckActivity.this,LoginActivity.class));
        finish();
    }


    private void downloadParseApp(String fileUrl){
        infoTextView.setText("Latest Version Found.");
        downloadProgressLayout.setVisibility(View.VISIBLE);
        checkUpdateBtnLayout.setVisibility(View.GONE);

        httpClient.get(fileUrl, new FileAsyncHttpResponseHandler(VersionCheckActivity.this) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                Util.printDebug("Download error",statusCode+"");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                Util.printDebug("Downloaded file",file.getAbsolutePath()+" "+file.getName());
                if(file!=null){
                    copyToFileManager(file);
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                int progress= (int) (((bytesWritten*1.0)/totalSize)*100);
                horizonProgressBar.setProgress(progress);
                progressText.setText("Download Progress: "+progress+"%");
                if(progress==100)
                    progressText.setText("Download Complete");
                //Util.printDebug("Progress ",((bytesWritten*1.0)/totalSize)*100+"%");
            }

        });
    }

    private void copyToFileManager(File file) {
        try {
            final File output = new File(Environment.getExternalStorageDirectory(), "lobster-v2.apk");
            InputStream is = new FileInputStream(file);
            OutputStream os = new FileOutputStream(output);
            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
            is.close();
            os.close();

            deleteAppCache();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(output), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //Deleting the downloaded file in internal storage, not the local file
            file.delete();

            finish();
        }catch (Exception e) {
            e.printStackTrace();
            infoTextView.setText(e.getMessage());
            Util.showToast(this,"Copy file error "+e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.skipUpdateBtn:
                enterIntoApp();
                break;
            case R.id.checkUpdateBtn:
                checkWhichAppToUpdate();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        httpClient.cancelAllRequests(true);
    }
}
