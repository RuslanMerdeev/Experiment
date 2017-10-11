package com.merdeev.experiment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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

    /** Результат работы */
    private Object result;

    /** Данные из ресурсов */
    private String app_name;

    /** Максимальный размер загружаемого файла */
    private static final int MAX_SIZE = 20 * 1024 * 1024;

    /** Признак необходимости сохранения файла */
    private boolean save;

    /**
     * Конструктор,
     * сохраняет параметры и запускает асинхронную задачу
     * @param cl слушатель завершения
     * @param url ссылка для скачивания
     * @param app_name название приложения
     * @param save режим загрузки
     */
    public Download(CompleteListener cl, String url, String app_name, boolean save) {
        // Проверяется, тот ли это url, который ожидаем
        if (url.contains("mail.ru") == false) return;

        // Сохраняются переданные параметры
        this.cl = cl;
        this.url = url;
        this.save = save;
        this.app_name = app_name;

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
            // Создается URL по ссылке на скачивание
            URL u = new URL(url);

            // Открывается соединение и загружаются данные
            URLConnection uc = u.openConnection();
            uc.connect();
            uc.getContent();

            // Сохраняются параметры загруженного файла
            String contentType = uc.getContentType();
            int contentLength = uc.getContentLength();
            Log.d(MainActivity.LOG, "download: doInBackground: file: type = \"" + contentType + "\", length = " + contentLength);

            // Определяется расширение загруженного файла по его типу
            contentType = getExtension(contentType);
            if (contentType == null) {
                Log.d(MainActivity.LOG, "download: doInBackground: unknown file type");
                return null;
            }

            // Проверяется размер загруженного файла, что он меньше максимального размера
            if (contentLength < MAX_SIZE) {
                // Проверяется, что необходимо сохранить файл в файловой системе
                if (save) {
                    // Создается подпапка в папке хранения файла
                    String path = app_name;
                    File f = createFile(path);
                    f.mkdirs();

                    // Создается файл в файловой системе для сохранения загружаемого файла
                    path = path + "/" + getFileName(u) + contentType;
                    File file = createFile(path);

                    // Сохраняется файл
                    saveBinaryFile(uc, contentLength, file);
                    result = new URI(file.getAbsolutePath());
                    Log.d(MainActivity.LOG, "download: doInBackground: saved: " + file.getPath());
                }
                // Проверяется, что нет необходимости сохранять файл
                else {
                    if (contentType.equals(".txt")) {
                        // Сохраняется текст
                        result = saveText(uc, contentLength);
                        Log.d(MainActivity.LOG, "download: doInBackground: loaded: text");
                    }
                    else {
                        // Сохраняется Bitmap
                        result = saveBitmap(uc, contentLength);
                        Log.d(MainActivity.LOG, "download: doInBackground: loaded: image");
                    }
                }
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
            Log.d(MainActivity.LOG, "download: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Log.d(MainActivity.LOG, i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }
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
     * Определяет расширение загружаемого файла по его типу
     * @param type тип файла
     * @return расширение
     */
    private String getExtension(String type) {
        if ("image/jpeg".equals(type)) return ".jpg";
        else if ("image/gif".equals(type)) return ".gif";
        else if ("image/png".equals(type)) return ".png";
        else if ("text/plain".equals(type)) return ".txt";
        else return null;
    }

    /**
     * Сохраняет файл
     * @param uc открытое соединение
     * @param contentLength размер файла
     * @param fileName файл хранения
     * @throws IOException
     */
    private void saveBinaryFile(URLConnection uc, int contentLength, File fileName) throws IOException {
        InputStream in = new BufferedInputStream(uc.getInputStream());
        byte[] data = new byte[contentLength];
        int bytesRead;
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
     * Сохраняет Bitmap
     * @param uc открытое соединение
     * @param contentLength размер файла
     * @return bitmap
     * @throws Exception
     */
    Bitmap saveBitmap(URLConnection uc, int contentLength) throws Exception {
        InputStream is = new BufferedInputStream(uc.getInputStream());
        return BitmapFactory.decodeStream(is);
    }

    /**
     * Сохраняет текст
     * @param uc открытое соединение
     * @param contentLength размер файла
     * @return текст
     * @throws Exception
     */
    String saveText(URLConnection uc, int contentLength) throws Exception {
        // Создается InputStream из соединения
        InputStream is = new BufferedInputStream(uc.getInputStream());

        // Создается ByteArrayOutputStream объект
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int bytesRead;
        int offset = 0;
        while (offset < contentLength) {
            bytesRead = is.read();
            baos.write(bytesRead);
            if (bytesRead == -1)
                break;
            offset += bytesRead;
        }
        is.close();

        if (offset != contentLength) {
            throw new IOException("Only read " + offset
                    + " bytes; Expected " + contentLength + " bytes");
        }

        // Возвращается строка нужной кодировки
        return baos.toString("Cp1251");
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

    /**
     * Возвращает Bitmap из файла в файловой системе в папке загрузок
     * @param path путь до файла с расширением
     * @return bitmap
     * @throws Exception
     */
    static Bitmap createBitmapFromFile(String path) throws Exception {
        return BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(path)));
    }

    /**
     * Возвращает строку текста из файла в файловой системе в папке загрузок
     * @param path путь до файла с расширением
     * @return строка
     * @throws Exception
     */
    static String createTextFromFile(String path) throws Exception {
        // Создается inputstream из файла
        InputStream is = new BufferedInputStream(new FileInputStream(path));

        // Создается ByteArrayOutputStream объект
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // В него считываются данные из файла
        int b;
        while ((b = is.read()) != -1) {
            baos.write(b);
        }
        is.close();

        // Возвращается строка нужной кодировки
        return baos.toString("Cp1251");
    }
}
