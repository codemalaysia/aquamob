package com.polluxlab.aquamob.models;

import java.io.Serializable;
import java.util.List;

/**
 * Api endpoint /api/user/5 traversing class via gson
 * Created by ARGHA K ROY on 1/21/2016.
 */
public class User implements Serializable {

    private String title;
    private String id;
    private String active;
    private List<Links> links;
    private String companyName;
    private String userName;

    public String getTitle() {
        return title;
    }

    public String getFormsUrl(){
        for (Links link :links) {
            if(link.getRel().equals("forms")){
                return link.getHref();
            }
        }
        return null;
    }

    public String getCompanyName(){
        for (Links link :links) {
            if(link.getRel().equals("company")){
                return link.getTitle();
            }
        }
        return null;
    }

    public String getUserName(){
        return userName;
    }

    public void setUserName(String userName){
        this.userName=userName;
    }

    @Override
    public String toString() {
        return userName;
    }
}
