package com.polluxlab.aquamob.linked;

import java.util.ArrayList;

/**
 * Author: ARGHA K ROY
 * Date: 3/31/2016.
 */
public class Child {

    private String id;
    private String title;
    private ArrayList<Child> units;

    public String getId() {
        return id;
    }

    public String getLabel() {
        return title;
    }

    public ArrayList<Child> getChildList() {
        return units;
    }

    @Override
    public String toString() {
        return title;
    }
}
