package com.polluxlab.aquamob.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.polluxlab.aquamob.R;
import com.polluxlab.aquamob.callbacks.FieldChangeCallback;
import com.polluxlab.aquamob.models.DropdownOptions;
import com.polluxlab.aquamob.models.FormData;
import com.polluxlab.aquamob.utils.Util;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.polluxlab.aquamob.models.FormTypes.BOOLEAN;
import static com.polluxlab.aquamob.models.FormTypes.CHOICE;
import static com.polluxlab.aquamob.models.FormTypes.DATE;
import static com.polluxlab.aquamob.models.FormTypes.DATETIME;
import static com.polluxlab.aquamob.models.FormTypes.DECIMAL;
import static com.polluxlab.aquamob.models.FormTypes.DROPDOWN;
import static com.polluxlab.aquamob.models.FormTypes.MULTICHOICE;
import static com.polluxlab.aquamob.models.FormTypes.NUMBER;
import static com.polluxlab.aquamob.models.FormTypes.SLIDER;
import static com.polluxlab.aquamob.models.FormTypes.TEXT;
import static com.polluxlab.aquamob.models.FormTypes.TIME;

/**
 * Author: ARGHA K ROY
 * Date: 3/13/2016.
 */
public class FormViews {

    private LayoutInflater inflater;
    private Context mContext;
    private FieldChangeCallback fieldChangeCallback;

