package com.polluxlab.aquamob.models;

import java.io.Serializable;

/**
 * Author: ARGHA K ROY
 * Date: 3/14/2016.
 */
public class FormField implements Serializable {
    private String name;
    private String label;
    private String type;
    private boolean remember;
    private Validation validation;


    class Validation{
        private String description;
        private boolean required;
    }

    class Source{
        private boolean conditional;
        private String realm;
        private Conditions conditions;
    }

    class Conditions{
        private String type;
    }

    class Options{
        private int min;
        private int max;
    }
}
