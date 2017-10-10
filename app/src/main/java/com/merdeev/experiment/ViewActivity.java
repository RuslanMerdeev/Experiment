package com.merdeev.experiment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * Activity для отображения содержания файла,
 * заполняется inflater в зависимости от типа отображаемой информации
 * @author R.Z.Merdeev
 */
public class ViewActivity extends AppCompatActivity {

    /**
     * При создании Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // Определяется место хранения файла, переданное от вызывавшего Activity
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");

        // Создается inflater
        LayoutInflater ltInflater = getLayoutInflater();

        // Находится llView
        LinearLayout ll = (LinearLayout) findViewById(R.id.llView);

        try {
            // Проверяется, что тип файла - картика
            if (path.contains(".gif")) {
                // Создается bitmap из файла
                Bitmap bm = BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(Download.createFile(path))));

                // Создается uri из файла (альтернатива bitmap, но растягивает изображение)
//                Uri uri = Uri.fromFile( Download.createFile(path) );

                // Создается и разворачивается view для картинки
                View v = ltInflater.inflate(R.layout.view, ll, false);

                // Для view передаются данные картинки
                ImageView iv = v.findViewById(R.id.ivView);
                iv.setImageBitmap(bm);
//                iv.setImageURI(uri);

                // View добавляется в layout Activity (отображается)
                ll.addView(v);
            }
            // Проверяется, что тип файла - текст
            else if (path.contains(".txt")) {
//                Bitmap bm = BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(Download.createFile(path))));
//                View v = ltInflater.inflate(R.layout.view, ll, false);
//                ImageView iv = v.findViewById(R.id.ivView);
////                Log.d(MainActivity.LOG, iv.toString());
//                iv.setImageBitmap(bm);
//                ll.addView(v);
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Log.d(MainActivity.LOG, "requestList: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }
}
