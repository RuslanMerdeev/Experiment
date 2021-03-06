package com.merdeev.experiment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
class Download extends AsyncTask {

    /** Слушатель завершения задачи */
    private CompleteListener cl;

    /** Ссылка для скачивания */
    private String url;

    /** Результат работы */
    private Object result;

    /** Тип результата работы */
    private Class type;

    /** Имя приложения для создания папки */
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
    Download(CompleteListener cl, String url, String app_name, boolean save) {
        Trace.save("download: constructor");

        // Проверяется, тот ли это url, который ожидаем
        if (!url.contains("mail.ru")) return;

        // Сохраняются переданные параметры
        this.cl = cl;
        this.url = url;
        this.save = save;
        this.app_name = app_name;

        // Запускается асинхронная задача
        if (this.execute() == null) {
            Trace.save("download: constructor: can't execute AsyncTask");
        }
    }

    /**
     * Выполняет фоновую работу по загрузке файла
     * @param objects массив объектов
     * @return объект
     */
    @Override
    protected Object doInBackground(Object[] objects) {
        Trace.save("download: doInBackground");

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
            Trace.save("download: doInBackground: file: type = \"" + contentType + "\", length = " + contentLength);

            // Определяется расширение загруженного файла по его типу
            contentType = getExtension(contentType);
            if (contentType == null) {
                Trace.save("download: doInBackground: unknown file type");
                return null;
            }

            // Проверяется размер загруженного файла, что он меньше максимального размера
            if (contentLength < MAX_SIZE) {
                // Проверяется, что необходимо сохранить файл в файловой системе
                if (save) {
                    // Путь - папке с именем приложения
                    String path = app_name;

                    // Создается файл в файловой системе для сохранения загружаемого файла
                    path = path + "/" + getFileName(u) + contentType;
                    File file = createFile(path);

                    // Сохраняется файл
                    saveBinaryFile(uc, contentLength, file);
                    result = new URI(file.getAbsolutePath());
                    type = URI.class;
                    Trace.save("download: doInBackground: saved: " + file.getPath());
                }
                // Проверяется, что нет необходимости сохранять файл
                else {
                    // Сохраняется массив байт
                    result = saveByteArray(uc, contentLength);

                    if (contentType.equals(".txt")) type = String.class;
                    else type = Bitmap.class;
                }
                Trace.save("download: doInBackground: loaded: " + getFileName(u) + contentType);
            } else {
                Trace.save("download: doInBackground: too big file");
                return null;
            }
        }
        // Выводится трейс для исключения
        catch (Exception e) {
            Trace.save("download: doInBackground: " + e.getClass() + ": " + e.getMessage());
            StackTraceElement[] el = e.getStackTrace();
            for (StackTraceElement i : el) {
                Trace.save(i.getFileName() + ": " + i.getLineNumber() + ": " + i.getMethodName());
            }
        }

        return null;
    }

    /**
     * Уведомляет слушателя об окончании выполнения задачи и отдает результат
     * @param o объект
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        // Уведомляется слушатель о завершении задачи, передается результат
        cl.complete(this, result, type);
    }

    /**
     * Создает/открывает файл в файловой системе в папке загрузок
     * @param path путь до файла с расширением
     * @return файл
     * @throws Exception исключение
     */
    static File createFile(String path) throws Exception {
        return new File(Environment.getExternalStorageDirectory(), path);
    }

    /**
     * Получает имя файла по URL
     * @param url URL
     * @return имя файла
     * @throws Exception исключение
     */
    private String getFileName(URL url) throws Exception {
        // Определяется полное имя файла по URL
        String fileName = url.getFile();

        // Отбрасывается путь до файла
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);

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
     * Возвращает данные в виде массива байт
     * @param uc открытое соединение
     * @param contentLength размер файла
     * @return массив байт
     * @throws Exception исключение
     */
    private byte[] saveByteArray(URLConnection uc, int contentLength) throws Exception {
        Trace.save("download: saveByteArray");

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

        return data;
    }

    /**
     * Сохраняет файл
     * @param uc открытое соединение
     * @param contentLength размер файла
     * @param fileName файл хранения
     * @throws Exception исключение
     */
    private void saveBinaryFile(URLConnection uc, int contentLength, File fileName) throws Exception {
        Trace.save("download: saveBinaryFile");

        byte[] data = saveByteArray(uc, contentLength);

        FileOutputStream out = new FileOutputStream(fileName);
        out.write(data);
        out.flush();
        out.close();
    }

    /**
     * Создает Bitmap из массива байт
     * @param b массив байт
     * @return bitmap
     * @throws Exception исключение
     */
    static Bitmap createBitmapFromByteArray(byte[] b) throws Exception {
        // Массив байт преобразуется в ByteArrayInputStream
        ByteArrayInputStream stream = new ByteArrayInputStream(b);

        // Возвращается Bitmap
        return BitmapFactory.decodeStream(stream);
    }

    /**
     * Создает текст из массива байт
     * @param b массив байт
     * @return текст
     * @throws Exception исключение
     */
    static String createTextFromByteArray(byte[] b) throws Exception {
        // Массив байт преобразуется в ByteArrayOutputStream
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(b);

        // Возвращается строка нужной кодировки
        return stream.toString("Cp1251");
    }

    /**
     * Возвращает Bitmap из файла в файловой системе в папке загрузок
     * @param uri ссылка на файл
     * @return bitmap
     * @throws Exception исключение
     */
    static Bitmap createBitmapFromFile(URI uri) throws Exception {
        return BitmapFactory.decodeStream(new BufferedInputStream(new FileInputStream(uri.getPath())));
    }

    /**
     * Возвращает строку текста из файла в файловой системе в папке загрузок
     * @param uri ссылка на файл
     * @return строка
     * @throws Exception исключение
     */
    static String createTextFromFile(URI uri) throws Exception {
        // Создается InputStream из файла
        InputStream is = new BufferedInputStream(new FileInputStream(uri.getPath()));

        // Создается ByteArrayOutputStream объект
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // В него считываются данные из файла
        int b;
        while ((b = is.read()) != -1) {
            stream.write(b);
        }
        is.close();

        // Возвращается строка нужной кодировки
        return stream.toString("Cp1251");
    }
}
