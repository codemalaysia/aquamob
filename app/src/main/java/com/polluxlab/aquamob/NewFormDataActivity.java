package com.polluxlab.aquamob;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.polluxlab.aquamob.callbacks.FieldChangeCallback;
import com.polluxlab.aquamob.helpers.DBHelper;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.models.Form;
import com.polluxlab.aquamob.models.FormData;
import com.polluxlab.aquamob.models.SaveData;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.aquamob.views.FormViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class NewFormDataActivity extends AppCompatActivity {

    private Form mForm;
    private Context mContext;

    private DBHelper mDbHelper;
    private PrefHelper mPrefHelper;
    private LinearLayout mMainLayout;
    private ArrayList<View> formFieldList;
    private SaveData saveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_form_data_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;

        LinearLayout switchLayout = (LinearLayout) findViewById(R.id.activeSwitchLayout);
        switchLayout.setVisibility(View.GONE);

        saveData = (SaveData) getIntent().getSerializableExtra("saved-data");
        mForm = (Form) getIntent().getSerializableExtra("form");
        getSupportActionBar().setTitle(mForm.getName());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDbHelper = new DBHelper(mContext);
        mPrefHelper = new PrefHelper(mContext);
        mMainLayout = (LinearLayout) findViewById(R.id.mainFormLayout);

        try {
            JSONObject serverFormJsonObject = new JSONObject(mDbHelper.getFormFields(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName()));
            JSONArray serverFormFieldsArray = serverFormJsonObject.getJSONArray("fields");
            formFieldList = new FormViews(mContext).getFieldList(mMainLayout, serverFormFieldsArray, false, new FieldChangeCallback() {
                @Override
                public void onFieldChange(View view) {

                }
            });
            if (saveData != null) {
                loadEditableData();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Util.printDebug("Forms json exception", e.getMessage());
        }
    }

    private void loadEditableData() throws JSONException {
        JSONArray savedDataArray = new JSONArray(saveData.getFormData());
        FormViews.loadEditableData(savedDataArray, formFieldList);
        Util.printDebug("Edit data", savedDataArray.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single_form, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_save:
                JSONArray userFormArray = new JSONArray();
                for (int i = 0; i < formFieldList.size(); i++) {
                    try {
                        FormData data = (FormData) formFieldList.get(i).getTag();

                        String value = (data.getSavedValue() == null) ? data.getDefaultValue() : data.getSavedValue();
                        if (value != null && value.equalsIgnoreCase("now()")) {
                            value = Util.getCaptureTime();
                        }

                        JSONObject userFormObject = new JSONObject();
                        userFormObject.put("name", data.getFieldJsonObject().getString("name"));
                        userFormObject.put("value", value);
                        userFormArray.put(userFormObject);
                    } catch (JSONException e) {
                        Util.printDebug("preview json error", e.getMessage());
                        e.printStackTrace();
                    }
                }

                if (saveData != null) {
                    Util.printDebug("Updating data", saveData.getDataId() + " - " + userFormArray.toString());
                    mDbHelper.updateUserForm(saveData.getDataId(), userFormArray.toString());
                    Util.showToast(this, "Data is updated");
                } else {
                    mDbHelper.saveUserForm(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName(), userFormArray.toString(),null);
                    Util.printDebug("Json Array Saved Form", userFormArray.toString());
                    Util.showToast(this, "Data is saved");
                }
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
