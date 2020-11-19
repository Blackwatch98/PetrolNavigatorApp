package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mLogin, mPassword;
    private Button confirmBtn;
    private ProgressBar progressBar;
    private TextView loginReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mLogin = findViewById(R.id.registerTextEmailAddress);
        mPassword = findViewById(R.id.registerTextPassword);
        confirmBtn = findViewById(R.id.registerConfirmBtn);
        progressBar = findViewById(R.id.progressBar2);
        loginReference = findViewById(R.id.loginReferenceText);

        if(mAuth.getCurrentUser() != null)
        {
            //sprawdź czy wybrany został przynajmniej promień
            startActivity(new Intent(getApplicationContext(), InitialSettingsActivity.class));
            finish();
        }

        loginReference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String login = mLogin.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (TextUtils.isEmpty(login)) {
                    mLogin.setError("Login jest wymagany!");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPassword.setError("Hasło jest wymagane!");
                    return;
                }

                if (password.length() < 6) {
                    mPassword.setError("Hasło za krótkie! (min. 6 znaków)");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(login, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            startActivity(new Intent(getApplicationContext(), InitialSettingsActivity.class));
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}