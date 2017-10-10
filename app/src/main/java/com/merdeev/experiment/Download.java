package com.merdeev.experiment;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Загружает файл в файловую систему
 * @author R.Z.Merdeev
 */
public class Download extends AsyncTask {

    /** Слушатель завершения задачи */
    private CompleteListener cl;

    /** Ссылка для скачивания */
    private String url;

    /** Адрес хранения файла */
    private String result;

    /** Максимальный размер загружаемого файла */
    private static final int MAX_SIZE = 20 * 1024 * 1024;

    /**
     * Конструктор
     * @param cl слушатель завершения
     * @param url ссылка для скачивания
     */
    public Download(CompleteListener cl, String url) {
        // Проверяется, тот ли это url, который ожидаем
        if (url.contains("mail.ru") == false) return;

        // Сохраняются переданные параметры
        this.cl = cl;
        this.url = url;

        // Запускается асинхронная задача
        this.execute();
    }

    /**
     * Выполняет фоновую работу по загрузке файла
     * @param objects
     * @return
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(MainActivity.LOG, "download: doInBackground");

        try {
            // Создается подпапка в папке хранения файла
            final String path = "Experiment";
            File f = createFile(path);
            f.mkdirs();

            // Создается URL по ссылке на скачивание
            URL u = new URL(url);

            // Открывается соединение
            URLConnection uc = u.openConnection();
            uc.connect();
            uc.getContent();

            // Сохраняются параметры загружаемого файла
            String contentType = uc.getContentType();
            int contentLength = uc.getContentLength();
            Log.d(MainActivity.LOG, "download: doInBackground: file: type = \"" + contentType + "\", length = " + contentLength);

            // Проверяется размер загружаемого файла, что он меньше максимального размера
            if (contentLength < MAX_SIZE) {
                File file;

                // Проверяется тип загружаемого файла
                if ("image/jpeg".equals(contentType)) {
                    result = path + "/" + getFileName(u) + ".jpg";
                } else if ("text/plain".equals(contentType)) {
                    result = path + "/" + getFileName(u) + ".txt";
                } else if ("image/gif".equals(contentType)) {
                    result = path + "/" + getFileName(u) + ".gif";
                } else {
                    Log.d(MainActivity.LOG, "download: doInBackground: unknown file type");
                    return null;
                }

                // Создается файл в файловой системе для сохранения загружаемого файла
                file = createFile(result);

                // Загружаются данные в файл
                saveBinaryFile(uc, contentLength, u, file);
                Log.d(MainActivity.LOG, "download: doInBackground: saved: " + file.getPath());
            } else {
                Log.d(MainActivity.LOG, "download: doInBackground: too big file");
                return null;
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Log.d(MainActivity.LOG, "download: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }

        return null;
    }

    /**
     * Уведомляет слушателя об окончании выполнения задачи и отдает результат
     * @param o
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        try {
            // Уведомляется слушатель о завершении задачи, передается результат
            cl.complete(this, result);
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Log.d(MainActivity.LOG, "requestList: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
    }

    /**
     * Загружает данные в файл
     * @param uc открытое соединение
     * @param contentLength размер файла
     * @param u ссылка на скачивание
     * @param fileName файл хранения
     * @throws IOException
     */
    private void saveBinaryFile(URLConnection uc, int contentLength, URL u, File fileName) throws IOException {
        InputStream raw = uc.getInputStream();
        InputStream in = new BufferedInputStream(raw);
        byte[] data = new byte[contentLength];
        int bytesRead = 0;
        int offset = 0;
        while (offset < contentLength) {
            bytesRead = in.read(data, offset, data.length - offset);
            if (bytesRead == -1)
                break;
            offset += bytesRead;
        }
        in.close();

        if (offset != contentLength) {
            throw new IOException("Only read " + offset
                    + " bytes; Expected " + contentLength + " bytes");
        }

        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.flush();
        out.close();
    }

    /**
     * Получает имя файла по URL
     * @param url URL
     * @return имя файла
     * @throws Exception
     */
    private String getFileName(URL url) throws Exception {
        // Определяется полное имя файла по URL
        String fileName = url.getFile();

        // Отбрасывается путь до файла
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        // Отбрасывается расширение файла и возвращается результат
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * Создает/открывает файл в файловой системе в папке загрузок
     * @param path путь до файла с расширением
     * @return файл
     * @throws Exception
     */
    static File createFile(String path) throws Exception {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path);
    }
}
