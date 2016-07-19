package com.polluxlab.aquamob.models;

import java.io.Serializable;

/**
 * Author ARGHA_K_ROY
 * Date 5/28/2016.
 */
public class SaveData implements Serializable {

    private String dataId;
    private String formId;
    private String formData;

    public SaveData(String dataId, String formId, String formData) {
        this.dataId = dataId;
        this.formId = formId;
        this.formData = formData;
    }

    public String getDataId() {
        return dataId;
    }

    public String getFormId() {
        return formId;
    }

    public String getFormData() {
        return formData;
    }

    @Override
    public String toString() {
        return "SaveData{" +
                "dataId='" + dataId + '\'' +
                ", formId='" + formId + '\'' +
                ", formData='" + formData + '\'' +
                '}';
    }
}
