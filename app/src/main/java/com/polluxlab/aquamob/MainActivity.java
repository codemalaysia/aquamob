package com.polluxlab.aquamob;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.polluxlab.aquamob.callbacks.FormsUpdateCallback;
import com.polluxlab.aquamob.callbacks.LoginResponse;
import com.polluxlab.aquamob.helpers.DBHelper;
import com.polluxlab.aquamob.helpers.HTTPHelper;
import com.polluxlab.aquamob.helpers.LoginRequest;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.models.Form;
import com.polluxlab.aquamob.models.SaveData;
import com.polluxlab.aquamob.models.User;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.aquamob.utils.Version;
import com.polluxlab.utils.ReleaseDetails;
import com.polluxlab.utils.ServerLinks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context mContext;
    private DBHelper dbHelper;
    private PrefHelper prefHelper;
    private ArrayList<Form> formList;
    private User mCurrentUser;
    private AsyncHttpClient httpClient;
    private GridView formsGridList;
    private Spinner userChangeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initialize();

        mCurrentUser = prefHelper.getCurrentUser();
        formList = dbHelper.getAllForms(mCurrentUser.getUserName());

        if (formList.size() == 0 && mCurrentUser.getFormsUrl() != null) {
            getAllFormsFromServer(mCurrentUser.getFormsUrl(), null);
        } else if (formList.size() == 0 && mCurrentUser.getFormsUrl() == null)
            showUserNameInputDialog();
        else showAllForms(formList);

        populateUserChangeDropDown();
    }

    private void initialize() {
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        formsGridList = (GridView) findViewById(R.id.mainFormsGL);
        userChangeSpinner = (Spinner) navigationView.getHeaderView(0).findViewById(R.id.mainUserChangeSpinner);
        dbHelper = new DBHelper(mContext);
        prefHelper = new PrefHelper(mContext);
        httpClient = HTTPHelper.getHTTPClient(prefHelper.getCurrentUser().getUserName(),"",prefHelper.getPlxClientId());

        ImageButton userAddBtn = (ImageButton) navigationView.getHeaderView(0).findViewById(R.id.mainUserAddIB);
        userAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserNameInputDialog();
            }
        });

        userChangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = (User) userChangeSpinner.getSelectedItem();
                prefHelper.saveCurrentUser(selectedUser);
                mCurrentUser = selectedUser;
                formList = dbHelper.getAllForms(mCurrentUser.getUserName());
                showAllForms(formList);
                drawer.closeDrawers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        formsGridList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(mContext, FormDataListActivity.class);
                intent.putExtra("form", formList.get(position));
                startActivity(intent);
            }
        });

        Util.printDebug("plx client id",prefHelper.getPlxClientId());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showExitConfirmDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_senddata:
                startSendingDataToServer();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSendingDataToServer() {
        if(Util.isConnectedToInternet(this)){
            ArrayList formIdList = new ArrayList(prefHelper.getFormIds());
            if(formIdList.size()>0){
                String formId=formIdList.get(0).toString();
                formIdList.remove(0);
                Util.printDebug("Form Id",formId);
                ArrayList<SaveData> savedForm=dbHelper.getAllSavedForms(formId,mCurrentUser.getUserName());
                if(savedForm.size()>0) {
                    Util.printDebug("Saved form size",savedForm.size()+"");
                    try {
                        JSONArray savedFormsArray = new JSONArray();
                        for (int i = 0; i < savedForm.size(); i++) {
                            savedFormsArray.put(new JSONArray(savedForm.get(i).getFormData()));
                        }
                        JSONObject serverJsonObject = new JSONObject();
                        serverJsonObject.put("formdata", savedFormsArray);
                        Util.printDebug("saved form array", serverJsonObject.toString());
                        sendDataToServer(formId, serverJsonObject,formIdList);
                    }catch (Exception e){
                        Util.printDebug("Error in startSendingData",e.getMessage());
                    }
                }else {
                    startSendingDataToServer();
                }
            }
        }else Util.showNoInternetDialog(this);
    }

    private void sendDataToServer(final String formId, JSONObject serverJsonObject, final ArrayList formIdList) throws UnsupportedEncodingException {
        httpClient=HTTPHelper.getHTTPClient(mCurrentUser.getUserName(),"",prefHelper.getPlxClientId());
        final StringEntity entity = new StringEntity(serverJsonObject.toString());
        String sendDataUrl=String.format(ServerLinks.SEND_DATA_URL,formId);
        Util.printDebug("send data url",sendDataUrl);
        Util.printDebug("send data", serverJsonObject.toString());
        httpClient.post(mContext, sendDataUrl, entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Util.printDebug("Server sent data response success", statusCode + "");
                if (formIdList.size() == 0) {
                    dbHelper.deleteAllSavedFormData(mCurrentUser.getUserName());
                } else {
                    dbHelper.deleteSavedFormDataByFormId(formId, mCurrentUser.getUserName());
                    startSendingDataToServer();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Util.printDebug("Server sent data response fail", statusCode + "");
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_about:
                showVersionInfoDialog();
                break;
            case R.id.nav_update:
                getAllFormsFromServer(mCurrentUser.getFormsUrl(), new FormsUpdateCallback() {
                    @Override
                    public void updated() {
                        Util.showToast(mContext, "Forms are updated successfully");
                        checkForUpdate();
                    }
                });
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkForUpdate() {
        Intent appUpdateIntent = new Intent(this, VersionCheckActivity.class);
        appUpdateIntent.putExtra("update", true);
        appUpdateIntent.putExtra("type", ReleaseDetails.type.toString());
        startActivity(appUpdateIntent);
        finish();
    }

    private void showVersionInfoDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Current app version");
        dialogBuilder.setMessage(Version.MAJOR + "." + Version.MINOR + "." + Version.BUILD_VERSION);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        Dialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private void showAllForms(ArrayList<Form> formArrayList) {
        if (formArrayList != null)
            formsGridList.setAdapter(new ArrayAdapter<>(mContext, R.layout.single_form_item_layout, formArrayList));
    }

    private void populateUserChangeDropDown() {
        final ArrayList<User> userList = prefHelper.getUsersInfo();
        userChangeSpinner.setAdapter(new ArrayAdapter<>(mContext, R.layout.user_spinner_item, userList));
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserName().equals(mCurrentUser.getUserName())) {
                userChangeSpinner.setSelection(i);
                break;
            }
        }
    }

    private void showUserNameInputDialog() {
        View view = getLayoutInflater().inflate(R.layout.user_change_dialog_layout, null);
        final EditText usernameEt = (EditText) view.findViewById(R.id.userEt);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                mContext);
        alertDialogBuilder.setTitle(getString(R.string.enter_username));
        alertDialogBuilder.setView(view);
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!Util.isConnectedToInternet(mContext)) {
                            Util.showNoInternetDialog(mContext);
                            return;
                        }
                        final String username = usernameEt.getText().toString().trim();
                        if (TextUtils.isEmpty(username.trim())) {
                            Util.showToast(mContext, "Please enter your username");
                            return;
                        }
                        validateLoginAndPullForms(username, new FormsUpdateCallback() {
                            @Override
                            public void updated() {
                                populateUserChangeDropDown();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void validateLoginAndPullForms(final String username, final FormsUpdateCallback formsUpdateCallback) {
        final ProgressDialog pDialog = Util.getProgressDialog(mContext, "Checking login. Please wait...");
        pDialog.show();
        LoginRequest mLoginRequest = new LoginRequest(mContext,new LoginResponse() {
            @Override
            public void successResponse(int statusCode, User user) {
                pDialog.dismiss();
                if (user == null || user.getFormsUrl() == null) {
                    showUserNameInputDialog();
                    Util.printDebug("Retrieving form url error", statusCode + "");
                    return;
                }
                user.setUserName(username);
                prefHelper.saveUserInfo(user);
                prefHelper.saveCurrentUser(user);
                mCurrentUser = user;
                Util.printDebug("Forms url", user.getFormsUrl());
                getAllFormsFromServer(user.getFormsUrl(), formsUpdateCallback);
            }

            @Override
            public void errorResponse(int statusCode, String responseString) {
                pDialog.dismiss();
                showUserNameInputDialog();
                Util.printDebug("Login", "failed " + statusCode + " " + responseString);
            }
        });
        mLoginRequest.login(ServerLinks.ENDPOINT, username);
    }


    private void getAllFormsFromServer(final String FORM_URL, final FormsUpdateCallback formsUpdateCallback) {
        Util.printDebug("Form url", FORM_URL);
        Util.printDebug("installation id", prefHelper.getPlxClientId());
        final ProgressDialog progressDialog = Util.getProgressDialog(mContext, "Getting forms from server...");
        httpClient = HTTPHelper.getHTTPClient(mCurrentUser.getUserName(), "",prefHelper.getPlxClientId());
        httpClient.get(FORM_URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Util.printDebug("Form download Response-", "status code : " + statusCode);
                if (statusCode != 200) return;
                Util.printDebug("Response-", response.toString());

                try {
                    dbHelper.deleteAllDownloadedForms(mCurrentUser.getUserName());

                   for (int i = 0; i < response.length(); i++) {
                        JSONObject formObject = response.getJSONObject(i);
                        Util.printDebug("Response-", statusCode + " " + response.getJSONObject(i).getJSONArray("fields").toString());
                        dbHelper.saveDownloadedForm(formObject.getString("id"), mCurrentUser.getUserName(),
                                formObject.getString("title"),
                                formObject.toString());
                       prefHelper.saveFormId(formObject.getString("id"));
                   }
                    formList = dbHelper.getAllForms(mCurrentUser.getUserName());

                    //TODO Remove this to save original json data
/*                    JSONArray formArray = new JSONArray(getResources().getString(R.string.forms_json));
                    for (int i = 0; i < formArray.length(); i++) {
                        JSONObject formObject = formArray.getJSONObject(i);
                        Util.printDebug("Response-", statusCode + " " + response.getJSONObject(i).getJSONArray("fields").toString());
                        dbHelper.saveDownloadedForm(formObject.getString("id"), mCurrentUser.getUserName(),
                                formObject.getString("title"),
                                formObject.toString());
                    }*/

                    formList = dbHelper.getAllForms(mCurrentUser.getUserName());
                    Util.printDebug("Form size", formList.size() + "");
                    showAllForms(formList);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("JSON Parse error", e.getMessage());
                }
                if (formsUpdateCallback != null) formsUpdateCallback.updated();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Util.printDebug("Error Response-", statusCode + " " + responseString);
                Util.showToast(mContext, "Form download failed ");
            }

            @Override
            public void onFinish() {
                super.onFinish();
                progressDialog.dismiss();
            }
        });
    }


    private void showExitConfirmDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.cancel();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure want to exit?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

}
