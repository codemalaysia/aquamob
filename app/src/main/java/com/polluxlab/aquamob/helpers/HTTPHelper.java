package com.polluxlab.aquamob.helpers;

import com.loopj.android.http.AsyncHttpClient;
import com.polluxlab.aquamob.utils.AppConst;

/**
 * Created by ARGHA K ROY on 5/19/2015.
 */
public class HTTPHelper {

    public static AsyncHttpClient getHTTPClient(){
        AsyncHttpClient httpClient=new AsyncHttpClient();
        httpClient.setMaxRetriesAndTimeout(AppConst.MAX_CONNECTION_TRY, AppConst.TIME_OUT);
        return httpClient;
    }

    public static AsyncHttpClient getHTTPClient(String username,String password){
        AsyncHttpClient httpClient=getHTTPClient();
        httpClient.setBasicAuth(username, password);
        return httpClient;
    }

    public static AsyncHttpClient getHTTPClient(String username,String password,String plxClientId){
        AsyncHttpClient httpClient=getHTTPClient();
        httpClient.setBasicAuth(username, password);
        httpClient.addHeader("plx-client-id",plxClientId);
        return httpClient;
    }

    public static AsyncHttpClient getParseHTTPClient(){
        AsyncHttpClient httpClient=getHTTPClient();
        httpClient.addHeader("X-Parse-Application-Id","STYqMP4SSyzwpMVCMSPMzfBt1aJco0T0w8SWgUAV");
        httpClient.addHeader("Content-Type","application/json");
        httpClient.addHeader("X-Parse-REST-API-Key", "bSqkwck6I6PGg3AC10PDIGfn0ylKSHJPlErlGLn9");
        return httpClient;
    }
}
