package com.polluxlab.aquamob;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.polluxlab.aquamob.callbacks.FieldChangeCallback;
import com.polluxlab.aquamob.helpers.DBHelper;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.models.Form;
import com.polluxlab.aquamob.models.FormData;
import com.polluxlab.aquamob.models.Header;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.aquamob.views.FormViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Author: ARGHA K ROY
 * Date: 3/12/2016.
 */
public class FormFieldsFragment extends Fragment {

    private static final String ARG_POSITION = "arg_position";
    private static final String ARG_FORM = "arg_form";

    private int mPosition = -1;
    private Form mForm;

    private DBHelper mDbHelper;
    private PrefHelper mPrefHelper;
    private LinearLayout mMainLayout;

    private FieldChangeCallback fieldChangeCallback;
    private ArrayList<View> formFieldList;
    private SwitchCompat activeFormSwitch;

    public static FormFieldsFragment newInstance(Form form, int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        args.putSerializable(ARG_FORM, form);
        FormFieldsFragment fragment = new FormFieldsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPosition = getArguments().getInt(ARG_POSITION);
        mForm = (Form) getArguments().getSerializable(ARG_FORM);
        mDbHelper = new DBHelper(getActivity());
        mPrefHelper = new PrefHelper(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.form_fields_fragment_layout, container, false);
        activeFormSwitch = (SwitchCompat) root.findViewById(R.id.formActiveSwitch);
        mMainLayout = (LinearLayout) root.findViewById(R.id.mainFormLayout);

        try {
            JSONObject serverFormJsonObject = new JSONObject(mDbHelper.getFormFields(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName()));
            JSONArray serverFormFieldsArray = serverFormJsonObject.getJSONArray("fields");

            Util.printDebug("Form Json", serverFormJsonObject.toString());
            fieldChangeCallback = new FieldChangeCallback() {
                @Override
                public void onFieldChange(View view) {
                    activeFormSwitch.setChecked(true);
                }
            };
            formFieldList = new FormViews(getActivity()).getFieldList(mMainLayout, serverFormFieldsArray, true, fieldChangeCallback);
        } catch (JSONException e) {
            e.printStackTrace();
            Util.printDebug("Forms json exception", e.getMessage());
        }
        return root;
    }

    public void saveFragment(ArrayList<Header> headers, JSONArray commonFieldsArray) {
        if(!activeFormSwitch.isChecked()) return;
        Util.printDebug("Save fragment ", mPosition + "");
        Util.printDebug("Headers ", headers.toString());
        try {
            JSONArray finalSavedArray = new JSONArray(commonFieldsArray.toString());
            for (int i = 0; i < headers.size(); i++) {
                JSONObject userFormObject = new JSONObject();
                userFormObject.put("name", headers.get(i).getName());
                userFormObject.put("value", headers.get(i).getValue());
                Util.printDebug("Header", headers.get(i).getName(), headers.get(i).getValue());
                finalSavedArray.put(userFormObject);
            }
            String swapFormId=new JSONArray(finalSavedArray.toString()).toString();
            Util.printDebug("Json Array Saved Form", finalSavedArray.toString());
            for (int i = 0; i < formFieldList.size(); i++) {
                FormData data = (FormData) formFieldList.get(i).getTag();

                String value = (data.getSavedValue() == null) ? data.getDefaultValue() : data.getSavedValue();
                if (value != null && value.equalsIgnoreCase("now()")) {
                    value = Util.getCaptureTime();
                }

                JSONObject userFormObject = new JSONObject();
                userFormObject.put("name", data.getFieldJsonObject().getString("name"));
                userFormObject.put("value", value);
                finalSavedArray.put(userFormObject);
            }

/*        if (saveData != null) {
            Util.printDebug("Updating data", saveData.getDataId() + " - " + userFormArray.toString());
            mDbHelper.updateUserForm(saveData.getDataId(), userFormArray.toString());
            Util.showToast(this, "Data is updated");
        } else {*/
            mDbHelper.saveUserForm(mForm.getFormId(), mPrefHelper.getCurrentUser().getUserName(), finalSavedArray.toString(),swapFormId);
            Util.printDebug("Json Array Saved Form", finalSavedArray.toString());
            //Util.showToast(getActivity(), "Data is saved");
            //}

        } catch (JSONException e) {
            Util.printDebug("preview json error", e.getMessage());
            e.printStackTrace();
        }
    }

}
