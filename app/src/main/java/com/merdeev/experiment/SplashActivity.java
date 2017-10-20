package com.merdeev.experiment;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Начальный Activity,
 * имеет иконку приложения
 * @author R.Z.Merdeev
 */
public class SplashActivity extends Context {

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
     */
    @Override
    public void complete(Object cc, Object result, Class type) {
        Trace.save("splashActivity: complete");

        try {
            // Проверяется, что источника вызова нет
            if (cc == null) {
                Trace.save("splashActivity: complete: сс: null");
                Dialog.showError(this);
                return;
            }

            // Проверяется, что результата нет
            if (result == null) {
                Trace.save("splashActivity: complete: result: null");
                Dialog.showError(this);
                return;
            }

            // Проверяется, что типа результата нет
            if (type == null) {
                Trace.save("splashActivity: complete: type: null");
                Dialog.showError(this);
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
                } else {
                    Trace.save("splashActivity: complete: unknown type");
                    Dialog.showError(this);
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
                } else {
                    Trace.save("splashActivity: complete: unknown type");
                    Dialog.showError(this);
                }
            }
            // Проверяется, что источник - объект класса Dialog
            else if (cc instanceof Dialog) {
                Trace.save("splashActivity: complete: Dialog");

                // Проверяется, что тип результата массив идентификаторов
                if (type == int[].class) {
                    int dialog = ((int[]) result)[0];
                    int button = ((int[]) result)[1];
                    if (dialog == Dialog.DIALOG_ERROR){
                        Trace.save("splashActivity: complete: Dialog: error");

                        switch (button) {
                            // Проверяется, что нажималась позитивная кнопка
                            case android.app.Dialog.BUTTON_POSITIVE:
                                Trace.save("splashActivity: complete: Dialog: error: positive");
                                finish();
                                break;

                                // Проверяется, что произошел cancel
                            case Dialog.BUTTON_CANCEL:
                                Trace.save("splashActivity: complete: Dialog: error: cancel");
                                finish();
                                break;

                            default:
                                Trace.save("splashActivity: complete: Dialog: error: unknown button");
                                Dialog.showError(this);
                                break;
                        }
                    }
                    else {
                        Trace.save("splashActivity: complete: Dialog: unknown dialog");
                        Dialog.showError(this);
                    }
                }
            }
            else {
                Trace.save("splashActivity: complete: unknown cc");
                Dialog.showError(this);
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Trace.save("splashActivity: complete: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Trace.save(i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }

    /**
     * Создает диалог для текущего Activity
     * @param id идентификатор создаваемого диалога
     * @return созданный диалог
     */
    @Override
    protected android.app.Dialog onCreateDialog(int id) {
        if (Dialog.getDialog() == null) {
            Trace.save("splashActivity: onCreateDialog: dialog: null");
            Dialog.showError(this);
            return super.onCreateDialog(id);
        }

        return Dialog.getDialog().onCreateDialog(id);
    }
}
