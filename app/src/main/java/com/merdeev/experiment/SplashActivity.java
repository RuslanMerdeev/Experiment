package com.merdeev.experiment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Map;

/**
 * Начальный Activity,
 * имеет иконку приложения
 * @author R.Z.Merdeev
 */
public class SplashActivity extends AppCompatActivity implements CompleteListener, DialogInterface.OnClickListener {

    /** Nдентификатор диалога для сообщения ошибки */
    private final int DIALOG_ERROR = 1;

    /** Данные из ресурсов */
    private String resource;
    private String reference;

    /** Сегодняшняя ссылка */
    private String reference_today;

    /** Признак запроса сегодняшней ссылки */
    private boolean today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(MainActivity.LOG, "splashActivity: onCreate");

        // Забираются из ресурсов некоторые String для отображения и адресов
        resource = getResources().getString(R.string.resource);
        reference = getResources().getString(R.string.reference);

        // Nнициируется запрос сегодняшней ссылки
        today = true;
        new RequestList(this, resource, reference, "");
    }

    @Override
    public void complete(Object cc, Object result, Class type) throws Exception {
        // Проверяется, что источник вызова есть
        if (cc == null) {
            Log.d(MainActivity.LOG, "splashActivity: asCompleteListener: complete: сс: null");
            showDialog(DIALOG_ERROR);
            return;
        }

        // Проверяется, что результат есть
        if (result == null) {
            Log.d(MainActivity.LOG, "splashActivity: asCompleteListener: complete: result: null");
            showDialog(DIALOG_ERROR);
            return;
        }

        // Проверяется, что источник - объект класса RequestList
        if (cc instanceof RequestList) {
            Log.d(MainActivity.LOG, "splashActivity: asCompleteListener: complete: RequestList");

            // Проверяется, что запрашивалась сегодняшняя ссылка
            if (today) {
                ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) result;
                reference_today = list.get(0).get("name");
                reference_today = reference_today.replaceAll("_", "/");

                // Nнициируется начальный запрос структуры корневой директории
                today = false;
                new RequestList(this, resource, reference_today, "");
            }
            // Проверяется, что запрашивалась структура корневой директории
            else {
                // Создается intent
                Intent intent = new Intent(this, MainActivity.class);

                // Сохраняются данные в intent для передачи создаваемому Activity
                intent.putExtra("ser", new Ser(result, type));
                intent.putExtra("reference", reference_today);

                // Запускается MainActivity
                startActivity(intent);

                // Останавливается этот Activity
                finish();
            }
        }
        else {
            Log.d(MainActivity.LOG, "splashActivity: asCompleteListener: complete: unknown cc");
            showDialog(DIALOG_ERROR);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            // Проверяется, что нужно создать диалог именно для списка файлов/папок текущей директории облака
            case DIALOG_ERROR:
                // Создается builder для диалога
                AlertDialog.Builder adb = new AlertDialog.Builder(this);

                // Устанавливается заголовок списка + смещение для информирования
                adb.setMessage("Что-то пошло не так :(");

                adb.setPositiveButton(R.string.ok, this);

                // Создание и возврат диалога
                return adb.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        finish();
    }
}
