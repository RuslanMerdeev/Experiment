package com.merdeev.experiment;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
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
public class MainActivity extends Context implements View.OnClickListener {

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

        Trace.save("mainActivity: onCreate");

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
        Dialog.showList(this, list_title + offset, getNames(list));
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
                Trace.save("mainActivity: onClick: btnList");

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
        // Запуск диалога прогресса
        Dialog.showProgress(this);

        // Запуск запроса
        new RequestList(this, resource, reference, offset);
    }

    /**
     * Создает диалог для текущего Activity
     * @param id идентификатор создаваемого диалога
     * @return созданный диалог
     */
    @Override
    protected android.app.Dialog onCreateDialog(int id) {
        if (Dialog.getDialog() == null) {
            Trace.save("mainActivity: onCreateDialog: dialog: null");
            Dialog.showError(this);
            return super.onCreateDialog(id);
        }

        return Dialog.getDialog().onCreateDialog(id);
    }

    /**
     * Определяет источник вызова и
     * обрабатывает данные {@link CompleteListener}
     * @param cc источник вызова, объект класса
     * @param result результат, произвольные данные
     * @param type тип данных
     */
    @Override
    public void complete(Object cc, Object result, Class type) {
        Trace.save("mainActivity: complete");

        try {
            // Проверяется, что источника вызова нет
            if (cc == null) {
                Trace.save("mainActivity: complete: сс: null");
                Dialog.showError(this);
                return;
            }

            // Проверяется, что результата нет
            if (result == null) {
                Trace.save("mainActivity: complete: result: null");
                Dialog.showError(this);
                return;
            }

            // Проверяется, что типа результата нет
            if (type == null) {
                Trace.save("mainActivity: complete: type: null");
                Dialog.showError(this);
                return;
            }

            // Проверяется, что источник - объект класса RequestList
            if (cc instanceof RequestList) {
                Trace.save("mainActivity: complete: RequestList");

                Dialog.finishProgress(this);

                // Проверяется, что тип результата список
                if (type == ArrayList.class) {
                    // Результат преобразуется к типу список данных
                    list = (ArrayList<Map<String, String>>) result;

                    // Отображается список
                    Dialog.showList(this, list_title + offset, getNames(list));
                } else {
                    Trace.save("mainActivity: complete: RequestList: unknown type");
                    Dialog.showError(this);
                }
            }
            // Проверяется, что источник - объект класса Download
            else if (cc instanceof Download) {
                Trace.save("mainActivity: complete: Download");

                Dialog.finishProgress(this);

                // Проверяется, что загружался файл с признаком содержания ссылки
                if (ref) {
                    // Проверяется, что тип результата текст
                    if (type == String.class) {
                        // Определяется сегодняшняя ссылка
                        reference = Download.createTextFromByteArray((byte[]) result);
                    }
                    // Проверяется, что тип результата ссылка на файл
                    else if (type == URI.class) {
                        //todo временно для отображения места хранения
                        tvContent.setText(((URI) result).getPath());

                        // Определяется сегодняшняя ссылка
                        reference = Download.createTextFromFile((URI) result);
                    } else {
                        Trace.save("mainActivity: complete: Download: unknown type");
                        Dialog.showError(this);
                        return;
                    }

                    // Nнициируется запрос структуры корневой директории
                    offset = "";
                    doRequestList();
                } else {
                    // Отображается содержание файла
                    showContent(new Ser(result, type));
                }
            }
            // Проверяется, что источник - объект класса Dialog
            else if (cc instanceof Dialog) {
                Trace.save("mainActivity: complete: Dialog");

                // Проверяется, что тип результата массив идентификаторов
                if (type == int[].class) {
                    int dialog = ((int[])result)[0];
                    int button = ((int[])result)[1];
                    if (dialog == Dialog.DIALOG_LIST) {
                        Trace.save("mainActivity: complete: Dialog: list");

                        switch (button) {
                            // Проверяется, что нажималась нейтральная кнопка
                            case android.app.Dialog.BUTTON_NEUTRAL:
                                Trace.save("mainActivity: complete: Dialog: list: neutral");
                                break;

                            // Проверяется, что нажималась негативная кнопка
                            case android.app.Dialog.BUTTON_NEGATIVE:
                                Trace.save("mainActivity: complete: Dialog: list: negative");

                                // Проверяется есть ли смещение относительно корневой директории
                                if (offset.contains("/")) {
                                    // Удаляется последний шаг смещения, т.е. шаг назад по директории
                                    offset = offset.substring(0, offset.lastIndexOf("/"));

                                    // Nнициируется запрос структуры текущей директории
                                    doRequestList();
                                }
                                break;

                            // Проверяется, что выбирался пункт списка
                            default:
                                if (button >= 0) {
                                    Trace.save("mainActivity: complete: Dialog: list: item: " + button);

                                    // Забираются данные о выбранном пункте
                                    HashMap<String, String> map = (HashMap<String, String>) list.get(button);

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
                                else {
                                    Trace.save("mainActivity: complete: Dialog: list: unknown button");
                                    Dialog.showError(this);
                                }
                        }
                    }
                    else if (dialog == Dialog.DIALOG_ERROR){
                        Trace.save("mainActivity: complete: Dialog: error");

                        switch (button) {
                            // Проверяется, что нажималась позитивная кнопка
                            case android.app.Dialog.BUTTON_POSITIVE:
                                Trace.save("mainActivity: complete: Dialog: error: positive");
                                finish();
                                break;

                            // Проверяется, что произошел cancel
                            case Dialog.BUTTON_CANCEL:
                                Trace.save("mainActivity: complete: Dialog: error: cancel");
                                finish();
                                break;

                            default:
                                Trace.save("mainActivity: complete: Dialog: error: unknown button");
                                Dialog.showError(this);
                                break;
                        }
                    }
                    else if (dialog == Dialog.DIALOG_PROGRESS) {
                        Trace.save("mainActivity: complete: Dialog: progress");

                        switch (button) {
                            // Проверяется, что произошел cancel
                            case Dialog.BUTTON_CANCEL:
                                Trace.save("mainActivity: complete: Dialog: progress: cancel");
                                break;

                            default:
                                Trace.save("mainActivity: complete: Dialog: progress: unknown button");
                                Dialog.showError(this);
                                break;
                        }
                    }
                    else {
                        Trace.save("mainActivity: complete: Dialog: unknown dialog");
                        Dialog.showError(this);
                    }
                } else {
                    Trace.save("mainActivity: complete: Dialog: unknown type");
                    Dialog.showError(this);
                }
            } else {
                Trace.save("mainActivity: complete: unknown cc");
                Dialog.showError(this);
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Trace.save("mainActivity: complete: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Trace.save(i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
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
        // Запуск диалога прогресса
        Dialog.showProgress(this);

        // Запуск загрузки
        new Download(this, createURLText(map, reference, offset), app_name, save_file);
    }

    /**
     * Nнициирует отображение информации в отдельном Activity {@link ViewActivity}
     * @param ser данные
     * @throws Exception исключение
     */
    private void showContent(Ser ser) throws Exception {
        Trace.save("mainActivity: showContent");

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
        Trace.save("mainActivity: getNames");

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
