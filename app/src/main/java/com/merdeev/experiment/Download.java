package com.merdeev.experiment;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by r.merdeev on 05.10.2017.
 */

public class Download extends AsyncTask {

    private CompleteListener cl;
    private String url;
    private String result = null;

    private static final int MAX_SIZE = 20 * 1024 * 1024;

    public Download(CompleteListener cl, String url) {
        this.cl = cl;
        this.url = url;

        this.execute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {

        final String path = "experiment";
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path);//Environment.getDataDirectory(), path);
        f.mkdirs();

        try {
            URL u = new URL(url);

            URLConnection uc = u.openConnection();
            uc.connect();
            uc.getContent();
            String contentType = uc.getContentType();
            int contentLength = uc.getContentLength();
            Log.d(MainActivity.LOG, "download: doInBackground: file: type = \"" + contentType + "\", length = " + contentLength);

            if(contentLength < MAX_SIZE) {
                File file;
                if ("image/jpeg".equals(contentType)) {
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path + "/" + getFileName(u) + ".jpg");
                } else if ("text/plain".equals(contentType)){
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path + "/" + getFileName(u)+".txt");
                } else if ("image/gif".equals(contentType)){
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path + "/" + getFileName(u)+".gif");
                }else{
                    Log.d(MainActivity.LOG, "download: doInBackground: unknown file type");
                    return null;
                }
                saveBinaryFile(uc, contentLength, u, file);
                Log.d(MainActivity.LOG, "download: doInBackground: saved: " + file.getPath());
                result = file.getPath();
            }else{
                Log.d(MainActivity.LOG, "download: doInBackground: too big file");
                return null;
            }
        } catch (Exception e) {
            Log.d(MainActivity.LOG, "download: doInBackground: " + e.getClass() + ": " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        cl.complete(this, result);
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

    private String getFileName(URL url) {
        String fileName = url.getFile();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
