package com.merdeev.experiment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

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

        Log.d(MainActivity.LOG, "viewActivity: onCreate");

        // Nзвлекаются данные, переданные от вызывавшего Activity
        Intent intent = getIntent();
        Ser ser = (Ser)intent.getSerializableExtra("ser");

        // Проверяется, что тип данных - URI
        if (ser.type.equals("uri")) {
            // Определяется путь
            String path = ((URI)ser.res).getPath();
            // Создается inflater
            LayoutInflater ltInflater = getLayoutInflater();

            // Находится llView
            LinearLayout ll = (LinearLayout) findViewById(R.id.llView);

            try {
                // Определяется view для отображения
                View v;

                // Проверяется, что тип файла - картика
                if (path.contains(".gif") || path.contains(".jpg") || path.contains(".png")) {
                    // Разворачивается view для картинки
                    v = ltInflater.inflate(R.layout.view, ll, false);

                    // Для view передаются данные картинки
                    ImageView iv = v.findViewById(R.id.ivView);
                    iv.setImageBitmap(Download.createBitmapFromFile(path));

                    // Создается uri из файла (альтернатива bitmap, но растягивает изображение)
//                Uri uri = Uri.fromFile( Download.createFile(path) );
//                iv.setImageURI(uri);
                }
                // Проверяется, что тип файла - текст
                else if (path.contains(".txt")) {
                    // Разворачивается view для текста
                    v = ltInflater.inflate(R.layout.text, ll, false);

                    // Для view передается текст
                    TextView tv = v.findViewById(R.id.tvText);
                    tv.setMovementMethod(new ScrollingMovementMethod());
                    tv.setText(Download.createTextFromFile(path));
                } else {
                    Log.d(MainActivity.LOG, "viewActivity: onCreate: unknown file type");
                    return;
                }

                // View добавляется в layout Activity (отображается)
                ll.addView(v);
            }
            // Выводится трейс для исключения
            catch (Exception e) {
                Log.d(MainActivity.LOG, "viewActivity: onCreate: " + e.getClass() + ": " + e.getMessage());
                StackTraceElement[] el = e.getStackTrace();
                for (StackTraceElement i : el) {
                    Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
                }
            }
        }
    }
}
