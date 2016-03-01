package com.polluxlab.aquamob;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.polluxlab.aquamob.callbacks.LoginResponse;
import com.polluxlab.aquamob.helpers.LoginRequest;
import com.polluxlab.aquamob.helpers.PrefHelper;
import com.polluxlab.aquamob.models.User;
import com.polluxlab.aquamob.utils.AppConst;
import com.polluxlab.aquamob.utils.Util;
import com.polluxlab.utils.ServerLinks;

public class LoginActivity extends AppCompatActivity {

    private EditText mUserNameEt;
    private Context mContext;
    private PrefHelper mPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        mContext=this;
        mPrefHelper=new PrefHelper(this);
        mUserNameEt= (EditText) findViewById(R.id.loginUserNameEt);

        if(mPrefHelper.getCurrentUser()!=null) startHomeActivity();
    }

    private void startHomeActivity() {
        Intent userIntent=new Intent(this,MainActivity.class);
        startActivity(userIntent);
        finish();
    }

    public void btnClick(View view){
        switch (view.getId()){
            case R.id.loginCreateAccBtn:
                String newAccUrl=AppConst.NEW_ACC_LINK;
                Intent i1 = new Intent(Intent.ACTION_VIEW);
                i1.setData(Uri.parse(newAccUrl));
                startActivity(i1);
                break;
            case R.id.loginForgotPassBtn:
                String forgotUrl= AppConst.FORGOT_USERNAME_LINK;
                Intent i2 = new Intent(Intent.ACTION_VIEW);
                i2.setData(Uri.parse(forgotUrl));
                startActivity(i2);
                break;
            case R.id.loginSubmitBtn:
                loginUser(mUserNameEt.getText().toString());
                break;
        }
    }

    private void loginUser(final String userName) {
        if(TextUtils.isEmpty(userName)){
            Util.showToast(mContext,"Please enter your username");
            return;
        }

        if(!Util.isConnectedToInternet(this)){
            Util.showNoInternetDialog(this);
            return;
        }

        final ProgressDialog dialog=Util.getProgressDialog(this,"Checking login. Please wait...");
        dialog.show();
        LoginRequest mLoginRequest = new LoginRequest(new LoginResponse() {
            @Override
            public void successResponse(int statusCode, User user) {
                dialog.dismiss();
                if(user==null || user.getFormsUrl()==null){
                    Util.printDebug("Retrieving form url error",statusCode+"");
                    return;
                }
                user.setUserName(userName);
                mPrefHelper.saveUserInfo(user);
                mPrefHelper.saveCurrentUser(user);
                Util.printDebug("Forms url", user.getFormsUrl());
                Util.printDebug("Company name", user.getCompanyName());
                startHomeActivity();
            }

            @Override
            public void errorResponse(int statusCode, String responseString) {
                dialog.dismiss();
                Util.printDebug("Login", "failed " + statusCode + " " + responseString);
                Util.showToast(LoginActivity.this,"Username not found");
            }
        });
        mLoginRequest.login(ServerLinks.ENDPOINT,userName);
    }
}
