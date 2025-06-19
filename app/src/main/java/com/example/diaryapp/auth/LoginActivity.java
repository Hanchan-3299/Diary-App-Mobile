package com.example.diaryapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.networkHelper.NetworkCallBackUtil;
import com.example.diaryapp.networkHelper.NetworkUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnCusLogin, btnRegister;
    SignInButton btnGoogleLogin;
    TextView txtRegister;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 9001;

    private NetworkCallBackUtil networkCallbackHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        networkCallbackHelper = new NetworkCallBackUtil();

        //cek apakah user sudah login
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(this, MainActivity.class));
            Toast.makeText(this, "Welcome Back !", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        //inisialisasi elemen
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnCusLogin = findViewById(R.id.btnLoginLogin);
        btnGoogleLogin = findViewById(R.id.btnLoginGoogleLogin);
        txtRegister = findViewById(R.id.txtLoginRegister);


        //konfig google sign up
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        //login via email dan password
        btnCusLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Email or Password is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Email's format is wrong", Toast.LENGTH_SHORT).show();
                return;
            }

            if(password.length() < 6) {
                Toast.makeText(this, "Password's length at least have 6 digit", Toast.LENGTH_LONG).show();
                return;
            }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!NetworkUtil.isInternetAvailable(this)) {
                        Toast.makeText(this, "No Internet, Please check your connection.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (task.isSuccessful()){

                        boolean isNewUser = Objects.requireNonNull(task.getResult().getAdditionalUserInfo()).isNewUser();

                        if (isNewUser){
                            Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(this, "Welcome back !", Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }else {
                        Toast.makeText(this, "Failed to log in " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });


        //Login dengan Google
        btnGoogleLogin.setOnClickListener(v -> {
            if (!NetworkUtil.isInternetAvailable(this)) {
                Toast.makeText(this, "No Internet, Please check your connection.", Toast.LENGTH_LONG).show();
                return;
            }

            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
        });


        //navigasi ke register
        txtRegister.setOnClickListener(v -> {
            if (!NetworkUtil.isInternetAvailable(this)) {
                Toast.makeText(this, "No Internet, Please check your connection.", Toast.LENGTH_LONG).show();
                return;
            }
            startActivity(new Intent(this, RegisterActivity.class));
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkCallbackHelper.registerNetworkCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkCallbackHelper.unregisterNetworkCallback(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthHelper.fireBaseAuthWithGoogle(LoginActivity.this, account.getIdToken());
            } catch (ApiException e) {
                if (e.getStatusCode() == 12501){
                    Toast.makeText(this, "Google Sign In Canceled", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Google Sign In Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}