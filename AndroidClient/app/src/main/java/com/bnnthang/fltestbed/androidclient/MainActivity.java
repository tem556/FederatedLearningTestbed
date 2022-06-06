package com.bnnthang.fltestbed.androidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startbutton_onclick(View view) throws Exception {
        String host = ((EditText) findViewById(R.id.editTextHost)).getText().toString();
        int port = Integer.parseInt(((EditText) findViewById(R.id.editTextPort)).getText().toString());

        ClientThread clientThread = new ClientThread(getFilesDir(), host, port);
        clientThread.start();
    }
}