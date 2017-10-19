package com.merdeev.experiment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Начальный Activity,
 * имеет иконку приложения
 * @author R.Z.Merdeev
 */
public class SplashActivity extends AppCompatActivity implements CompleteListener, DialogInterface.OnClickListener {

    /** Nдентификатор диалога ошибки */
    private final int DIALOG_ERROR = 1;

    /** Данные из ресурсов */
    private String resource;
    private String reference;
    private String app_name;

    /** Сегодняшняя ссылка */
    private String reference_today;

    /** Признак запроса сегодняшней ссылки */
    private boolean today;

    /**
     * При создании начального Activity
     * @param savedInstanceState параметр
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Забираются из ресурсов некоторые String для отображения и адресов
        resource = getResources().getString(R.string.resource);
        reference = getResources().getString(R.string.reference);
        app_name = getResources().getString(R.string.app_name);

        // Создается трейсер
        new Trace(app_name, "log.txt");

        Trace.save("splashActivity: onCreate");

        // Nнициируется запрос сегодняшней ссылки
        today = true;
        new RequestList(this, resource, reference, "");

    }

    /**
     * Определяет источник вызова и
     * обрабатывает данные {@link CompleteListener}
     * @param cc источник вызова, объект класса
     * @param result результат, произвольные данные
     * @param type тип данных
     * @throws Exception исключение
     */
    @Override
    public void complete(Object cc, Object result, Class type) throws Exception {
        Trace.save("splashActivity: complete");

        // Проверяется, что источника вызова нет
        if (cc == null) {
            Trace.save("splashActivity: complete: сс: null");
            showNewDialog(DIALOG_ERROR);
            return;
        }

        // Проверяется, что результата нет
        if (result == null) {
            Trace.save("splashActivity: complete: result: null");
            showNewDialog(DIALOG_ERROR);
            return;
        }

        // Проверяется, что типа результата нет
        if (type == null) {
            Trace.save("splashActivity: complete: type: null");
            showNewDialog(DIALOG_ERROR);
            return;
        }

        // Проверяется, что источник - объект класса RequestList
        if (cc instanceof RequestList) {
            Trace.save("splashActivity: complete: RequestList");

            // Проверяется, что тип результата список
            if (type == ArrayList.class) {
                // Проверяется, что запрашивалась сегодняшняя ссылка
                if (today) {
                    // Nнициируется загрузка последнего файла
                    ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) result;
                    HashMap<String, String> map = (HashMap<String, String>) list.get(list.size() - 1);
                    new Download(this, MainActivity.createURLText(map, reference, "/" + map.get("name")), app_name, false);
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
                Trace.save("splashActivity: complete: unknown type");
                showNewDialog(DIALOG_ERROR);
            }
        }
        // Проверяется, что источник - объект класса Download
        else if (cc instanceof Download) {
            Trace.save("splashActivity: complete: Download");

            // Проверяется, что тип результата текст
            if (type == String.class) {
                // Определяется сегодняшняя ссылка
                reference_today = Download.createTextFromByteArray((byte[]) result);

                // Nнициируется начальный запрос структуры корневой директории
                today = false;
                new RequestList(this, resource, reference_today, "");
            }
            else {
                Trace.save("splashActivity: complete: unknown type");
                showNewDialog(DIALOG_ERROR);
            }
        }
        else {
            Trace.save("splashActivity: complete: unknown cc");
            showNewDialog(DIALOG_ERROR);
        }
    }

    /**
     * Создает диалог для текущего Activity
     * @param id идентификатор создаваемого диалога
     * @return созданный диалог
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            // Проверяется, что нужно создать диалог ошибки
            case DIALOG_ERROR:
                Trace.save("splashActivity: onCreateDialog: dialog_error");

                // Создается builder для диалога
                AlertDialog.Builder adb = new AlertDialog.Builder(this);

                // Устанавливается сообщение ошибки
                adb.setMessage("Что-то пошло не так :(");

                // Устанавливается кнопка OK
                adb.setPositiveButton(R.string.ok, this);

                // Устанавливается запрет на выход из диалога по кнопке назад
                adb.setCancelable(false);

                // Создание и возврат диалога
                return adb.create();
        }
        return super.onCreateDialog(id);
    }

    /**
     * При нажатии кнопки диалога
     * @param dialogInterface диалог
     * @param i выбранный пункт
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        Trace.save("splashActivity: onClick");

        finish();
    }

    /**
     * Создает заново диалог
     * @param i идентификатор диалога
     */
    private void showNewDialog(int i) {
        Trace.save("splashActivity: showNewDialog");

        removeDialog(i);
        showDialog(i);
    }
}
