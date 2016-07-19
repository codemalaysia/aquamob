package com.polluxlab.aquamob.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.polluxlab.aquamob.models.User;
import com.polluxlab.aquamob.utils.AppConst;
import com.polluxlab.aquamob.utils.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * All sharedpreferance save data and retrieve in this class
 * Created by ARGHA K ROY on 6/1/2015.
 */
public class PrefHelper {
    SharedPreferences preferences;
    Gson gson;

    private static final String PLX_CLIENT_ID_PREF="plx_client_id_pref";

    public PrefHelper(Context context){
        gson=new Gson();
        preferences=context.getSharedPreferences(AppConst.APP_PREF, Context.MODE_PRIVATE);
    }

    public User getCurrentUser(){
        String userJson=preferences.getString(AppConst.PREF_USERNAME,null);
        if(userJson==null) return null;
        return gson.fromJson(userJson,User.class);
    }

    public void saveCurrentUser(User user){
        SharedPreferences.Editor editor=preferences.edit();
        Gson gson=new Gson();
        String userJson=gson.toJson(user);
        editor.putString(AppConst.PREF_USERNAME,userJson);
        editor.apply();
    }

    public void saveFormId(String formId){
        Set<String> formIdSet=preferences.getStringSet(AppConst.PREF_FORM_IDS,new HashSet<String>());
        formIdSet.add(formId);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putStringSet(AppConst.PREF_FORM_IDS, formIdSet);
        editor.apply();
    }

    public Set<String> getFormIds(){
        return preferences.getStringSet(AppConst.PREF_FORM_IDS, new HashSet<String>());
    }

    public void saveUserInfo(User user){
        Gson gson=new Gson();
        String userJson=gson.toJson(user);
        Util.printDebug("User json", userJson);
        Set<String> userInfoSet=preferences.getStringSet(AppConst.PREF_USER_INFOS,new HashSet<String>());
        userInfoSet.add(userJson);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putStringSet(AppConst.PREF_USER_INFOS, userInfoSet);
        editor.apply();
    }

    public ArrayList<User> getUsersInfo(){
        Set<String> stringSet = preferences.getStringSet(AppConst.PREF_USER_INFOS, new HashSet<String>());
        Gson gson = new Gson();
        ArrayList<User> users=new ArrayList<>();
        for (String set:stringSet){
            User user=gson.fromJson(set,User.class);
            users.add(user);
        }
        return users;
    }

    public boolean clearAllPref(){
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.apply();
        return true;
    }

    public void savePLXId(String plxClientId){
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(PLX_CLIENT_ID_PREF,plxClientId);
        editor.apply();
    }

    public String getPlxClientId(){
        return preferences.getString(PLX_CLIENT_ID_PREF,null);
    }
}
