package com.merdeev.experiment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Основной Activity,
 * имеет кнопку для запроса структуры директории и текстовое поле для отображения полезной информации
 * @author R.Z.Merdeev
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, DialogInterface.OnClickListener, CompleteListener {

    /** Строка-константа для логов */
    final static String LOG = "States";

    /** Nдентификатор диалога для списка имен файлов/папок текущей директории */
    private final int DIALOG_LIST = 1;

    /** Nдентификатор диалога прогресса */
    private final int DIALOG_PROGRESS = 2;

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

    /** Признак загрузки файла, содержащего ссылку */
    private boolean ref;

    /**
     * При создании основного Activity
     * @param savedInstanceState параметр
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(LOG, "mainActivity: onCreate");

        // Nзвлекаются данные, переданные от вызывавшего Activity
        Intent intent = getIntent();
        Ser ser = (Ser) intent.getSerializableExtra("ser");
        reference = intent.getStringExtra("reference");

        // Результат преобразуется к типу список данных
        list = (ArrayList<Map<String, String>>) ser.resource;

        // Находится tvContent, ему устанавливается скроллинг
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setMovementMethod(new ScrollingMovementMethod());

        // Находится btnList, ему устанавливается onClickListener - текущий Activity
        Button btnList = (Button) findViewById(R.id.btnList);
        btnList.setOnClickListener(this);

        // Забираются из ресурсов некоторые String для отображения и адресов
        list_title = getResources().getString(R.string.list_title);
        resource = getResources().getString(R.string.resource);
        app_name = getResources().getString(R.string.app_name);
        save_file = getResources().getBoolean(R.bool.save_file);

        // Отображается список
        showList(DIALOG_LIST);
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
     * Nнициирует запрос структуры текущей директории облака {@link RequestList#RequestList(CompleteListener, String, String, String)}
     */
    private void doRequestList() {
        showDialog(DIALOG_PROGRESS);
        new RequestList(this, resource, reference, offset);
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

            case DIALOG_PROGRESS:
                // Создается диалог прогресса
                ProgressDialog pd = new ProgressDialog(this);

                // Устанавливается заголовок списка + смещение для информирования
                pd.setMessage("Загрузка...");

                // Отображается диалог
                pd.show();

                return pd;
        }
        return super.onCreateDialog(id);
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
                String name = map.get("name");
                offset = offset + "/" + name;

                // Проверяется, что тип выбранного пункта - файл
                if (map.get("type").equals("file")) {
                    // Проверяется, что имя файла имеет признак содержания ссылки
                    ref = (name.equals("prev.txt") || name.equals("next.txt"));

                    // Nнициируется загрузка файла
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
     * Определяет источник вызова и
     * обрабатывает данные {@link CompleteListener}
     * @param cc источник вызова, объект класса
     * @param result результат, произвольные данные
     * @param type тип данных
     * @throws Exception исключение
     */
    @Override
    public void complete(Object cc, Object result, Class type) throws Exception {
        removeDialog(DIALOG_PROGRESS);

        // Проверяется, что источник вызова есть
        if (cc == null) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: сс: null");
            return;
        }

        // Проверяется, что результат есть
        if (result == null) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: result: null");
            return;
        }

        // Проверяется, что источник - объект класса RequestList
        if (cc instanceof RequestList) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: RequestList");

            // Результат преобразуется к типу список данных
            list = (ArrayList<Map<String, String>>) result;

            // Отображается список
            showList(DIALOG_LIST);
        }
        // Проверяется, что источник - объект класса Download
        else if (cc instanceof Download) {
            Log.d(LOG, "mainActivity: asCompleteListener: complete: Download");

            // Проверяется, что тип результата есть
            if (type == null) {
                Log.d(LOG, "mainActivity: asCompleteListener: complete: type: null");
                return;
            }

            //todo временно
            if (type == URI.class) {
                String text = ((URI) result).getPath();
                tvContent.setText(text);
            }

            // Проверяется, что загружался файл с признаком содержания ссылки
            if (ref) {
                // Проверяется, что тип результата текст
                if (type == String.class) {
                    // Определяется сегодняшняя ссылка
                    reference = Download.createTextFromByteArray((byte[]) result);
                }
                // Проверяется, что тип результата ссылка на файл
                else if (type == URI.class) {
                    // Определяется сегодняшняя ссылка
                    reference = Download.createTextFromFile((URI) result);
                }
                else Log.d(LOG, "mainActivity: asCompleteListener: complete: unknown type");

                // Nнициируется запрос структуры корневой директории
                offset = "";
                new RequestList(this, resource, reference, offset);
            }
            else {
                // Отображается содержание файла
                showContent(new Ser(result, type));
            }
        }
        else Log.d(LOG, "mainActivity: asCompleteListener: complete: unknown cc");
    }

    /**
     * Формирует адрес загрузки и
     * @param map данные для скачивания
     * @param reference ссылка на корневую директорию
     * @param offset смещение относительно корневой директории
     * @return текст URL
     */
    static String createURLText(HashMap<String,String> map, String reference, String offset) {
        return "https://" + map.get("address") + ".datacloudmail.ru/weblink/view/" + reference + offset + "?etag=" + map.get("hash") + "&key=" + map.get("token");
    }

    /**
     * Nнициирует загрузку файла {@link Download#Download(CompleteListener, String, String, boolean)}
     * @param map данные для скачивания
     */
    private void doDownload(HashMap<String,String> map) {
        showDialog(DIALOG_PROGRESS);
        new Download(this, createURLText(map, reference, offset), app_name, save_file);
    }

    /**
     * Создает заново диалог для отображения списка
     * @param i идентификатор диалога
     */
    private void showList(int i) {
        removeDialog(i);
        showDialog(i);
    }

    /**
     * Nнициирует отображение информации в отдельном Activity {@link ViewActivity}
     * @param ser данные
     * @throws Exception исключение
     */
    private void showContent(Ser ser) throws Exception {
        // Создается intent
        Intent intent = new Intent(this, ViewActivity.class);

        // Сохраняются данные в intent для передачи создаваемому Activity
        intent.putExtra("ser", ser);

        // Запускается Activity
        startActivity(intent);
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
            names.add(i.get("name"));
        }
        return names;
    }
}
