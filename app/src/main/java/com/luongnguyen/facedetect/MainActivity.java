package com.luongnguyen.facedetect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.luongnguyen.facedetect.Methods.AttendanceList;
import static com.luongnguyen.facedetect.Methods.GALLERY_FOLDER;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = ".ActivityMain";
    Button RecognizeButton, InputFaceButton, ListViewButton, TrainButton;
    FloatingActionButton EmailButton;
    private boolean GotPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int PermissionCam = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int PermissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int PermissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        // Layout items
        RecognizeButton = findViewById(R.id.RecognizeButton);
        InputFaceButton = findViewById(R.id.InputFaceButton);
        ListViewButton = findViewById(R.id.ListViewButton);
        TrainButton = findViewById(R.id.TrainButton);
        EmailButton = findViewById(R.id.EmailButton);

        // Set onClick action
        RecognizeButton.setOnClickListener(this);
        InputFaceButton.setOnClickListener(this);
        ListViewButton.setOnClickListener(this);
        TrainButton.setOnClickListener(this);
        EmailButton.setOnClickListener(this);

        // Request permission if not granted
        GotPermission = PermissionCam == PackageManager.PERMISSION_GRANTED
                && PermissionWrite == PackageManager.PERMISSION_GRANTED
                && PermissionRead == PackageManager.PERMISSION_GRANTED;
        if (!GotPermission) {
            requestPermission();
        }
        // start reading from Classlist.csv file
        Log.d(TAG,"start initializing Classlist");
        Methods.InitializeClassList(this);
        Log.d(TAG,"start initializing Gallery with some sample pics");
        Methods.InitializeGallery(this);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();
        perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_DENIED);
        perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_DENIED);
        perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_DENIED);
        for (int i = 0; i < permissions.length; i++) {
            perms.put(permissions[i], grantResults[i]);
        }
        if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            GotPermission = true;
        } else {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this)
                        .setMessage("Please grant Camera and Storage permission for this application")
                        .setPositiveButton("Dismiss all", null)
                        .show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.InputFaceButton:
                Intent InputAct = new Intent(MainActivity.this, InputFace.class);
                startActivity(InputAct);
                break;

            case R.id.RecognizeButton:
                Intent RecogAct = new Intent(MainActivity.this, Recognizer.class);
                startActivity(RecogAct);
                break;
            case R.id.ListViewButton:
                Intent ListAct = new Intent(MainActivity.this, List_View.class);
                startActivity(ListAct);
                break;
            case R.id.TrainButton:
                Methods.train(this);
                break;
            case R.id.EmailButton:
                Methods.Printest();
                Methods.WriteCSV(AttendanceList,this);
                Methods.EmailCSV(this);
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}//end of main