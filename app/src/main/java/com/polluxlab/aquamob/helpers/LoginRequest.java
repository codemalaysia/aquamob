package com.polluxlab.aquamob.helpers;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.polluxlab.aquamob.callbacks.LoginResponse;
import com.polluxlab.aquamob.models.Endpoint;
import com.polluxlab.aquamob.models.Profile;
import com.polluxlab.aquamob.models.User;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.utils.ServerLinks;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * User login and endpoint mapping class
 * Created by ARGHA K ROY on 1/20/2016.
 */
public class LoginRequest {
    LoginResponse loginResponse;
    AsyncHttpClient httpClient;
    Context mContext;

    public LoginRequest(Context context,LoginResponse loginResponse) {
        this.loginResponse = loginResponse;
        mContext=context;
    }

    public void login(String ENDPOINT, String username) {
        httpClient = HTTPHelper.getHTTPClient(username, "");
        httpClient.get(ENDPOINT, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                Endpoint endpoint = gson.fromJson(response.toString(), Endpoint.class);
                final String INSTALLATION_ID_URL = endpoint.getInstallationId();
                Util.printDebug("Installation URL", INSTALLATION_ID_URL);
                getPLXClientId(INSTALLATION_ID_URL,endpoint);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loginResponse.errorResponse(statusCode, responseString);
            }
        });
    }

    private void getPLXClientId(final String INSTALLATION_ID_URL, final Endpoint endpoint) {
        httpClient.get(INSTALLATION_ID_URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    final String PLX_CLIENT_ID = response.getString("id");
                    new PrefHelper(mContext).savePLXId(PLX_CLIENT_ID);
                    httpClient.addHeader("plx-client-id",PLX_CLIENT_ID);
                    httpClient.get(ServerLinks.ENDPOINT,null,new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            Gson gson = new Gson();
                            Endpoint endpoint = gson.fromJson(response.toString(), Endpoint.class);
                            final String PROFILE_URL = endpoint.getProfileUrl();
                            Util.printDebug("Profile URL", PROFILE_URL);
                            getProfileData(PROFILE_URL);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.printDebug("plx client id error", e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loginResponse.errorResponse(statusCode, responseString);
            }
        });
    }

    private void getProfileData(String URL) {
        httpClient.get(URL, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                Profile profile = gson.fromJson(response.toString(), Profile.class);
                final String USER_URL = profile.getUserUrl();
                Util.printDebug("User URL", USER_URL);
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
                loginResponse.successResponse(statusCode, user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                loginResponse.errorResponse(statusCode, responseString);
            }
        });
    }
}
