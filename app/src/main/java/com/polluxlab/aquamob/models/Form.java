package com.polluxlab.aquamob.models;

/**
 * Created by ARGHA K ROY on 5/26/2015.
 */
public class Form {
    private String id;
    private String name;
    private String formId;

    public Form(String id,String formId,String name){
        this.id=id;
        this.formId =formId;
        this.name=name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormId() {
        return formId;
    }

    @Override
    public String toString() {
        return name;
    }
}
