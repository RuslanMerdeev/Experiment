package com.merdeev.experiment;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

/**
 * Created by r.merdeev on 05.10.2017.
 */

public class D_temp extends AsyncTask {

    private CompleteListener o;
    private String url;

//    private static final String CHARS = "1234567890QWERTYUIOPASDFGHJKLZXCVBNM";
//    private static final int ID_LENGTH = 6;
//    private static final String STR_MESSAGE = "(%d) %s";
//    private static final String STR_SUCCESS = "(%d) %s %d/%d=%f %s %d %s";
//    private static final String FILE_NAME = "ifs%d.txt";
//    private static final String URL = "http://files.goodresource.net/files/get?fileId=%s";
    private static final int MAX_SIZE = 20 * 1024 * 1024;

//    private final Random random = new Random();
//    private int mSuccess = 0;
//    private int mTotal = 0;

    public D_temp(CompleteListener o, String url) {
        this.o = o;
        this.url = url;

        this.execute();
    }


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

//    private void writeToFile(final String fileName, final String out) {
//        FileWriter fWriter = null;
//        BufferedWriter writer = null;
//        try {
//            fWriter = new FileWriter(fileName, true);
//            writer = new BufferedWriter(fWriter);
//            writer.append(out);
//            writer.newLine();
//        } catch (IOException e) {
//            Log.d(MainActivity.LOG, "download: doInBackground: exception: "+e.getMessage());
//        } finally {
//            try {
//                if (writer != null)
//                    writer.close();
//            } catch (IOException e) {
//            }
//        }
//    }

    private String getFileName(URL url) {
        String fileName = url.getFile();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        final String path = "experiment";
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path);//Environment.getDataDirectory(), path);
        f.mkdirs();

//        final String fileName = String.format(FILE_NAME, mThreadId);
//        while (true) {
//            mTotal++;
//            String s = generateString(random, CHARS, ID_LENGTH);
//            final String url = String.format(URL, s);
            try {
                URL u = new URL(url);

                URLConnection uc = u.openConnection();
                uc.connect();
                uc.getContent();
                String contentType = uc.getContentType();
                int contentLength = uc.getContentLength();
                Log.d(MainActivity.LOG, "download: doInBackground: file: type = \"" + contentType + "\", length = " + contentLength);
//                String status = "done    ";
                if(contentLength < MAX_SIZE) {
                    if ("image/jpeg".equals(contentType)) {
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path + "/" + getFileName(u) + ".jpg");
                        saveBinaryFile(uc, contentLength, u, file);
                        Log.d(MainActivity.LOG, "download: doInBackground: saved: " + file.getPath());
                    } else if ("text/plain".equals(contentType)){
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path + "/" + getFileName(u)+".txt");
                        saveBinaryFile(uc, contentLength, u, file);
                        Log.d(MainActivity.LOG, "download: doInBackground: saved: " + file.getPath());
                    } else if ("image/gif".equals(contentType)){
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path + "/" + getFileName(u)+".gif");
                        saveBinaryFile(uc, contentLength, u, file);
                        Log.d(MainActivity.LOG, "download: doInBackground: saved: " + file.getPath());
//                    }else if("image/tiff".equals(contentType)){
//                        File file = new File(path, s + ".tiff");
//                        saveBinaryFile(uc, contentLength, u, file);
//                    }else if("image/x-ms-bmp".equals(contentType)){
//                        File file = new File(path, s + ".bmp");
//                        saveBinaryFile(uc, contentLength, u, file);
//                    }else if("image/png".equals(contentType)){
//                        File file = new File(path, s + ".png");
//                        saveBinaryFile(uc, contentLength, u, file);
//                    }else if("image/gif".equals(contentType)){
//                        File file = new File(path, s + ".gif");
//                        saveBinaryFile(uc, contentLength, u, file);
//                    }else if("application/x-rar-compressed".equals(contentType)){
//                        File file = new File(path, s + ".gif");
//                        saveBinaryFile(uc, contentLength, u, file);
//                    }else if("application/zip".equals(contentType)){
//                        File file = new File(path, s + ".gif");
//                        saveBinaryFile(uc, contentLength, u, file);
//                    }else if("application/7z".equals(contentType)){
//                        File file = new File(path, s + ".gif");
//                        saveBinaryFile(uc, contentLength, u, file);
                    }else{
                        Log.d(MainActivity.LOG, "download: doInBackground: unknown file type");
                    }
                }else{
                    Log.d(MainActivity.LOG, "download: doInBackground: too big file");
                }
//                mSuccess++;
//                final double p = mSuccess / mTotal;
//                final String out = String.format(STR_SUCCESS,
//                        mThreadId, status, mSuccess, mTotal, p, contentType,
//                        contentLength, url);
//                System.out.println(out);
//                writeToFile(fileName, out);
            } catch (MalformedURLException e) {
                Log.d(MainActivity.LOG, "download: doInBackground: exception: "+e.getMessage());
            } catch (FileNotFoundException e) {
                Log.d(MainActivity.LOG, "download: doInBackground: exception: "+e.getMessage());
            } catch (IOException e) {
                Log.d(MainActivity.LOG, "download: doInBackground: exception: "+e.getMessage());
            }
//        }
        return null;
    }
}
