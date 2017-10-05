package com.merdeev.experiment;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

/**
 * Created by r.merdeev on 02.10.2017.
 */

public class RequestList extends AsyncTask {
    private StringBuilder result;
    private String resource;
    private String reference;
    private ArrayList<String> list;
    private CompleteListener o;

    RequestList(CompleteListener o, String resource, String reference) {
        super();
        if (resource.contains("mail.ru") == false) return;
        this.resource = resource;
        this.reference = reference;
        this.o = o;
        list = new ArrayList<>();

        this.execute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        this.o.complete(this, list);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d(MainActivity.LOG, "requestList: doInBackground");
        try {
            String temp;
            Connection con = Jsoup.connect(resource+reference);

            Document doc = con.get();
            temp = doc.body().toString();
            temp = temp.substring(temp.lastIndexOf("window.app"));
            temp = temp.substring(temp.indexOf("items"));
            temp = temp.split("]")[0];
            String[] atemp = temp.split("\"");

            for (int i=2; i<atemp.length; i+=2) {
                list.add(atemp[i].split(reference)[1] + "\n");
            }

        }
        catch (Exception e) {
            Log.d(MainActivity.LOG, "requestList: doInBackground: Exception: " + e.getMessage());
        }
        return null;
    }
}
