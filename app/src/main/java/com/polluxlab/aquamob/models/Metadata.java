package com.polluxlab.aquamob.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Author ARGHA_K_ROY
 * Date 5/28/2016.
 */
public class Metadata implements Serializable{


    @SerializedName("offline-label")
    private OfflineLabel offlineLabel;

    public class OfflineLabel{
        private Title main_title;
        private Title sub_title;

        public Title getMain_title() {
            return main_title;
        }

        public Title getSub_title() {
            return sub_title;
        }
    }

    public class Title{
        private String format;
        private String fields[];

        public String getFormat() {
            return format;
        }

        public String[] getFields() {
            return fields;
        }
    }

    public OfflineLabel getOfflineLabel() {
        return offlineLabel;
    }
}
