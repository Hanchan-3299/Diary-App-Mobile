package com.example.diaryapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.networkHelper.NetworkCallBackUtil;
import com.example.diaryapp.networkHelper.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    EditText etEmail, etPassword;
    TextView txtLogin;
    Button btnregister;
    FirebaseAuth mAuth;

    private NetworkCallBackUtil networkCallbackHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        networkCallbackHelper = new NetworkCallBackUtil();

        //cek apakah user sudah login
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(this, MainActivity.class));
            Toast.makeText(this, "Welcome Back !", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //inisialisasi elemen
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        txtLogin = findViewById(R.id.txtRegisterLogin);
        btnregister = findViewById(R.id.btnRegisterRegister);
        mAuth = FirebaseAuth.getInstance();

        //Register
        btnregister.setOnClickListener(v -> {
            if (!NetworkUtil.isInternetAvailable(this)) {
                Toast.makeText(this, "No Internet, Please check your connection.", Toast.LENGTH_LONG).show();
                return;
            }

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Email & Password is empty", Toast.LENGTH_LONG).show();
                return;
            }

            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Email's Format is wrong", Toast.LENGTH_LONG).show();
                return;
            }

            if(password.length() < 6) {
                Toast.makeText(this, "Password's length at least have 6 digit", Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task ->{
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account Successfully Created", Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            Toast.makeText(this, "Account Failed to Create Account", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        //navigasi ke login
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
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
}