package com.polluxlab.aquamob;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.polluxlab.aquamob.helpers.DBHelper;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.linked.Child;
import com.polluxlab.aquamob.linked.Linked;
import com.polluxlab.aquamob.models.Form;
import com.polluxlab.aquamob.models.FormData;
import com.polluxlab.aquamob.models.Header;
import com.polluxlab.aquamob.models.Metadata;
import com.polluxlab.aquamob.models.SaveData;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.aquamob.views.FormViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FormDataListActivity extends AppCompatActivity {

    private Context mContext;
    private JSONObject formObject;
    private Linked linkedItems;
    private DBHelper mDbHelper;
    private PrefHelper mPrefHelper;
    public static ArrayList<ArrayList<Header>> mainHeaderList;
    private String sectorFieldName, subSectorFieldName, pondFieldName = ""; //This field names are needed for header values

    private final int FORM_SAVE_REQUEST_ID = 121;

    private ArrayList<SaveData> savedDataList;
    private FormDataListAdapter listAdapter;
    private Form mForm;
    private SwipeMenuListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_data_list_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        mDbHelper = new DBHelper(mContext);
        mPrefHelper = new PrefHelper(mContext);
        mForm = (Form) getIntent().getSerializableExtra("form");
        listView = (SwipeMenuListView) findViewById(R.id.formDataSMListView);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(dp2px(90));
                deleteItem.setIcon(R.drawable.ic_delete_white_36dp);
                menu.addMenuItem(deleteItem);
            }
        };

        showAllSavedData();
        // set creator
        listView.setMenuCreator(creator);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject metaObject = null;
                try {
                    metaObject = formObject.getJSONObject("metadata");
                    if (!metaObject.has("swap_level")) {
                        Intent intent = new Intent(mContext, NewFormDataActivity.class);
                        intent.putExtra("saved-data", savedDataList.get(position));
                        intent.putExtra("form", getIntent().getSerializableExtra("form"));
                        startActivityForResult(intent, FORM_SAVE_REQUEST_ID);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("on item click error", e.getMessage());
                }
            }
        });
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        mDbHelper.deleteSavedFormDataById(savedDataList.get(position).getDataId());
                        Util.showToast(mContext, "Form data is deleted");
                        showAllSavedData();
                        break;
                }
                return false;
            }
        });

        try {
            Util.printDebug("Form json", mDbHelper.getFormFields(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName()));
            formObject = new JSONObject(mDbHelper.getFormFields(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName()));
            if (formObject.has("linked")) {
                linkedItems = new Gson().fromJson(formObject.getJSONObject("linked").toString(), Linked.class);
                Util.printDebug("Linked items count", linkedItems.getChildList().size() + " - " + linkedItems.getChildList().get(0).getChildList().size() + " - " + linkedItems.getChildList().get(0).getChildList().get(0).getChildList().size());
                Util.printDebug("Form object", formObject.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Util.printDebug("Form object json exception", e.getMessage());
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (formObject != null && formObject.has("metadata")) {
                    try {
                        JSONObject metaObject = formObject.getJSONObject("metadata");
                        if (metaObject.has("swap_level")) {
                            int swapLevel = metaObject.getInt("swap_level");
                            showRepeatFieldsDialog(swapLevel);
                        } else {
                            Intent intent = new Intent(mContext, NewFormDataActivity.class);
                            intent.putExtra("form", getIntent().getSerializableExtra("form"));
                            startActivityForResult(intent, FORM_SAVE_REQUEST_ID);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Util.printDebug(e.getMessage());
                    }
                }
            }
        });
        getSupportActionBar().setTitle(mForm.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAllSavedData() {
        savedDataList = mDbHelper.getAllSavedForms(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName());
        Util.printDebug("Loading Saved data", savedDataList.toString());
        listAdapter = new FormDataListAdapter(savedDataList);
        listView.setAdapter(listAdapter);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private void showRepeatFieldsDialog(final int swapLevel) throws JSONException {
        ArrayList<JSONObject> repeatFields = getRepeatFieldObjects();
        View view = getLayoutInflater().inflate(R.layout.repeat_fields_dialog_layout, null, false);
        final ArrayList<View> repeatListViews = addRepeatFields(view, repeatFields, swapLevel);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setMessage("Repeat fields")
                .setView(view)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, Spinner> repeatFields = new HashMap<String, Spinner>();
                        repeatFields.put("sector", sectorSpinner);
                        repeatFields.put("subsector", subSectorSpinner);
                        repeatFields.put("pond", pondSpinner);

                        mainHeaderList = generateHeaderTitles(linkedItems, swapLevel, repeatFields);
                        Util.printDebug("Main Header List", mainHeaderList.toString());

                        JSONArray commonFieldsDataArray = new JSONArray();
                        for (View commonFieldView : repeatListViews) {
                            JSONObject dataObject = new JSONObject();
                            FormData formData = (FormData) commonFieldView.getTag();
                            try {
                                dataObject.put("name", formData.getFieldJsonObject().getString("name"));
                                dataObject.put("value", formData.getSavedValue() != null ? formData.getSavedValue() : formData.getDefaultValue());
                                commonFieldsDataArray.put(dataObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Util.printDebug("Common field data exception", e.getMessage());
                            }
                        }
                        Util.printDebug("Common datas on list", commonFieldsDataArray.toString());

                        Intent intent = new Intent(mContext, FormFieldsPagerActivity.class);
                        intent.putExtra("form", getIntent().getSerializableExtra("form"));
                        intent.putExtra("common-fields-data", commonFieldsDataArray.toString());
                        startActivityForResult(intent, FORM_SAVE_REQUEST_ID);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog dialog = dialogBuilder.create();
        dialog.show();
    }

    private ArrayList<ArrayList<Header>> generateHeaderTitles(Linked linkedItems, int swapLevel, HashMap<String, Spinner> repeatFields) {
        ArrayList<ArrayList<Header>> mainHeaderList = new ArrayList<>();
        String output = "";
        ArrayList<Child> childArrayList = linkedItems.getChildList();
        for (int i = 0; i < (swapLevel >= 2 ? 1 : childArrayList.size()); i++) {
            Child childSector = childArrayList.get(i);
            Header headerSector = new Header(childSector.getId(), sectorFieldName, childSector.getLabel());
            ArrayList<Child> childArrayList2 = childArrayList.get(i).getChildList();

            if (swapLevel >= 2) {
                Child sectorSpinner = (Child) ((Spinner) repeatFields.get("sector")).getSelectedItem();
                childArrayList2 = sectorSpinner.getChildList();
            }

            for (int j = 0; j < (swapLevel >= 3 ? 1 : childArrayList2.size()); j++) {
                Child childSubSector = childArrayList2.get(j);
                Header headerSubSector = new Header(childSubSector.getId(), subSectorFieldName, childSubSector.getLabel());
                ArrayList<Child> childArrayList3 = childArrayList2.get(j).getChildList();

                if (swapLevel >= 3) {
                    Child subSectorSpinner = ((Child) ((Spinner) repeatFields.get("subsector")).getSelectedItem());
                    childArrayList3 = subSectorSpinner.getChildList();
                }

                for (int k = 0; k < childArrayList3.size(); k++) {
                    Child childPond = childArrayList3.get(k);
                    ArrayList<Header> subHeaderList = new ArrayList<>();
                    if (swapLevel == 1) subHeaderList.add(headerSector);
                    if (swapLevel <= 2) subHeaderList.add(headerSubSector);

                    Header headerPond = new Header(childPond.getId(), pondFieldName, childPond.getLabel());

                    subHeaderList.add(headerPond);
                    mainHeaderList.add(subHeaderList);
                }
            }
        }
        return mainHeaderList;
    }

    private Spinner sectorSpinner;
    private Spinner subSectorSpinner;
    private Spinner pondSpinner;

    private ArrayList<View> addRepeatFields(View view, ArrayList<JSONObject> repeatFields, int swapLevel) throws JSONException {
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.dialogLinearLayout);
        ArrayList<View> repeatFieldsViewList = new ArrayList<>();
        for (int i = 0; i < repeatFields.size(); i++) {
            JSONObject fieldObject = repeatFields.get(i);
            if (fieldObject.getString("type").equals("sector") || fieldObject.getString("type").equals("subsector") || fieldObject.getString("type").equals("pond") || fieldObject.getString("type").equals("subpond")) {
                if (swapLevel == 1) continue;
                if (swapLevel == 2 && !(fieldObject.getString("type").equals("sector"))) continue;
                if (swapLevel == 3 && !fieldObject.getString("type").equals("sector") && !fieldObject.getString("type").equals("subsector"))
                    continue;

                View fieldView = createCommonDropdownField(fieldObject);
                layout.addView(fieldView);
                repeatFieldsViewList.add(fieldView);
            } else {
                JSONArray otherFieldsJsonArray = new JSONArray();
                for (int c = 0; c < repeatFields.size(); c++) {
                    otherFieldsJsonArray.put(repeatFields.get(c));
                }
                repeatFieldsViewList.addAll(new FormViews(mContext).getFieldList(layout, otherFieldsJsonArray, false, null));
            }
        }
        if (sectorSpinner != null) sectorSpinner.setSelection(0);
        return repeatFieldsViewList;
    }

    private View createCommonDropdownField(JSONObject fieldObject) throws JSONException {
        final View view = getLayoutInflater().inflate(R.layout.field_spinner_input, null);
        final FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        view.setTag(formData);
        final Spinner fieldSpinner = (Spinner) view.findViewById(R.id.fieldSpinner);
        FormViews.showFieldHeaders(view, fieldObject);
        fieldSpinner.setTag(fieldObject.getString("type"));
        switch (fieldObject.getString("type")) {
            case "sector":
                sectorSpinner = fieldSpinner;
                fieldSpinner.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, linkedItems.getChildList()));
                break;
            case "subsector":
                subSectorSpinner = fieldSpinner;
                break;
            case "pond":
                pondSpinner = fieldSpinner;
                break;
        }

        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((Child) fieldSpinner.getSelectedItem()).getChildList() == null) return;

                if (fieldSpinner.getTag().toString().equals("sector") && subSectorSpinner != null) {
                    subSectorSpinner.setAdapter(new ArrayAdapter<Child>(mContext, android.R.layout.simple_spinner_dropdown_item, ((Child) fieldSpinner.getSelectedItem()).getChildList()));
                } else if (fieldSpinner.getTag().toString().equals("subsector") && pondSpinner != null) {
                    pondSpinner.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, ((Child) fieldSpinner.getSelectedItem()).getChildList()));
                }

                formData.setSavedValue(fieldSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fieldSpinner.setSelection(0);

                formData.setDefaultValue(fieldSpinner.getSelectedItem().toString());
            }
        });


        return view;
    }

    private ArrayList<JSONObject> getRepeatFieldObjects() throws JSONException {
        JSONArray fieldsArray = formObject.getJSONArray("fields");
        ArrayList<JSONObject> repeatFields = new ArrayList<>();
        for (int i = 0; i < fieldsArray.length(); i++) {
            JSONObject fieldObject = fieldsArray.getJSONObject(i);

            switch (fieldObject.getString("type")) {
                case "sector":
                    sectorFieldName = fieldObject.getString("name");
                case "subsector":
                    subSectorFieldName = fieldObject.getString("name");
                case "pond":
                    pondFieldName = fieldObject.getString("name");
                case "subpond":
                    repeatFields.add(fieldObject);
                    break;
            }
            if (fieldObject.has("repeat") && fieldObject.getBoolean("repeat")) {
                repeatFields.add(fieldObject);
            }
        }
        return repeatFields;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FORM_SAVE_REQUEST_ID:
                showAllSavedData();
                break;
        }
    }


    class FormDataListAdapter extends BaseAdapter {

        ArrayList<SaveData> savedDataList;

        public FormDataListAdapter(ArrayList<SaveData> savedDataList) {
            this.savedDataList = savedDataList;
        }

        @Override
        public int getCount() {
            return savedDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return savedDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getLayoutInflater().inflate(R.layout.form_data_single_row_layout, null, false);
            TextView mainTitleTv = (TextView) row.findViewById(R.id.formDataRowMainTitleTv);
            TextView subTitleTv = (TextView) row.findViewById(R.id.formDataRowSubTitleTv);
            try {
                if (formObject.has("metadata")) {
                    Util.printDebug("Main metadata", formObject.getJSONObject("metadata").toString());
                    Metadata metadata = new Gson().fromJson(formObject.getJSONObject("metadata").toString(), Metadata.class);

                    //Generating maintitle
                    String formValues[] = metadata.getOfflineLabel().getMain_title().getFields();
                    JSONArray savedDataArray = new JSONArray(savedDataList.get(position).getFormData());
                    Util.printDebug("Saved data array", savedDataArray.toString());
                    for (int i = 0; i < formValues.length; i++) {
                        for (int j = 0; j < savedDataArray.length(); j++) {
                            if (savedDataArray.getJSONObject(j).getString("name").equals(formValues[i])) {
                                formValues[i] = savedDataArray.getJSONObject(j).getString("value");
                                break;
                            }
                        }
                    }
                    //Util.printDebug("Converted data", Arrays.toString(formValues));
                    mainTitleTv.setText(String.format(metadata.getOfflineLabel().getMain_title().getFormat(), formValues));

                    //Generating subtitle
                    formValues = metadata.getOfflineLabel().getSub_title().getFields();
                    //Util.printDebug("Converted data", Arrays.toString(formValues));

                    for (int i = 0; i < formValues.length; i++) {
                        for (int j = 0; j < savedDataArray.length(); j++) {
                            if (savedDataArray.getJSONObject(j).getString("name").equals(formValues[i])) {
                                formValues[i] = savedDataArray.getJSONObject(j).getString("value");
                                break;
                            }
                        }
                    }
                    //Util.printDebug("Converted data", Arrays.toString(formValues));
                    subTitleTv.setText(String.format(metadata.getOfflineLabel().getSub_title().getFormat(), formValues));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Util.printDebug("Offline label error", e.getMessage());
            }
            return row;
        }
    }
}
