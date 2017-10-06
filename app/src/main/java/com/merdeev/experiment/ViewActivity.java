package com.merdeev.experiment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class ViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");


        LayoutInflater ltInflater = getLayoutInflater();
        LinearLayout ll = (LinearLayout) findViewById(R.id.llView);


        try {
            if (path.contains(".gif")) {
                Bitmap bm = BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(Download.createFile(path))));
                View v = ltInflater.inflate(R.layout.view, ll, false);
                ImageView iv = v.findViewById(R.id.ivView);
                iv.setImageBitmap(bm);
                ll.addView(v);
            }
            else if (path.contains(".txt")) {
//                Bitmap bm = BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(Download.createFile(path))));
//                View v = ltInflater.inflate(R.layout.view, ll, false);
//                ImageView iv = v.findViewById(R.id.ivView);
////                Log.d(MainActivity.LOG, iv.toString());
//                iv.setImageBitmap(bm);
//                ll.addView(v);
            }
        }
        catch (Exception e) {
            Log.d(MainActivity.LOG, "requestList: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }
}
