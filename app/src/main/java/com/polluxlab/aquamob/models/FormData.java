package com.polluxlab.aquamob.models;

import org.json.JSONObject;

/**
 * Created by ARGHA K ROY on 4/24/2015.
 */
public class FormData {
    private JSONObject fieldJsonObject;
    private String savedValue;
    private String defaultValue;
    private String type;
    private String exportValue;

    public FormData(JSONObject fieldJsonObject, String type) {
        this.fieldJsonObject = fieldJsonObject;
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getSavedValue() {
        return savedValue;
    }

    public void setSavedValue(String savedValue) {
        this.savedValue = savedValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public JSONObject getFieldJsonObject() {
        return fieldJsonObject;
    }

    public String getType() {
        return type;
    }

    public String getExportValue() {
        return exportValue;
    }

    public void setExportValue(String exportValue) {
        this.exportValue = exportValue;
    }
}
