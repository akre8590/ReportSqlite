package com.diegocasas.reportsqlite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.itextpdf.text.DocumentException;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST_STORAGE = 1;
    Button pdf;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_STORAGE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST_STORAGE);
            }
        }else {
            //no hace nada
        }

        final DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
        pdf = (Button)findViewById(R.id.pdfReport);
        pdf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    databaseHelper.generateReportPDF();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "No se pudo crear el reporte", Toast.LENGTH_SHORT).show();
                } catch (DocumentException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "No se pudo crear el reporte", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(MainActivity.this, ViewPDF.class);
                intent.putExtra("path", "/storage/emulated/0/PDF/Report.pdf");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent);
            }
        });
    }
}
