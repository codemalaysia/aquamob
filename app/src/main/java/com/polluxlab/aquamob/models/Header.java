package com.polluxlab.aquamob.models;

import java.io.Serializable;

/**
 * Author ARGHA_K_ROY
 * Date 5/26/2016.
 */
public class Header implements Serializable {

    private String id;
    private String name;
    private String value;

    public Header(String id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return id+":"+name+":"+value;
    }
}
