package com.merdeev.experiment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.URI;

/**
 * Activity для отображения содержания файла,
 * заполняется inflater в зависимости от типа отображаемой информации
 * @author R.Z.Merdeev
 */
public class ViewActivity extends AppCompatActivity {

    /**
     * При создании Activity
     * @param savedInstanceState параметр
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        Trace.save("viewActivity: onCreate");

        try {
            // Nзвлекаются данные, переданные от вызывавшего Activity
            Intent intent = getIntent();
            Ser ser = (Ser) intent.getSerializableExtra("ser");

            // Проверяется, что тип данных - URI
            if (ser.type == URI.class) {
                Trace.save("viewActivity: onCreate: uri");

                // Определяется URI
                URI uri = (URI) ser.resource;

                switch (uri.getPath().substring(uri.getPath().lastIndexOf("."))) {
                    // Проверяется, что тип файла - картика
                    case (".gif"):
                    case (".jpg"):
                    case (".png"):
                        // Показывается картинка
                        showImage(uri);
                        break;
                    // Проверяется, что тип файла - текст
                    case (".txt"):
                        // Показывается текст
                        showText(uri);
                        break;

                    default:
                        Trace.save("viewActivity: onCreate: unknown file type");
                        break;
                }
            }
            // Проверяется, что тип данных - Bitmap
            else if (ser.type == Bitmap.class) {
                Trace.save("viewActivity: onCreate: bitmap");

                // Определяется Bitmap
                Bitmap bitmap = Download.createBitmapFromByteArray((byte[]) ser.resource);

                // Показывается картинка
                showImage(bitmap);
            }
            // Проверяется, что тип данных - String
            else if (ser.type == String.class) {
                Trace.save("viewActivity: onCreate: string");

                // Определяется текст
                String text = Download.createTextFromByteArray((byte[]) ser.resource);

                // Показывается текст
                showText(text);
            }
            else {
                Trace.save("viewActivity: onCreate: unknown resource");
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Trace.save("viewActivity: onCreate: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Trace.save(i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }

    /**
     * Добавляет в layout текущего Activity картинку
     * @param resource ресурс картинки
     * @throws Exception исключение
     */
    private void showImage(Object resource) throws Exception {
        Trace.save("viewActivity: showImage");

        // Создается inflater
        LayoutInflater ltInflater = getLayoutInflater();

        // Находится llView
        LinearLayout ll = (LinearLayout) findViewById(R.id.llView);

        // Разворачивается view для картинки
        View v = ltInflater.inflate(R.layout.view, ll, false);

        // Находится ivView
        ImageView iv = v.findViewById(R.id.ivView);

        // Проверяется, что ресурс - это URI
        if (resource instanceof URI) {
            // Для view передаются данные картинки
            iv.setImageBitmap(Download.createBitmapFromFile((URI) resource));
        }
        // Проверяется, что ресурс - это Bitmap
        else if (resource instanceof Bitmap) {
            // Для view передаются данные картинки
            iv.setImageBitmap((Bitmap) resource);
        }
        else {
            Trace.save("viewActivity: showImage: unknown resource");
            return;
        }


        // View добавляется в layout Activity (отображается)
        ll.addView(v);
    }

    /**
     * Добавляет в layout текущего Activity текст
     * @param resource ресурс текста
     * @throws Exception исключение
     */
    private void showText(Object resource) throws Exception {
        Trace.save("viewActivity: showText");

        // Создается inflater
        LayoutInflater ltInflater = getLayoutInflater();

        // Находится llView
        LinearLayout ll = (LinearLayout) findViewById(R.id.llView);

        // Разворачивается view для текста
        View v = ltInflater.inflate(R.layout.text, ll, false);

        // Находится tvText, ему устанавливается скроллинг
        TextView tv = v.findViewById(R.id.tvText);
        tv.setMovementMethod(new ScrollingMovementMethod());

        // Проверяется, что ресурс - это ссылка
        if (resource instanceof URI) {
            // Для view передаются данные текста
            tv.setText(Download.createTextFromFile((URI) resource));
        }
        // Проверяется, что ресурс - это String
        else if (resource instanceof String) {
            // Для view передаются данные тектса
            tv.setText((String) resource);
        }
        else {
            Trace.save("viewActivity: showText: unknown resource");
            return;
        }

        // View добавляется в layout Activity (отображается)
        ll.addView(v);
    }
}
