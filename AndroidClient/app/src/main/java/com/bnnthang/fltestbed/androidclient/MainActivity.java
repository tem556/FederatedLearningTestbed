package com.bnnthang.fltestbed.androidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bnnthang.fltestbed.clients.BaseClient;
import com.bnnthang.fltestbed.clients.IClientOperations;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
//    FederatedLearningClient client = null;
    ClientThread clientThread = null;

    static {
        System.loadLibrary("opencv_java");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startbutton_onclick(View view) throws Exception {
        System.out.println("starting");
        if (clientThread != null && clientThread.isAlive()) {
            System.out.println("already running");
            return;
        }
        clientThread = new ClientThread(getFilesDir());
        clientThread.start();

//        if (client == null || !client.isRunning()) {
//            // TODO: change hard coded value
//            client = new FederatedLearningClientImpl("10.27.56.229", 4602, getApplicationContext());
//            client.serve();
//        } else {
////            System.out.println("training already running");
//            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "training already running", Snackbar.LENGTH_LONG);
//            snackbar.show();
//        }
    }
}