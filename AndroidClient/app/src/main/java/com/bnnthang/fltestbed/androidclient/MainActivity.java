package com.bnnthang.fltestbed.androidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startbutton_onclick(View view) throws Exception {
        ClientThread clientThread = new ClientThread(getFilesDir());
        clientThread.start();
    }
}