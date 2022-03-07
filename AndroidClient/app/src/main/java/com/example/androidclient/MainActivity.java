package com.example.androidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {
    FederatedLearningClient client = null;

    static {
        System.loadLibrary("opencv_java");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        client = null;
    }

    public void startbutton_onclick(View view) throws IOException, URISyntaxException {
        System.out.println("starting");

        if (client == null || !client.isRunning()) {
            // TODO: change hard coded value
            client = new FederatedLearningClientImpl("172.20.102.177", 4602, getApplicationContext());
            client.serve();
        } else {
//            System.out.println("training already running");
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "training already running", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}