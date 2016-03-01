package com.polluxlab.aquamob.helpers;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.polluxlab.aquamob.callbacks.LoginResponse;
import com.polluxlab.aquamob.models.Endpoint;
import com.polluxlab.aquamob.models.Profile;
import com.polluxlab.aquamob.models.User;
import com.polluxlab.aquamob.utils.Util;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * User login and endpoint mapping class
 * Created by ARGHA K ROY on 1/20/2016.
 */
public class LoginRequest {
    LoginResponse loginResponse;
    AsyncHttpClient httpClient;

    public LoginRequest(LoginResponse loginResponse){
        this.loginResponse = loginResponse;
    }

    public void login(String ENDPOINT,String username){
        httpClient=HTTPHelper.getHTTPClient(username,"");
        httpClient.get(ENDPOINT, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                Endpoint endpoint = gson.fromJson(response.toString(), Endpoint.class);
                final String PROFILE_URL=endpoint.getProfileUrl();
                Util.printDebug("Profile URL", PROFILE_URL);
                getProfileData(PROFILE_URL);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loginResponse.errorResponse(statusCode, responseString);
            }
        });
    }

    private void getProfileData(String URL){
        httpClient.get(URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                Profile profile = gson.fromJson(response.toString(), Profile.class);
                final String USER_URL=profile.getUserUrl();
                Util.printDebug("User URL",USER_URL);
                getUserData(USER_URL);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loginResponse.errorResponse(statusCode, responseString);
            }
        });
    }

    private void getUserData(String URL) {
        httpClient.get(URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);
                loginResponse.successResponse(statusCode,user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loginResponse.errorResponse(statusCode, responseString);
            }
        });
    }
}
