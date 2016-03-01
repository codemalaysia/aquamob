package com.polluxlab.aquamob.models;

import java.util.List;

/**
 * Api endpoint /api/profile traversing class via gson
 * Created by ARGHA K ROY on 1/21/2016.
 */
public class Profile {
    private String title;
    private List<Links> links;

    public String getUserUrl(){
        for (Links link :links) {
            if(link.getRel().equals("user")){
                return link.getHref();
            }
        }
        return null;
    }
}
