package com.example.multiplayerui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.yourproject.youename.R;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    EditText nameInput, nicknameInput, passwordInput;
    Button signupButton;

    TextView link;

    String signupUrl = "https://twodmetaverse-qfs5.onrender.com/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        nameInput = findViewById(R.id.editTextFullName);
        nicknameInput = findViewById(R.id.nicknameInput);
        passwordInput = findViewById(R.id.editTextPassword);
        signupButton = findViewById(R.id.buttonSignup);
        link = findViewById(R.id.textLogin);

        link.setOnClickListener(v-> goToLogin());

        signupButton.setOnClickListener(v -> sendSignupRequest());

    }
    private void sendSignupRequest() {
        String username = nameInput.getText().toString();
        String nickname = nicknameInput.getText().toString();
        String password = passwordInput.getText().toString();
        // Show a progress dialog or toast
        Toast.makeText(SignupActivity.this, "Signing up...", Toast.LENGTH_SHORT).show();

        StringRequest request = new StringRequest(Request.Method.POST, signupUrl,
            response -> {
                Toast.makeText(SignupActivity.this, "Signup Success!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(SignupActivity.this,Login.class);
                startActivity(intent);
                // Navigate to login or home screen here
            },
            error -> {
                Toast.makeText(SignupActivity.this, "Error: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("nickname", nickname);
                params.put("password", password);
                return params;
            }
        };

        // Add to request queue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    public void goToLogin() {
        Intent intent = new Intent(SignupActivity.this,Login.class);
        startActivity(intent);
    }
}
