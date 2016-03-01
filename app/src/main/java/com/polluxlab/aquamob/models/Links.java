package com.polluxlab.aquamob.models;

import java.io.Serializable;

/**
 * Api traversing nested object class via gson
 * Created by ARGHA K ROY on 1/21/2016.
 */
class Links implements Serializable {
    private String rel;
    private String href;
    private String type;
    private String title;

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }
}