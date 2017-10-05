package com.merdeev.experiment;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by r.merdeev on 03.10.2017.
 */

// This class downloads a file from a URL.
class Download extends AsyncTask {

    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;


    private URL url; // download URL
    private int size; // size of download in bytes
    private int downloaded; // number of bytes downloaded
    private CompleteListener o;
    private String cont;

    // Constructor for Download.
    public Download(CompleteListener o, URL url) {
        this.url = url;
        this.o = o;

        // Begin the download.
        this.execute();
    }

    // Get this download's URL.
    public String getUrl() {
        return url.toString();
    }

    // Get this download's size.
    public int getSize() {
        return size;
    }

    // Get this download's progress.
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        this.o.complete(this, cont);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(MainActivity.LOG, "download: doInBackground");
        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            // Open connection to URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Log.d(MainActivity.LOG, "download: doInBackground: url=\""+url.getPath()+"\"");

            // Specify what portion of file to download.
            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

            // Connect to server.
            connection.connect();

            Log.d(MainActivity.LOG, "download: doInBackground: resp code="+connection.getResponseCode());
            // Make sure response code is in the 200 range.
            if (connection.getResponseCode() / 100 != 2) {
                Log.d(MainActivity.LOG, "download: doInBackground: error: wrong resp code");
                return null;
            }

            Log.d(MainActivity.LOG, "download: doInBackground: content length="+connection.getContentLength());
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                Log.d(MainActivity.LOG, "download: doInBackground: error: wrong content length");
                return null;
            }

      /* Set the size for this download if it
         hasn't been already set. */
            if (size == -1) {
                size = contentLength;
            }

            // Open file and seek to the end of it.
            file = new RandomAccessFile(getFileName(url), "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();

            while (true) {
        /* Size buffer according to how much of the
           file is left to download. */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1)
                    break;

                // Write buffer to file.
                file.write(buffer, 0, read);
                downloaded += read;
                Log.d(MainActivity.LOG, "download: doInBackground: progress: " + getProgress() + "%");
            }
            cont = file.readLine();

        } catch (Exception e) {
            Log.d(MainActivity.LOG, "download: doInBackground: exception: " + e.getMessage());
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {}
            }

            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {}
            }
        }
        return null;
    }

    // Get file name portion of URL.
    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
}
