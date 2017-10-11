package com.merdeev.experiment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Основной начальный Activity,
 * имеет кнопку для запроса структуры директории и текстовое поле для отображения полезной информации
 * @author R.Z.Merdeev
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, CompleteListener {

    /** Строка-константа для логов */
    final static String LOG = "States";

    /** Nдентификатор диалога для списка имен файлов/папок текущей директории */
    private final int DIALOG_LIST = 1;

    /** Текстовое поле для отображения полезной информации */
    private TextView tvContent;

    /** Список данных о файлах/папках текущей директории */
    private ArrayList<Map<String, String>> list;

    /** Смещение для текущей директории относительно корневой */
    private String offset = "";

    /** Данные из ресурсов */
    private String list_title;
    private String resource;
    private String reference;
    private String app_name;
    private boolean save_file;

    /**
     * При создании основного Activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG, "mainActivity: onCreate");

        // Находится tvContent, ему устанавливается скроллинг
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setMovementMethod(new ScrollingMovementMethod());

        // Находится btnList, ему устанавливается onClickListener - текущий Activity
        Button btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(this);

        // Забираются из ресурсов некоторые String для отображения и адресов
        list_title = getResources().getString(R.string.list_title);
        resource = getResources().getString(R.string.resource);
        reference = getResources().getString(R.string.reference);
        app_name = getResources().getString(R.string.app_name);
        save_file = getResources().getBoolean(R.bool.save_file);

        // Nнициируется начальный запрос структуры корневой директории
        doRequestList();
    }

    /**
     * При нажатии кнопок
     * @param view источник вызова
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Проверяется, что нажималась кнопка btnList
            case R.id.btnList:
                Log.d(LOG, "btnList: onClick");

                // Стирается смещение, текущая директория - корневая
                offset = "";

                // Nнициируется запрос структуры корневой директории
                doRequestList();
                break;
        }
    }

    /**
     * При выборе пункта диалога или нажатии кнопки диалога
     * @param dialogInterface диалог
     * @param i выбранный пункт
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            // Проверяется, что нажималась нейтральная кнопка
            case Dialog.BUTTON_NEUTRAL:
                break;

            // Проверяется, что нажималась негативная кнопка
            case Dialog.BUTTON_NEGATIVE:
                // Проверяется есть ли смещение относительно корневой директории
                if(offset.contains("/")) {
                    // Удаляется последний шаг смещения, т.е. шаг назад по директории
                    offset = offset.substring(0, offset.lastIndexOf("/"));

                    // Nнициируется запрос структуры текущей директории
                    doRequestList();
                }
                break;

            // Проверяется, что выбирался пункт списка
            default:
                // Забираются данные о выбранном пункте
                HashMap<String,String> map = (HashMap<String,String>)list.get(i);

                // Добавляется шаг смещения по имени папки
                String elem = "/" + map.get("name");
                offset = offset + elem;

                // Проверяется, что тип выбранного пункта - файл
                if (map.get("type").equals("file")) {
                    // инициируется загрузка файла
                    doDownload(map);
                }
                // Тип выбранного пункта - папка
                else {
                    // Nнициируется запрос структуры текущей директории
                    doRequestList();
                }
                break;
        }
    }

    /**
     * Nнициирует запрос структуры текущей директории облака {@link RequestList#RequestList(CompleteListener, String, String, String)}
     */
    private void doRequestList() {
        new RequestList(this, resource, reference, offset);
    }

    /**
     * Формирует адрес загрузки и
     * инициирует загрузку файла {@link Download#Download(CompleteListener, String, String, boolean)}
     */
    private void doDownload(HashMap<String,String> map) {
        String address = "https://" + map.get("address") + ".datacloudmail.ru/weblink/view/" + reference + offset + "?etag=" + map.get("hash") + "&key=" + map.get("token");
        new Download(this, address, app_name, save_file);
    }

    /**
     * Определяет источник вызова и
     * обрабатывает данные {@link CompleteListener}
     * @param o источник вызова, объект класса
     * @param res результат, произвольные данные
     * @throws Exception
     */
    @Override
    public void complete(Object o, Object res) throws Exception {
        // Проверяется наличие результата
        if (res == null) throw new NullPointerException("complete: res: null");

        if (o instanceof RequestList) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: RequestList");

            // Преобразуется тип результата к списку
            list = (ArrayList<Map<String, String>>) res;

            // Вызывается диалог
            showDialog(DIALOG_LIST);
        } else if (o instanceof Download) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: Download");
            if (res instanceof String) {
                //todo временно
                String text = (String) res;
                tvContent.setText(text);

                // Отображается содержание файла
                showContent(new Ser(res, "text"));
            }
            else if (res instanceof Bitmap) {
                // Отображается содержание файла
                showContent(new Ser(res, "bitmap"));
            }
            else if (res instanceof URI) {
                // Отображается содержание файла
                showContent(new Ser(res, "uri"));
            }
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
            // Проверяется, что нужно создать диалог именно для списка файлов/папок текущей директории облака
            case DIALOG_LIST:
                // Создается builder для диалога
                AlertDialog.Builder adb = new AlertDialog.Builder(this);

                // Устанавливается заголовок списка + смещение для информирования
                adb.setTitle(list_title + offset);

                // Формируется список имен
                ArrayList<String> names = getNames(list);

                // Создается адаптер для списка
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, names);

                // Builder-ом устанавливается адаптер, нейтральная и негативная кнопки для диалога
                // Выбор пункта обрабатывается текущим Activity
                adb.setAdapter(adapter, this);
                adb.setNeutralButton(R.string.cancel, this);
                adb.setNegativeButton(R.string.back, this);

                // Создание и возврат диалога
                return adb.create();
        }
        return super.onCreateDialog(id);
    }

    /**
     * Подготавливает диалог для текущего Activity
     * @param id идентификатор подготавливаемого диалога
     * @param dialog подготавливаемый диалог
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        switch (id) {
            // Проверяется, что нужно подготовить диалог именно для списка файлов/папок текущей директории облака
            case DIALOG_LIST:
                // Преобразуется тип диалога к AlertDialog
                AlertDialog aDialog = (AlertDialog) dialog;

                // Обновляется заголовок списка
                aDialog.setTitle(getResources().getString(R.string.list_title) + offset);

                // Формируется список имен
                ArrayList<String> names = getNames(list);

                // Создается адаптер для списка
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, names);

                // Устанавливается адаптер
                aDialog.getListView().setAdapter(adapter);
                break;
        }
    }

    /**
     * Формирует список имен из списка данных
     * @param list список данных файлов/папок директории
     * @return список имен
     */
    private ArrayList<String> getNames(ArrayList<Map<String, String>> list) {
        // Создается список имен
        ArrayList<String> names = new ArrayList<>();

        // Для всех элементов списка данных
        for (Map<String, String> i : list) {
            // Получается имя файла/папки и добавляется в список
            names.add((i).get("name"));
        }
        return names;
    }

    /**
     * Nнициирует отображение информации в отдельном Activity {@link ViewActivity}
     * @param ser данные
     * @throws Exception
     */
    private void showContent(Ser ser) throws Exception {
        // Создается intent
        Intent intent = new Intent(this, ViewActivity.class);

        // Сохраняются данные в intent для передачи создаваемому Activity
        intent.putExtra("ser", ser);

        // Запускается Activity
        startActivity(intent);
    }
}
