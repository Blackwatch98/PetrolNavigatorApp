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

import com.example.petrolnavigatorapp.utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mLogin, mPassword;
    private Button confirmBtn;
    private ProgressBar progressBar;
    private TextView loginReference;
    private FirebaseFirestore fireStore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

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
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String login = mLogin.getText().toString().trim();
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
                            userId = mAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fireStore.collection("users").document(userId);
                            User user = new User(userId, login);
                            documentReference.set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterActivity.this, "Udało ci się zarejestrować :)", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), InitialSettingsActivity.class));
                                    finish();
                                }
                            })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(RegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    }
                                });
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