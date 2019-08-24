package com.ndn.jwtan.identitymanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GenerateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
    }

    public void http(View view) {
        Intent intent = new Intent(this, GenerateToken.class);
        startActivity(intent);
    }

    public void ndncert(View view) {
        Intent intent = new Intent(this, GenerateNDNToken.class);
        startActivity(intent);
    }
}
