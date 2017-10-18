package com.merdeev.experiment;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Выдает трейсы в logcat и записывает в файл
 * @author R.Z.Merdeev
 */
class Trace {

    /** Строка-константа для логов */
    private final static String LOG = "States";

    /** Файл хранения трейсов */
    private static File file = null;

    /**
     * Конструктор,
     * создает файл для хранения трейсов
     * @param folder путь к папке
     * @param name имя файла
     */
    Trace(String folder, String name) {

        Log.d(LOG, System.currentTimeMillis() + " " + "trace: constructor");

        try {
            // Создается папка с именем приложения
            file = Download.createFile(folder);
            if (!file.mkdirs()) Log.d(LOG, System.currentTimeMillis() + " " + "trace: constructor: folder already exist");

            // Создается пустой log.txt файл
            file = Download.createFile(folder + "/" + name);
            if (!file.delete()) Log.d(LOG, System.currentTimeMillis() + " " + "trace: constructor: first creating file");
            file.createNewFile();
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Log.d(LOG, System.currentTimeMillis() + " " + "trace: constructor: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(LOG, System.currentTimeMillis() + " " + i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }

    /**
     * Сохраняет трейсы
     * @param string текст трейса
     */
    static void save(String string) {
        try {
            Log.d(LOG, System.currentTimeMillis() + " " + string);

            if (file == null) Log.d(LOG, System.currentTimeMillis() + " " + "trace: save: file: null");
            else {
                if (!file.isFile()) Log.d(LOG, System.currentTimeMillis() + " " + "trace: save: file: not file: " + file.getAbsolutePath());
                else {
                    appendText(file, System.currentTimeMillis() + " " + string + "\n");
                }
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Log.d(LOG, System.currentTimeMillis() + " " + "trace: save: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(LOG, System.currentTimeMillis() + " " + i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }

    /**
     * Добавляет в конец файла тектс
     * @param file файл
     * @param text текст
     * @throws Exception исключение
     */
    private static void appendText(File file, String text) throws Exception {
        FileOutputStream stream = new FileOutputStream(file, true);
        stream.write(text.getBytes(), 0, text.length());
        stream.flush();
        stream.close();
    }
}
