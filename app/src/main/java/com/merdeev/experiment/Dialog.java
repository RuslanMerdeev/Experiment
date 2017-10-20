package com.merdeev.experiment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Создает диалог
 * @author R.Z.Merdeev
 */
class Dialog implements DialogInterface.OnClickListener {

    /** Nдентификатор диалога для списка имен файлов/папок текущей директории */
    static final int DIALOG_LIST = 1;

    /** Nдентификатор диалога прогресса */
    static final int DIALOG_PROGRESS = 2;

    /** Nдентификатор диалога ошибки */
    static final int DIALOG_ERROR = 3;

    /** Nдентификатор для cancel */
    static final int BUTTON_CANCEL = -4;

    /** Созданный диалог */
    private static Dialog dialog;

    /** Текущий контекст */
    private Context context;

    /** Nдентификатор диалога для отображения */
    private int dialog_id;

    /** Заголовок диалога */
    private String title;

    /** Данные диалога */
    private Object data;

    /**
     * Конструктор,
     * сохраняет параметры и заново создает диалог
     * @param context внутренний контекст
     * @param dialog_id идентификатор диалога
     * @param title заголовок
     * @param data данные
     */
    Dialog(Context context, int dialog_id, String title, Object data) {
        this.context = context;
        this.dialog_id = dialog_id;
        this.title = title;
        this.data = data;
    }

    /**
     * Создает диалог для текущего Activity
     * @param id идентификатор создаваемого диалога
     * @return созданный диалог
     */
    protected android.app.Dialog onCreateDialog(int id) {
        AlertDialog.Builder adb;

        switch (id) {
            // Проверяется, что нужно создать диалог для списка файлов/папок текущей директории облака
            case DIALOG_LIST:
                Trace.save("dialog: onCreateDialog: list");

                // Создается builder для диалога
                adb = new AlertDialog.Builder(context);

                // Устанавливается заголовок списка + смещение для информирования
                adb.setTitle(title);

                // Формируется список имен
                ArrayList<String> names = (ArrayList<String>)data;

                // Создается адаптер для списка
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item, names);

                // Builder-ом устанавливается адаптер, нейтральная и негативная кнопки для диалога
                // Выбор пункта обрабатывается текущим Activity
                adb.setAdapter(adapter, this);
                adb.setNeutralButton(R.string.cancel, this);
                adb.setNegativeButton(R.string.back, this);

                // Создание и возврат диалога
                return adb.create();

            // Проверяется, что нужно создать диалог прогресса
            case DIALOG_PROGRESS:
                Trace.save("dialog: onCreateDialog: progress");

                // Создается диалог прогресса
                ProgressDialog pd = new ProgressDialog(context);

                // Устанавливается заголовок списка + смещение для информирования
                pd.setMessage((String)data);

                // Устанавливается запрет на выход из диалога по кнопке назад
                pd.setCancelable(false);

                // Отображается диалог
                pd.show();

                return pd;

            // Проверяется, что нужно создать диалог ошибки
            case DIALOG_ERROR:
                Trace.save("dialog: onCreateDialog: error");

                // Создается builder для диалога
                adb = new AlertDialog.Builder(context);

                // Устанавливается сообщение ошибки
                adb.setMessage((String)data);

                // Устанавливается кнопка OK
                adb.setPositiveButton(R.string.ok, this);

                // Устанавливается запрет на выход из диалога по кнопке назад
                adb.setCancelable(false);

                // Устанавливается временное ограничение на работу диалога
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        cancel(context, DIALOG_ERROR);
                    }
                }, 5000);

                // Создание и возврат диалога
                return adb.create();
        }
        return this.onCreateDialog(id);
    }

    /**
     * При нажатии кнопки диалога
     * @param dialogInterface диалог
     * @param i идентификатор
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            // Проверяется, что нажималась нейтральная кнопка
            case android.app.Dialog.BUTTON_NEUTRAL:
                Trace.save("dialog: onClick: neutral");
                break;

            // Проверяется, что нажималась негативная кнопка
            case android.app.Dialog.BUTTON_NEGATIVE:
                Trace.save("dialog: onClick: negative");
                break;

            // Проверяется, что нажималась позитивная кнопка
            case android.app.Dialog.BUTTON_POSITIVE:
                Trace.save("dialog: onClick: positive");
                break;

            // Проверяется, что выбирался пункт списка
            default:
                Trace.save("dialog: onClick: item: " + i);
                break;
        }
        dialog = null;
        context.complete(this, new int[]{dialog_id, i}, int[].class);
    }

    /**
     * Запускает диалог
     * @param context контекст
     * @param dialog_id идентификатор
     * @param title заголовок
     * @param data данные
     */
    private static void start(Context context, int dialog_id, String title, Object data) {
        // Проверяется, что диалогов сейчас нет
        if (dialog == null) {
            dialog = new Dialog(context, dialog_id, title, data);
            context.removeDialog(dialog_id);
            context.showDialog(dialog_id);
        }
    }

    /**
     * Уничтожает диалог
     * @param context контекст
     * @param dialog_id идентификатор
     */
    private static void cancel(Context context, int dialog_id) {
        Trace.save("dialog: cancel");

        // Проверяется, что какой-то диалог есть
        if (dialog != null) {
            Dialog temp = dialog;
            dialog = null;
            context.removeDialog(dialog_id);
            context.complete(temp, new int[]{dialog_id, BUTTON_CANCEL}, int[].class);
        }
    }

    /**
     * Запускает отображение диалога прогресса
     * @param context контекст
     */
    static void showProgress(Context context) {
        start(context, DIALOG_PROGRESS, "", "Загрузка...");
    }

    /**
     * Запускает отображение диалога ошибки
     * @param context контекст
     */
    static void showError(Context context) {
        start(context, DIALOG_ERROR, "", "Что-то пошло не так :(");
    }

    /**
     * Запускает отображение диалога для списка
     * @param context контекст
     * @param title заголовок
     * @param arrayList список
     */
    static void showList(Context context, String title, ArrayList<String> arrayList) {
        start(context, DIALOG_LIST, title, arrayList);
    }

    /**
     * Уничтожает диалог прогресса
     * @param context контекст
     */
    static void finishProgress(Context context) {
        cancel(context, DIALOG_PROGRESS);
    }

    /**
     * Возвращает созданный диалог
     * @return диалог
     */
    static Dialog getDialog() {
        return dialog;
    }
}
