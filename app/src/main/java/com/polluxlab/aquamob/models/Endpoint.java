package com.polluxlab.aquamob.models;

import java.util.List;

/**
 * Api endpoint /api traversing class via gson
 * Created by ARGHA K ROY on 1/20/2016.
 */
public class Endpoint {
    private String title;
    private String version;
    private Resources resources;

    public String getProfileUrl(){
        return resources.getProfile().getProfileUrl();
    }

    class Resources{
        private Profile profile;
        private User user;

        public Profile getProfile(){
            return profile;
        }
    }

    class User{
        private String title;
        private List<Links> links;
    }

    class Profile{
        private String title;
        private List<Links> links;

        public String getProfileUrl(){
            return links.get(0).getHref();
        }
    }
}
