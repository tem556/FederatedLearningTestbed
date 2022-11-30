package com.bnnthang.fltestbed.androidclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startbutton_onclick(View view) {
        String host = ((EditText) findViewById(R.id.hostEditText)).getText().toString();
        int port = Integer.parseInt(((EditText) findViewById(R.id.portEditText)).getText().toString());

        RadioGroup radioButtonGroup = (RadioGroup) findViewById(R.id.datasetRadioGroup);
        int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        View radioButton = radioButtonGroup.findViewById(radioButtonID);
        int idx = radioButtonGroup.indexOfChild(radioButton);

        ClientThread clientThread = new ClientThread(idx, getFilesDir(), host, port);
        clientThread.start();
    }
}