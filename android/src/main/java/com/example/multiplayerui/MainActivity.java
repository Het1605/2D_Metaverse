package com.example.multiplayerui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.yourproject.youename.R;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MICROPHONE_PERMISSION = 1;
    private LinearLayout mainBox, createBox, joinBox;
    private TextView mapCodeText;
    private EditText mapCodeInput;
    private String generatedCode = "";

    public static HashMap<String,String> hashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE_PERMISSION);
            }
        }

        mainBox = findViewById(R.id.mainBox);
        createBox = findViewById(R.id.createRoomBox);
        joinBox = findViewById(R.id.joinRoomBox);

        Button btnCreate = findViewById(R.id.btnCreateRoom);
        Button btnJoin = findViewById(R.id.btnJoinRoom);
        Button btnBackFromCreate = findViewById(R.id.btnBackFromCreate);
        Button btnBackFromJoin = findViewById(R.id.btnBackFromJoin);
        Button btnFinalCreate = findViewById(R.id.btnFinalCreateRoom);
        Button btnJoinMap = findViewById(R.id.btnJoinMap);

        mapCodeText = findViewById(R.id.textMapCode);
        mapCodeInput = findViewById(R.id.editMapCode);

        btnCreate.setOnClickListener(v -> {
            generatedCode = generateRandomCode();
            mapCodeText.setText(generatedCode);
            mainBox.setVisibility(View.GONE);
            createBox.setVisibility(View.VISIBLE);
        });

        btnJoin.setOnClickListener(v -> {
            mainBox.setVisibility(View.GONE);
            joinBox.setVisibility(View.VISIBLE);
        });

        btnBackFromCreate.setOnClickListener(v -> {
            createBox.setVisibility(View.GONE);
            mainBox.setVisibility(View.VISIBLE);
        });

        btnBackFromJoin.setOnClickListener(v -> {
            joinBox.setVisibility(View.GONE);
            mainBox.setVisibility(View.VISIBLE);
        });

        btnFinalCreate.setOnClickListener(v -> {
            hashMap.put("creating", "true");
            goToLibGDX(generatedCode);
        });

        btnJoinMap.setOnClickListener(v -> {
            String enteredCode = mapCodeInput.getText().toString().trim();
            if (!enteredCode.isEmpty()) {
                hashMap.put("creating", "false");
                goToLibGDX(enteredCode);
            } else {
                Toast.makeText(this, "Invalid code!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLibGDX(String code) {
        Intent intent = new Intent(this, com.yourproject.youename.android.AndroidLauncher.class); // Point to LibGDX launcher
        intent.putExtra("MAP_CODE", code);
        startActivity(intent);
        finish();
    }

    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