    public FormViews(Context context) {
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ArrayList<View> getFieldList(LinearLayout mainLayout, JSONArray serverFormFieldsArray, boolean resetLayout, FieldChangeCallback fieldChangeCallback) throws JSONException {
        if (fieldChangeCallback == null) fieldChangeCallback = new FieldChangeCallback() {
            @Override
            public void onFieldChange(View view) {

            }
        };

        this.fieldChangeCallback = fieldChangeCallback;
        if (resetLayout) mainLayout.removeAllViews();
        ArrayList<View> fieldArrayList = new ArrayList<>();
        for (int i = 0; i < serverFormFieldsArray.length(); i++) {
            View view = null;
            JSONObject field = serverFormFieldsArray.getJSONObject(i);
            if (resetLayout && field.has("repeat") && field.getBoolean("repeat")) continue;
            String fieldType = field.getString("type").toLowerCase();
            switch (fieldType) {
                case TEXT:
                    view = createTextInputField(field);
                    break;
                case NUMBER:
                    view = createNumberInputField(field);
                    break;
                case DECIMAL:
                    view = createDecimalInputField(field);
                    break;
                case DATE:
                case TIME:
                case DATETIME:
                    view = showDateTimeInputField(field);
                    break;
                case BOOLEAN:
                    view = createBooleanInputField(field);
                    break;
                case CHOICE:
                    view = createSingleChoiceDialog(field);
                    break;
                case MULTICHOICE:
                    view = createMultiChoiceDialog(field);
                    break;
                case SLIDER:
                    view = createSliderInputField(field);
                    break;
                case DROPDOWN:
                    view = createSpinnerInputField(field);
                    break;
            }

            if (view != null) {
                FormData dataModel = (FormData) view.getTag();
                if (!field.has("visibility") || (field.has("visibility") && !field.getString("visibility").equalsIgnoreCase("hidden"))) {
                    mainLayout.addView(view);
                }
                fieldArrayList.add(view);
            }
        }
        return fieldArrayList;
    }

    private View createSingleChoiceDialog(JSONObject fieldObject) throws JSONException {
        return showOptionChoiceField(fieldObject);
    }

    private View createMultiChoiceDialog(JSONObject fieldObject) throws JSONException {
        return showOptionChoiceField(fieldObject);
    }

    private View showOptionChoiceField(final JSONObject fieldObject) throws JSONException {
        FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        final View view = inflater.inflate(R.layout.field_button_input, null);
        showFieldHeaders(view, fieldObject);
        final Button fieldButton = (Button) view.findViewById(R.id.button);
        final JSONArray formOptions = fieldObject.getJSONObject("source").getJSONArray("options");
        for (int i = 0; i < formOptions.length(); i++) {
            JSONObject optionObject = formOptions.getJSONObject(i);
            if (optionObject.has("default") && optionObject.getBoolean("default")) {
                fieldButton.setText(optionObject.getString("label"));
                formData.setDefaultValue(optionObject.getString("label"));
                break;
            }
        }
        fieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showFieldOptionDialog(view, formOptions, fieldObject.getString("type").equalsIgnoreCase(MULTICHOICE));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        view.setTag(formData);
        return view;
    }

    AlertDialog alert;

    private void showFieldOptionDialog(final View view, final JSONArray options, boolean multichoice) throws JSONException {
        final Button fieldButton = (Button) view.findViewById(R.id.button);
        final String choices[] = new String[options.length()];
        final FormData dataModel = (FormData) view.getTag();
        int selected = -1;
        final boolean defaultSelection[] = new boolean[options.length()];
        final boolean userSelection[] = new boolean[options.length()];

        for (int i = 0; i < options.length(); i++) {
            JSONObject optionObject = options.getJSONObject(i);
            choices[i] = optionObject.getString("label");
            if (optionObject.has("default") && optionObject.getBoolean("default")) {
                selected = i;
                defaultSelection[i] = true;
            }
        }

        if (dataModel.getSavedValue() != null) {
            Arrays.fill(defaultSelection, false);
            Util.printDebug("Selected Values", dataModel.getSavedValue());
            String selectValues[] = dataModel.getSavedValue().split(",");
            for (String selectValue : selectValues) {
                for (int j = 0; j < choices.length; j++) {
                    if (selectValue.equalsIgnoreCase(choices[j])) {
                        defaultSelection[j] = true;
                        userSelection[j] = true;
                        selected = j;
                    }
                }
            }
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        if (multichoice) {
            alertDialog.setMultiChoiceItems(choices, defaultSelection, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    userSelection[which] = isChecked;
                }
            });
        } else {
            alertDialog.setSingleChoiceItems(choices, selected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    Arrays.fill(userSelection, false);
                    userSelection[pos] = true;
                    alert.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                }
            });
        }

        alertDialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int buttonPos) {
                int pos = -1;
                for (int i = 0; i < userSelection.length; i++) {
                    if (userSelection[i]) {
                        pos = i;
                        break;
                    }
                }
                if (pos == -1) return;

                String userChoiceData = "";
                for (int i = 0; i < userSelection.length; i++) {
                    if (userSelection[i]) {
                        userChoiceData += choices[i] + ",";
                    }
                }

                fieldButton.setText(userChoiceData.substring(0, userChoiceData.length() - 1));
                dataModel.setSavedValue(fieldButton.getText().toString());
                fieldChangeCallback.onFieldChange(view);
                dialog.cancel();
            }
        });

        alert = alertDialog.create();
        alert.show();
    }


    private View showDateTimeInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_button_input, null);
        showFieldHeaders(view, fieldObject);
        final Button fieldButton = (Button) view.findViewById(R.id.button);
        showFieldHeaders(view, fieldObject);
        final View dialogView = inflater.inflate(R.layout.form_date_time_picker_layout, null);
        final DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
        final TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);
        final String type = fieldObject.getString("type");
        switch (type) {
            case DATE:
                timePicker.setVisibility(View.GONE);
                fieldButton.setText("Pilih Date");
                break;
            case TIME:
                datePicker.setVisibility(View.GONE);
                fieldButton.setText("Pilih Time");
                break;
            case DATETIME:
                fieldButton.setText("Pilih Time & Date");
                break;
        }
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (type) {
                            case DATE:
                                fieldButton.setText(parseDateTime(datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear(), 0, 0));
                                break;
                            case TIME:
                                fieldButton.setText(parseDateTime(0, 0, 0, timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
                                break;
                            case DATETIME:
                                fieldButton.setText(parseDateTime(datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear(), timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
                                break;
                        }
                        FormData dataModel = (FormData) view.getTag();
                        dataModel.setSavedValue(fieldButton.getText().toString());
                        fieldChangeCallback.onFieldChange(view);
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setView(dialogView);
        fieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        if (fieldObject.getString("default").equalsIgnoreCase("NOW()")) {
            if (type.equals(DATE))
                fieldButton.setText(Util.getDate());
            else if (type.equals(TIME))
                fieldButton.setText(Util.getTime());
            else fieldButton.setText(Util.getDateTime());
        }

        FormData data = new FormData(fieldObject,fieldObject.getString("type"));
        if(fieldObject.has("default")) data.setDefaultValue(fieldObject.getString("default"));
        view.setTag(data);
        return view;
    }

    private String parseDateTime(int dayOfMonth, int month, int year, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        Date date = new Date(calendar.getTimeInMillis());
        if (dayOfMonth == 0 && month == 0 && year == 0) {
            SimpleDateFormat timeOnly = new SimpleDateFormat("hh':'mm aa");
            return timeOnly.format(date);
        } else if (hour == 0 && minute == 0) {
            SimpleDateFormat dateOnly = new SimpleDateFormat("dd'/'MM'/'yy");
            return dateOnly.format(date);
        } else {
            SimpleDateFormat timeDate = new SimpleDateFormat("dd'/'MM'/'yy hh':'mm aa");
            return timeDate.format(date);
        }
    }

    private View createSliderInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_slider_input, null);
        showFieldHeaders(view, fieldObject);
        final FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        final TextView sliderValueTv = (TextView) view.findViewById(R.id.sliderValueTv);
        DiscreteSeekBar seekBar = (DiscreteSeekBar) view.findViewById(R.id.fieldSeekbar);
        sliderValueTv.setText(String.valueOf(seekBar.getProgress()));

        int seekInterval = 1;
        if (fieldObject.has("source")) {
            if (fieldObject.getJSONObject("source").has("options")) {
                JSONObject fieldOptions = fieldObject.getJSONObject("source").getJSONObject("options");
                seekBar.setMax(fieldOptions.getInt("max"));
                seekBar.setMin(fieldOptions.getInt("min"));

                if (fieldOptions.has("interval")) seekInterval = fieldOptions.getInt("interval");
                if (fieldOptions.has("default")) {
                    formData.setDefaultValue(fieldOptions.getInt("default") + "");
                    seekBar.setProgress(fieldOptions.getInt("default"));
                    sliderValueTv.setText(String.valueOf(fieldOptions.getInt("default")));
                }
            }
        }

        final int finalSeekInterval = seekInterval;
        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int progress, boolean fromUser) {
                progress = (progress / finalSeekInterval) * finalSeekInterval;
                seekBar.setProgress(progress);
                if (fromUser) formData.setSavedValue(seekBar.getProgress() + "");

                sliderValueTv.setText(String.valueOf(progress));
                fieldChangeCallback.onFieldChange(view);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
        view.setTag(formData);
        return view;
    }

    private View createBooleanInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_boolean_input, null);
        FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        view.setTag(formData);

        showFieldHeaders(view, fieldObject);
        final JSONObject options = fieldObject.getJSONObject("source").getJSONObject("options");
        SwitchCompat switchField = (SwitchCompat) view.findViewById(R.id.formSwitch);
        switchField.setChecked(options.getBoolean("default"));

        String trueValue = "true";
        if (options.has("true")) {
            trueValue = options.getString("true");
        }
        String falseValue = "false";
        if (options.has("false")) {
            falseValue = options.getString("false");
        }
        final String finalTrueValue = trueValue;
        final String finalFalseValue = falseValue;
        switchField.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FormData formData = (FormData) view.getTag();
                formData.setSavedValue((isChecked) ? finalTrueValue : finalFalseValue);
                fieldChangeCallback.onFieldChange(view);
            }
        });
        formData.setDefaultValue((options.getBoolean("default")) ? switchField.getTextOn().toString() : switchField.getTextOff().toString());
        return view;
    }

    private View createSpinnerInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_spinner_input, null);
        final Spinner fieldSpinner = (Spinner) view.findViewById(R.id.fieldSpinner);
        JSONObject optionsObject = fieldObject.getJSONObject("source");
        DropdownOptions dropdownOptions = new Gson().fromJson(optionsObject.toString(), DropdownOptions.class);
        fieldSpinner.setAdapter(new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item, dropdownOptions.getOptions()));
        showFieldHeaders(view, fieldObject);
        final FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        view.setTag(formData);
        fieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                formData.setSavedValue(fieldSpinner.getSelectedItem().toString());
                fieldChangeCallback.onFieldChange(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                formData.setDefaultValue(fieldSpinner.getSelectedItem().toString());
            }
        });
        return view;
    }

    private View createTextInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_text_input, null);
        FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        view.setTag(formData);
        modifyViewInputField(view, fieldObject);
        showFieldHeaders(view, fieldObject);
        return view;
    }

    private View createNumberInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_text_input, null);
        FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        view.setTag(formData);
        modifyViewInputField(view, fieldObject);
        showFieldHeaders(view, fieldObject);
        return view;
    }

    private View createDecimalInputField(JSONObject fieldObject) throws JSONException {
        final View view = inflater.inflate(R.layout.field_text_input, null);
        FormData formData = new FormData(fieldObject,fieldObject.getString("type"));
        view.setTag(formData);
        modifyViewInputField(view, fieldObject);
        showFieldHeaders(view, fieldObject);
        return view;
    }

    private void modifyViewInputField(final View view, final JSONObject fieldObject) throws JSONException {
        final EditText inputTextField = (EditText) view.findViewById(R.id.inputEditText);
        String type = fieldObject.getString("type");
        final FormData formData = (FormData) view.getTag();
        if (fieldObject.has("hint")) {
            inputTextField.setHint(fieldObject.getString("hint"));
        }
        if (fieldObject.has("default")) {
            inputTextField.setText(fieldObject.getString("default"));
            formData.setDefaultValue(fieldObject.getString("default"));
        }
        switch (type) {
            case NUMBER:
                inputTextField.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case TEXT:
                break;
            case DECIMAL:
                inputTextField.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
        }

        inputTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                formData.setSavedValue(inputTextField.getText().toString());
                fieldChangeCallback.onFieldChange(view);
            }
        });
    }

    public static void showFieldHeaders(View view, JSONObject fieldObject) throws JSONException {
        TextView headerTv = (TextView) view.findViewById(R.id.fieldHeaderTV);
        headerTv.setText(fieldObject.getString("label"));

        if (fieldObject.has("description")) {
            TextView subHeaderTv = (TextView) view.findViewById(R.id.fieldSubHeaderTV);
            subHeaderTv.setText(fieldObject.getString("label"));
            subHeaderTv.setVisibility(View.VISIBLE);
        }
    }

    public static void loadEditableData(JSONArray dataArray, ArrayList<View> fieldList) throws JSONException {
        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject dataObj = dataArray.getJSONObject(i);
            String name = dataObj.getString("name");
            String value = dataObj.getString("value");
            for (int j = 0; j < fieldList.size(); j++) {
                View fieldView = fieldList.get(j);
                FormData formData = (FormData) fieldView.getTag();
                JSONObject fieldJson = formData.getFieldJsonObject();
                if (name.equals(fieldJson.getString("name"))) {
                    formData.setSavedValue(value);
                    String fieldType = formData.getType();
                    switch (fieldType) {
                        case TEXT:
                        case NUMBER:
                            EditText editText = (EditText) fieldView.findViewById(R.id.inputEditText);
                            editText.setText(value);
                            break;
                        case DATE:
                        case TIME:
                        case DATETIME:
                        case CHOICE:
                            Button buttonView = (Button) fieldView.findViewById(R.id.button);
                            buttonView.setText(value);
                            break;
                        case BOOLEAN:
                            SwitchCompat switchBtn = (SwitchCompat) fieldView.findViewById(R.id.formSwitch);
                            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("Ye"))
                                switchBtn.setChecked(true);
                            else switchBtn.setChecked(false);
                            break;
                        case SLIDER:
                            DiscreteSeekBar seekBar = (DiscreteSeekBar) fieldView.findViewById(R.id.fieldSeekbar);
                            TextView seekBarTv = (TextView) fieldView.findViewById(R.id.sliderValueTv);
                            seekBarTv.setText(value);
                            seekBar.setProgress(Integer.parseInt(value));
                            break;
                        case DROPDOWN:
                            Spinner spinner = (Spinner) fieldView.findViewById(R.id.fieldSpinner);
                            for (int x=0;x<spinner.getAdapter().getCount();x++){
                                if(value.equals(spinner.getAdapter().getItem(x).toString())){
                                    spinner.setSelection(x);
                                    break;
                                }
                            }
                            break;
                    }
                    //Util.printDebug("Field Json",fieldJson.toString());
                    break;
                }
            }
        }
    }
}
