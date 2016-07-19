package com.polluxlab.aquamob.models;

import java.util.ArrayList;

/**
 * Author: ARGHA K ROY
 * Date: 3/31/2016.
 */
public class DropdownOptions {
    private ArrayList<Option> options;

    public ArrayList<Option> getOptions() {
        return options;
    }

    public class Option{
        private String id;
        private String label;

        @Override
        public String toString() {
            return label;
        }
    }
}
