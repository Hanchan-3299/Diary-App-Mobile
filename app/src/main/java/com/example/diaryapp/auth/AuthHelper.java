package com.example.diaryapp.auth;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.networkHelper.NetworkUtil;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
import java.util.concurrent.Executor;

public class AuthHelper {
    private static final int RC_SIGN_IN = 9001;


    //Autentikasi Firebase dengan token Google
    public static void fireBaseAuthWithGoogle (Activity activity, String idToken){
        if (!NetworkUtil.isInternetAvailable(activity)) {
            Toast.makeText(activity, "No Internet, Please check your connection.", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if(task.isSuccessful()){
                        boolean isNewUser = Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser();

                        if(isNewUser){
                            Toast.makeText(activity, "Welcome", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(activity, "Welcome Back !", Toast.LENGTH_SHORT).show();
                        }
                        activity.startActivity(new Intent(activity, MainActivity.class));
                        activity.finish();
                    }else{
                        Toast.makeText(activity, "Firebase Google Auth Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void loginChecker (Activity activity, FirebaseUser user) {
        if (!NetworkUtil.isInternetAvailable(activity)) {
            Toast.makeText(activity, "No Internet, Please check your connection.", Toast.LENGTH_LONG).show();
            return;
        }
        if (user == null){
            Toast.makeText(activity, "You haven't logged in yet", Toast.LENGTH_SHORT).show();
            activity.finish();
            return;
        }
    }
}
