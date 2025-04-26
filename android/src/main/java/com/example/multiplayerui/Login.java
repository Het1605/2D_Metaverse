package com.example.multiplayerui;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.toolbox.JsonObjectRequest;
import com.yourproject.youename.R;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.multiplayerui.MainActivity;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    EditText nameInput, passwordInput;
    Button LoginButton;

    String loginUrl = "https://twodmetaverse-qfs5.onrender.com/login";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameInput = findViewById(R.id.editUserName);
        passwordInput = findViewById(R.id.editPassword);
        LoginButton = findViewById(R.id.buttonLogin);

        LoginButton.setOnClickListener(v -> sendLoginRequest());

    }
    private void sendLoginRequest() {
        String username = nameInput.getText().toString();
        String password = passwordInput.getText().toString();

        Toast.makeText(Login.this, "Logging in...", Toast.LENGTH_SHORT).show();

        String loginUrl = "https://twodmetaverse-qfs5.onrender.com/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(Login.this, "JSON error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, loginUrl, jsonBody,
            response -> {
                try {
                    if (response.has("user")) {
                        JSONObject user = response.getJSONObject("user");

                        String usernameVal = user.getString("username");
                        String nicknameVal = user.getString("nickname");
                        String idVal = user.getString("_id");

                        MainActivity.hashMap.put("username", usernameVal);
                        MainActivity.hashMap.put("nickname", nicknameVal);
                        MainActivity.hashMap.put("Id", idVal);

                        Toast.makeText(Login.this, "Login Success!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Login.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(Login.this, "Parse Error", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                error.printStackTrace();
                Toast.makeText(Login.this, "Volley Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Set timeout
        jsonRequest.setRetryPolicy(new DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonRequest);
    }
}
