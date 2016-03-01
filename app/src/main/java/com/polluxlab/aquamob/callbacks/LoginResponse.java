package com.polluxlab.aquamob.callbacks;


import com.polluxlab.aquamob.models.User;

/**
 * Interface for Asynchttphelper response callback
 * Created by ARGHA K ROY on 1/20/2016.
 */
public interface LoginResponse {

     void successResponse(int statusCode, User user);
     void errorResponse(int statusCode, String responseString);
}
